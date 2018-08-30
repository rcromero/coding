/*
 * SudokuValue.cpp
 *
 *  Created on: 17 de ago de 2018
 *      Author: Renato Romero
 */

#include <iostream>
using namespace std;

#include "SudokuValue.h"

SudokuValue::SudokuValue(int r, int c, int q, int *row, int *column, int *quadrant) {
  this->indexR = r;
  this->indexC = c;
  this->indexQ = q;

  this->row = row;
  this->col = column;
  this->qud = quadrant;

  this->values = *row;
}

void SudokuValue::checkValues() {
  values &= *row & *col & *qud;
  if (values == 0)
    throw std::invalid_argument("unable to resolve");
}

bool SudokuValue::hasOnlyOnePossibility() {
  return values > 0 && (values & (values -1)) == 0;
}

void SudokuValue::optimize(int reduce) {
  values -= values & reduce;
}

int SudokuValue::resolve() {
  if (!hasOnlyOnePossibility())
    throw std::invalid_argument("unable to resolve");

  int resolve = getNextPossibility();

  *row ^= resolve;
  *col ^= resolve;
  *qud ^= resolve;

  return resolve;
}

SudokuValue::~SudokuValue() {
}
