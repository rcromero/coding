/*
 * SudokuTable.h
 *
 *  Created on: 17 de ago de 2018
 *      Author: Renato Romero
 */
#include "SudokuConstants.h"
#include "SudokuValue.h"

#ifndef SUDOKUTABLE_H_
#define SUDOKUTABLE_H_

struct LinkedValue {
  SudokuValue * curr;
  LinkedValue * next;
};

class SudokuTable {
private:
  // table to resolve, in bits
  int **table;

  // missing values
  int row[ROWS];
  int column[COLUMNS];
  int quadrant[QUADRANTS];

  // spots to resolve
  SudokuValue * tableToResolve[ROWS][COLUMNS];
  // organized by all, row, column and quadrant
  LinkedValue * toResolve;
  LinkedValue * rowToResolve[ROWS];
  LinkedValue * columnToResolve[COLUMNS];
  LinkedValue * quadrantToResolve[QUADRANTS];

  void          initValues();
  LinkedValue * createLinkedValue(SudokuValue *sudokuValue);
  LinkedValue * addLinkedValue(LinkedValue *linkedValue, SudokuValue *sudokuValue);
  LinkedValue * removeLinkedValue(LinkedValue *linkedValue, SudokuValue *sudokuValue);

  SudokuValue * getGuessValue();
  LinkedValue * checkUnique(LinkedValue * check[]);
  int           bitCount(int value);

public:
  SudokuTable(int **sudokuTable);
  void          guess();
  void          resolve(SudokuValue *value);
  void          optmizeByQuadrant();
  LinkedValue * checkUniqueRow();
  LinkedValue * checkUniqueColumn();
  LinkedValue * checkUniqueQuadrant();
  LinkedValue * getToResolve() {return this->toResolve;};
  int **        getTable()     {return this->table;    };

  virtual ~SudokuTable();

  void          printResult();
};

#endif /* SUDOKUTABLE_H_ */
