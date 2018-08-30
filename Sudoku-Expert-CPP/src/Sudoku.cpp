//============================================================================
// Name        : Sudoku.cpp
// Author      : Renato Romero
// Version     : 1.0
// Copyright   : Your copyright notice
// Description : Resolving Sudoku in C++, Ansi-style
//============================================================================

#include <ctime>
#include <iostream>
using namespace std;

#include "Sudoku.h"
#include "SudokuTable.h"

int main() {
  clock_t start = std::clock();

  int toResolveDec [ROWS][COLUMNS] = {
    { 0, 0, 3, 0, 0, 4, 0, 5, 8 },
    { 6, 0, 0, 1, 0, 0, 0, 0, 2 },
    { 2, 0, 4, 0, 0, 0, 0, 0, 0 },
    { 0, 7, 9, 0, 0, 0, 0, 0, 0 },
    { 1, 0, 0, 0, 8, 0, 3, 0, 0 },
    { 0, 0, 0, 6, 0, 0, 0, 0, 0 },
    { 3, 0, 5, 0, 0, 8, 0, 9, 0 },
    { 0, 2, 0, 0, 9, 0, 0, 0, 1 },
    { 8, 0, 0, 0, 0, 0, 0, 0, 0 }
  };

  // loop for benchmark
  //for(int i = 0; i < 100000; i++)
    resolveTable(copyTableDec2Bin(toResolveDec));

  clock_t finish = std::clock();
  std::cout << double(finish - start) << "ms" << std::endl;
  return 0;
}

int convertDec2Bin(int decValue) {
  if(decValue > 0)
    decValue = 1 << (decValue - 1);
  return decValue;
}

int convertBin2Dec(int binValue) {
  int decValue = 0;
  for(int value = 1; binValue > 0; binValue >>= 1, value++)
    if((binValue & 1) != 0)
      decValue += value;

  return decValue;
}

int** copyTable(int **resolve) {
  int** dest = 0;
  dest = new int*[ROWS];
  for(int j, i = 0; i < ROWS; i++) {
    dest[i] = new int[COLUMNS];
    for(j = 0; j < COLUMNS; j++)
      dest[i][j] = resolve[i][j];
  }
  return dest;
}

int** copyTableDec2Bin(int resolve[ROWS][COLUMNS]) {
  int** dest = 0;
  dest = new int*[ROWS];
  for(int j, i = 0; i < ROWS; i++) {
    dest[i] = new int[COLUMNS];
    for(j = 0; j < COLUMNS; j++)
      dest[i][j] = convertDec2Bin(resolve[i][j]);
  }
	return dest;
}

void resolveTable(int **init) {
  SudokuTable *sudokuTable = new SudokuTable(init);
  bool found;
  static bool resolved = false;
  SudokuValue * resolve = NULL;
  LinkedValue * check, * next;
  do {
    found = false;
    // check 3 functions
    check = sudokuTable->getToResolve();
    do {
      resolve = check->curr;
      check = check->next;
      if(resolve->hasOnlyOnePossibility()) {
        sudokuTable->resolve(resolve);
        found = true;
      }
    } while(check != NULL);

    for(check = sudokuTable->checkUniqueRow(); check != NULL; check = next) {
      sudokuTable->resolve(check->curr);
      found = true;

      next = check->next;
      delete check;
    }

    for(check = sudokuTable->checkUniqueColumn(); check != NULL; check = next) {
      sudokuTable->resolve(check->curr);
      found = true;

      next = check->next;
      delete check;
    }

    for(check = sudokuTable->checkUniqueQuadrant(); check != NULL; check = next) {
      sudokuTable->resolve(check->curr);
      found = true;

      next = check->next;
      delete check;
    }

    if(sudokuTable->getToResolve() != NULL) {
      if(found)
        continue;
      if(sudokuTable->optmizeByQuadrant())
        continue;
      sudokuTable->guess();
    } else {
      if(!resolved) {
        sudokuTable->printResult();

        resolved = true;
      }
    }
    break;
  } while(true);

  delete sudokuTable;
}

