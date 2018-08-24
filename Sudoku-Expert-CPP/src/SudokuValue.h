/*
 * SudokuValue.h
 *
 *  Created on: 17 de ago de 2018
 *      Author: Renato Romero
 */
#include "SudokuConstants.h"

#ifndef SUDOKUVALUE_H_
#define SUDOKUVALUE_H_

class SudokuValue {
private:
  // index from the table
  int indexR, indexC, indexQ;
  // functions with possible values, "the 3 functions"
  int *row, *col, *qud;
  // possible values, each bit of the integer indicates which value is a solution for that spot
  int values;

public:
  SudokuValue(int r, int c, int q, int *row, int *column, int *quadrant);

  int getR() { return this->indexR; };
  int getC() { return this->indexC; };
  int getQ() { return this->indexQ; };

  void checkValues();
  bool hasOnlyOnePossibility();
  int  getNextPossibility()          { return this->values;   };
  void setNextPossiblity(int values) { this->values = values; };

  void optimize(int reduce);
  int  resolve();
  virtual ~SudokuValue();
};

#endif /* SUDOKUVALUE_H_ */
