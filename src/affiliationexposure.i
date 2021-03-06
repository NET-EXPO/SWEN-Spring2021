%module affiliationexposure

%{
#include "affiliationexposure.h"
%}

%include "affiliationexposure.h"

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

int* int_array(int size) {
    int *first_mode_array = (int*)calloc(size, sizeof(int));
    return first_mode_array;
 }

%}