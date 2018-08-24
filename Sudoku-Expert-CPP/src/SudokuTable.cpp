/*
 * SudokuTable.cpp
 *
 *  Created on: 17 de ago de 2018
 *      Author: Renato Romero
 */

#include <iostream>
using namespace std;

#include "SudokuTable.h"
#include "SudokuValue.h"
#include "Sudoku.h"

SudokuTable::SudokuTable(int **sudokuTable) {
  this->table = sudokuTable;
  initValues();
}

void SudokuTable::initValues() {
  toResolve = NULL;
  SudokuValue *sudokuValue;
  for (int *value, r, c, ii, jj, i, j, q = 0; q < QUADRANTS; q++) {
    quadrant[q] = ALLVALUES;
    quadrantToResolve[q] = NULL;

    ii = (q / 3) * 3;
    jj = (q % 3) * 3;

    for (i = 0; i < 3; i++) {
     for (j = 0; j < 3; j++) {
        r = i + ii;
        c = j + jj;
        if(c == 0) {
          row[r] = ALLVALUES;
          rowToResolve[r] = NULL;
        }
        if(r == 0) {
          column[c] = ALLVALUES;
          columnToResolve[c] = NULL;
        }

        value = &table[r][c];
        if(*value != 0) {
          row[r]      -= *value;
          column[c]   -= *value;
          quadrant[q] -= *value;
          tableToResolve[r][c] = NULL;
        } else {
          sudokuValue = new SudokuValue(r, c, q, &row[r], &column[c], &quadrant[q]);
          tableToResolve[r][c] = sudokuValue;

          toResolve            = addLinkedValue(toResolve, sudokuValue);
          rowToResolve[r]      = addLinkedValue(rowToResolve[r], sudokuValue);
          columnToResolve[c]   = addLinkedValue(columnToResolve[c], sudokuValue);
          quadrantToResolve[q] = addLinkedValue(quadrantToResolve[q], sudokuValue);
        }
      }
    }
  }
  for (LinkedValue *check = toResolve; check != NULL; check = check->next)
    check->curr->checkValues();

  optmizeByQuadrant();
}

/**
 * Checks if a missing value of a row or column can only be found in a specific quadrant and
 * remove this possibility from the other quadrants.
 */
void SudokuTable::optmizeByQuadrant() {
  int byRow[3], byColumn [3];
  for (int uniqueValues, duplicateValues, ii, jj, i, j, q = 0; q < QUADRANTS; q++) {
    if (quadrantToResolve[q] == NULL)
      continue;

    uniqueValues = 0;
    duplicateValues = 0;

    ii = (q / 3) * 3;
    jj = (q % 3) * 3;

    // by row
    for (i = 0; i < 3; i++) {
      byRow[i] = 0;
      for (j = 0; j < 3; j++)
        if (tableToResolve[i + ii][j + jj] != NULL)
          byRow[i] |= tableToResolve[i + ii][j + jj]->getNextPossibility();

      duplicateValues |= uniqueValues & byRow[i];
      uniqueValues ^= byRow[i];
    }
    uniqueValues &= (ALLVALUES - duplicateValues);

    if (uniqueValues != 0) {
      for (i = 0; i < 3; i++) {
        byRow[i] &= uniqueValues;
        if (byRow[i] != 0) {
          for (j = 0; j < COLUMNS; j++) {
            if (j < jj || j >= jj + 3) {
              SudokuValue * optimize = tableToResolve[i + ii][j];
              if (optimize != NULL)
                optimize->optimize(byRow[i]);
            }
          }
        }
      }
      uniqueValues = 0;
    }

    // by column
    duplicateValues = 0;
    for (i = 0; i < 3; i++) {
      byColumn[i] = 0;
      for (j = 0; j < 3; j++)
        if (tableToResolve[j + ii][i + jj] != NULL)
          byColumn[i] |= tableToResolve[j + ii][i + jj]->getNextPossibility();

      duplicateValues |= uniqueValues & byColumn[i];
      uniqueValues ^= byColumn[i];
    }
    uniqueValues &= (ALLVALUES - duplicateValues);

    if (uniqueValues != 0) {
      for (i = 0; i < 3; i++) {
        byColumn[i] &= uniqueValues;
        if (byColumn[i] != 0) {
          for (j = 0; j < ROWS; j++) {
            if (j < ii || j >= ii + 3) {
              SudokuValue * optimize = tableToResolve[j][i + jj];
              if (optimize != NULL)
                optimize->optimize(byColumn[i]);
            }
          }
        }
      }
    }
  }
}

LinkedValue * SudokuTable::checkUniqueRow() {
  return this->checkUnique(rowToResolve);
}
LinkedValue * SudokuTable::checkUniqueColumn() {
  return this->checkUnique(columnToResolve);
}
LinkedValue * SudokuTable::checkUniqueQuadrant() {
  return this->checkUnique(quadrantToResolve);
}

LinkedValue * SudokuTable::checkUnique(LinkedValue * check[]) {
  LinkedValue * result = NULL, * source;
  for(int resultValues, uniqueValues, duplicateValues, i = 0; i < 9; i++) {
    if(check[i] == NULL)
      continue;

    uniqueValues = 0;
    duplicateValues = 0;

    // check for unique values
    for(source = check[i]; source != NULL; source = source->next) {
      duplicateValues |= uniqueValues & source->curr->getNextPossibility();
      uniqueValues ^= source->curr->getNextPossibility();
    }
    uniqueValues &= (ALLVALUES - duplicateValues);

    // use the unique values
    if (uniqueValues != 0)
      for(source = check[i]; source != NULL; source = source->next) {
        resultValues = source->curr->getNextPossibility() & uniqueValues;
        if (resultValues > 0 && (resultValues & (resultValues-1)) == 0) {
          source->curr->setNextPossiblity(resultValues);
          result = addLinkedValue(result, source->curr);
        }
      };
  }

  return result;
}

/**
 * Methods to manage the linked lists
 */
LinkedValue * SudokuTable::addLinkedValue(LinkedValue *linkedValue, SudokuValue *sudokuValue) {
  LinkedValue * next = createLinkedValue(sudokuValue);
  if(linkedValue != NULL) {
    LinkedValue *check = linkedValue;
    while(check->next != NULL)
      check = check->next;
    check->next = next;
    return linkedValue;
  }
  return next;
}

LinkedValue * SudokuTable::createLinkedValue(SudokuValue *sudokuValue) {
  LinkedValue * lv = new LinkedValue;
  lv->curr = sudokuValue;
  lv->next = NULL;
  return lv;
}

LinkedValue * SudokuTable::removeLinkedValue(LinkedValue *linkedValue, SudokuValue *sudokuValue) {
  if(linkedValue != NULL) {
    LinkedValue * previous = NULL, * check = linkedValue;
    while(check->next != NULL) {
      if(check->curr == sudokuValue)
        break;
      previous = check;
      check = check->next;
    }
    if(check->curr == sudokuValue) {
      if(previous != NULL)
        previous->next = check->next;
      else
        linkedValue = check->next;
      delete check;
    }
  }
  return linkedValue;
}

/**
 * Resolve one spot
 */
void SudokuTable::resolve(SudokuValue *sudokuValue) {
  int resolve = sudokuValue->resolve();
  int r = sudokuValue->getR();
  int c = sudokuValue->getC();
  int q = sudokuValue->getQ();

  table[r][c] = resolve;
  tableToResolve[r][c] = NULL;

  // remove the link
  toResolve = removeLinkedValue(toResolve, sudokuValue);
  rowToResolve[r] = removeLinkedValue(rowToResolve[r], sudokuValue);
  columnToResolve[c] = removeLinkedValue(columnToResolve[c], sudokuValue);
  quadrantToResolve[q] = removeLinkedValue(quadrantToResolve[q], sudokuValue);

  LinkedValue * check;
  for(check = rowToResolve[r]; check != NULL; check = check->next)
    check->curr->checkValues();

  for(check = columnToResolve[c]; check != NULL; check = check->next)
    check->curr->checkValues();

  for(check = quadrantToResolve[q]; check != NULL; check = check->next)
    check->curr->checkValues();

  delete sudokuValue;
}

/**
 * Guess the result. Function used by the expert difficulty.
 */
void SudokuTable::guess() {
  SudokuValue * guessValue = getGuessValue();
  if(guessValue != NULL) {
    // new guess
    for(int ** guessTable, check, value = 1; value < 10; value++) {
      check = convertDec2Bin(value);
      if((check & guessValue->getNextPossibility()) == 0)
        continue;

      guessTable = copyTable(table);
      guessTable[guessValue->getR()][guessValue->getC()] = check;
      try {
        resolveTable(guessTable);
        // successful guess!!!
        return;
      } catch (std::invalid_argument &exp) {
        // bad guess
      }
    }
    throw std::invalid_argument("unable to guess");
  }
}

/**
 * Check the list of values to resolve and find the easier one to guess.
 */
SudokuValue * SudokuTable::getGuessValue() {
  SudokuValue * result = NULL;
  int currValues2Guess, resultValues2Guess;
  int currSpace2Guess, resultSpace2Guess = 0;
  for(LinkedValue * check = toResolve; check != NULL; check = check->next) {
    if(result == NULL) {
      result = check->curr;
      resultValues2Guess = bitCount(result->getNextPossibility());
    } else {
      currValues2Guess = bitCount(check->curr->getNextPossibility());
      if(currValues2Guess != resultValues2Guess) {
        if(currValues2Guess < resultValues2Guess) {
          result = check->curr;
          resultValues2Guess = currValues2Guess;
        }
      } else {
        currSpace2Guess  = bitCount(row[check->curr->getR()]);
        currSpace2Guess += bitCount(column[check->curr->getC()]);
        currSpace2Guess += bitCount(quadrant[check->curr->getQ()]);
        if(resultSpace2Guess == 0) {
          resultSpace2Guess  = bitCount(row[result->getR()]);
          resultSpace2Guess += bitCount(column[result->getC()]);
          resultSpace2Guess += bitCount(quadrant[result->getQ()]);
        }
        if(currSpace2Guess < resultSpace2Guess) {
          result = check->curr;
          resultSpace2Guess = currSpace2Guess;
          resultValues2Guess = currValues2Guess;
        }
      }
    }
  }
  return result;
}

int SudokuTable::bitCount(int value) {
  int result = 0;
  for (; value > 0; value >>= 1)
    result += (value & 1);
  return result;
}

void SudokuTable::printResult() {
  for(int j, i = 0; i < 9; i++) {
    std::cout << std::endl;
    for(j = 0; j < 9; j++) {
      std::cout << convertBin2Dec(table[i][j]) << ", ";
    }
  }
  std::cout << std::endl;
}

SudokuTable::~SudokuTable() {
  LinkedValue * check, * next;
  int i;
  for(i = 0; i < ROWS; i++)
    for(check = rowToResolve[i]; check != NULL; check = next) {
      next = check->next;
      delete check;
    }

  for(i = 0; i < COLUMNS; i++)
    for(check = columnToResolve[i]; check != NULL; check = next) {
      next = check->next;
      delete check;
    }

  for(i = 0; i < QUADRANTS; i++)
    for(check = quadrantToResolve[i]; check != NULL; check = next) {
      next = check->next;
      delete check;
    }

  for(check = toResolve; check != NULL; check = next) {
    next = check->next;
    delete check->curr;
    delete check;
  }

  for(i = 0; i < ROWS; i++)
    delete table[i];
  delete table;
}
