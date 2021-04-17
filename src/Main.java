import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static {
        System.loadLibrary("affiliationexposure");
        System.loadLibrary("networkexposure");
    }

    private static Map<String, List<String>> statistics = new ConcurrentHashMap<>();

    private static final String NETWORK = "NETWORK";
    private static final String AFFILIATION = "AFFILIATION";
    private static final String[] csvHeader = {"FileName", "Start", "End", "TimeTaken(MilliSeconds)"};

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("File Type and Directory name are mandatory for script");
        }

        String fileType = args[0];

        if (!NETWORK.equals(fileType) && !AFFILIATION.equals(fileType)) {
            System.err.println("File Type should be NETWORK or AFFILIATION");
        }

        String directoryName = args[1];

        try {
            for(String file: getAllFilesInDir(directoryName)) {
                if (NETWORK.equals(fileType)) {
                    if (file.endsWith("_y.csv")) {
                        String netFile = file.replaceAll("_y.csv$", ".csv");
                        executeNetworkExposure(directoryName + "/" + netFile,
                                directoryName + "/" +     file, true);
                    }
                } else if (AFFILIATION.equals(fileType)) {
                    if (file.endsWith("_y_mode.csv")) {
                        String netFile = file.replaceAll("_y_mode.csv$", ".csv");
                        executeAffiliationExposure(true, directoryName + "/" + netFile,
                                directoryName + "/" + file, "1");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't open the directory. Please give complete path of the directory" + e.getMessage());
        }

        writePerformanceToFile();

    }

    private static void executeAffiliationExposure(Boolean hasHeader, String netFile, String yFile, String firstModeFlag) {
        long start = System.currentTimeMillis();

        ModeInfo modeData = new ModeInfo();
        modeData.setFirst_mode_flag(firstModeFlag);
        affiliationexposure.setup_mode_data(yFile, hasHeader, modeData);

        SWIGTYPE_p_int firstModeArray = affiliationexposure.int_array(modeData.getFirst_mode_indices());
        SWIGTYPE_p_int secondModeArray = affiliationexposure.int_array(modeData.getSecond_mode_indices());

        SWIGTYPE_p_p_double yArray = affiliationexposure.double_array(modeData.getFirst_mode_indices(), 2);
        SWIGTYPE_p_p_double eArray = affiliationexposure.double_array(modeData.getFirst_mode_indices(), 2);

        affiliationexposure.read_y_mode_file(yFile, yArray, firstModeArray, secondModeArray, hasHeader, modeData.getFirst_mode_flag());

        SWIGTYPE_p_p_double aMatrix = affiliationexposure.generateAMatrix(netFile, firstModeArray,
                secondModeArray, hasHeader, modeData);
        SWIGTYPE_p_p_double aPrimeMatrix = affiliationexposure.generateAPrimeMatrix(aMatrix, modeData);
        SWIGTYPE_p_p_double cMatrix = affiliationexposure.transposeAMatrix(aMatrix, aPrimeMatrix, modeData);

        affiliationexposure.calc_affiliation_exposure(cMatrix, eArray, yArray, modeData);
        affiliationexposure.print_aff_exposure_values(yArray, eArray, modeData);

        long end = System.currentTimeMillis();
        saveStatistics(netFile, start, end);
    }

    private static void executeNetworkExposure(String netFile, String yFile, Boolean hasHeader) {
        long start = System.currentTimeMillis();
        int matrixDimension = networkexposure.getDimension(yFile, hasHeader);

        SWIGTYPE_p_p_double yArray = networkexposure.double_array(matrixDimension, 2);
        SWIGTYPE_p_p_double wMatrix = networkexposure.double_array(matrixDimension, matrixDimension);

        SWIGTYPE_p_int matrixDimensionPtr = networkexposure.new_intp();
        networkexposure.intp_assign(matrixDimensionPtr, matrixDimension);

        networkexposure.init_w_matrix(wMatrix, matrixDimensionPtr);

        SWIGTYPE_p_p_double eArray = networkexposure.double_array(matrixDimension, 2);

        networkexposure.read_y_file(yFile, yArray, hasHeader);
        networkexposure.read_net_file(netFile, wMatrix, hasHeader);

        networkexposure.calc_network_exposure(wMatrix, yArray, eArray, matrixDimension);

        networkexposure.print_net_exposure_values(yArray, eArray, matrixDimension);
        long end = System.currentTimeMillis();
        saveStatistics(netFile, start, end);
    }

    private static List<String> getAllFilesInDir(String directoryName) throws IOException  {
        File dir = new File(directoryName);
        if (dir.list() == null) {
            throw new IOException("No files under directory specified.");
        }
        System.out.println("Dir: " + directoryName);
        for(String s: dir.list()) {
            System.out.println("File: " + s);
        }
        return Arrays.asList(Objects.requireNonNull(dir.list()));
    }

    private static void saveStatistics(String fileName, long start, long end) {
        statistics.put(fileName, List.of(String.valueOf(start), String.valueOf(end), String.valueOf(end - start)));
    }

    private static void writePerformanceToFile() {

        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter("performance_statistics.csv"));
            bufferedWriter.write(String.join(",", csvHeader));
            bufferedWriter.write("\n");

            for(Map.Entry<String, List<String>> entry: statistics.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(entry.getKey()).append(",");
                stringBuilder.append(String.join(",", entry.getValue()));
                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.write("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                }
            } catch (Exception e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
    }

}