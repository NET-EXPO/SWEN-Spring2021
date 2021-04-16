cmake_minimum_required (VERSION 3.0)
project (bridge)

find_package (Java REQUIRED)
find_package (JNI REQUIRED)
include (UseJava)

include_directories (
	${CMAKE_CURRENT_BINARY_DIR}
	${CMAKE_CURRENT_SOURCE_DIR}
	${JNI_INCLUDE_DIRS}
)

set (SOURCES
	counter.c
	jni_wrapper.c
)

add_library (counter SHARED ${SOURCES})
target_link_libraries (counter ${JAVA_JVM_LIBRARY})
add_jar (${PROJECT_NAME} Main.java Counter.java ENTRY_POINT Main)