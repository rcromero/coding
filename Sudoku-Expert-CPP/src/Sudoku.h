/*
 * Sudoku.h
 *
 *  Created on: 22 de ago de 2018
 *      Author: Renato Romero
 */

#include "SudokuConstants.h"

#ifndef SUDOKU_H_
#define SUDOKU_H_

int   convertDec2Bin(int decValue);
int   convertBin2Dec(int binValue);
int** copyTable(int **resolve);
int** copyTableDec2Bin(int resolve[ROWS][COLUMNS]);
void  resolveTable(int **init);

#endif /* SUDOKU_H_ */
