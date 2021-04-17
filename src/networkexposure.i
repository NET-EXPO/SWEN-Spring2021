%module networkexposure

%include typemaps.i
%{
#include "networkexposure.h"
%}

%include "networkexposure.h"

%include cpointer.i
%pointer_functions(int, intp);

%inline %{
// Helper function to create a 2d array
double* *double_array(int rows, int cols) {
    int i;
    double **arr = (double **)malloc(rows * sizeof(double *));
    for (i=0; i<rows; i++)
        arr[i] = (double *)malloc(cols * sizeof(double));
    return arr;
 }

void *setitem(double **array, int row, int col, int value) {
    array[row][col] = value;
    }
%}