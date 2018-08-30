package sudoku;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;


/**
 * Expert version of Sudoku.
 * 
 * @author Renato Romero
 */
public class Sudoku {

	public final static int ROWS      = 9;
	public final static int COLUMNS   = 9;
	public final static int QUADRANTS = 9;
	public final static int	ALLVALUES	= 0b111111111;
	
	public static boolean resolved = false;

	public static void main(String... args) {
		long start = System.currentTimeMillis();
		
		int [][] toResolveDec = {
			{ 7, 0, 9,   4, 0, 0,   0, 0, 0 }, 
			{ 0, 5, 0,   2, 3, 8,   9, 0, 0 }, 
			{ 0, 3, 6,   0, 0, 0,   0, 0, 0 },
			
			{ 0, 0, 0,   0, 8, 0,   0, 0, 9 }, 
			{ 9, 0, 5,   0, 0, 0,   8, 0, 4 }, 
			{ 4, 0, 0,   0, 7, 0,   0, 0, 0 },
			
			{ 0, 0, 0,   0, 0, 0,   4, 9, 0 }, 
			{ 0, 0, 3,   1, 5, 9,   0, 7, 0 }, 
			{ 0, 0, 0,   0, 0, 7,   1, 0, 3 }
			};
		
		for(int i = 0; i < 100_000; i++)
			resolveTable(copyTableDec2Bin(toResolveDec));
		
		long finish = System.currentTimeMillis();
		System.out.println((finish - start) + "ms");
	}

	public static int convertDec2Bin(int decValue) {
		if(decValue > 0)
			decValue = 1 << (decValue - 1);
		return decValue;
	}
	
	public static int convertBin2Dec(int binValue) {
		int decValue = 0;
		for(int value = 1; binValue > 0; binValue >>= 1, value++)
			if((binValue & 1) != 0)
				decValue += value;
		return decValue;
	}

	public static int[][] copyTable(int[][] source) {
		int[][] dest = new int[ROWS][];
		for (int i = 0; i < ROWS; i++)
			dest[i] = Arrays.copyOf(source[i], COLUMNS);
		return dest;
	}
	
	public static int[][] copyTableDec2Bin(int[][] source) {
		int[][] dest = new int[ROWS][COLUMNS];
		for (int j, i = 0; i < ROWS; i++)
			for(j = 0; j < COLUMNS; j++)
				dest[i][j] = convertDec2Bin(source[i][j]);
		return dest;
	}	
	
	public static void resolveTable(int[][] init) {

		SudokuTable sudokuTable = new SudokuTable(init);
		boolean [] found = {false};
		do {
			found[0] = false;
			// check 3 functions...
			sudokuTable.toResolve.stream().filter(SudokuTable.SudokuValue::hasOnlyOnePossibility).forEach(sudokuValue -> {
				sudokuValue.resolve();
				found[0] = true;
			});

			// check unique quadrant...
			sudokuTable.checkUnique(sudokuTable.quadrantValues).forEach(sudokuValue -> {
				sudokuValue.resolve();
				found[0] = true;
			});

			// check unique row...
			sudokuTable.checkUnique(sudokuTable.rowValues).forEach(sudokuValue -> {
				sudokuValue.resolve();
				found[0] = true;
			});

			// check unique column...
			sudokuTable.checkUnique(sudokuTable.columnValues).forEach(sudokuValue -> {
				sudokuValue.resolve();
				found[0] = true;
			});

			if(!sudokuTable.isResolved()) {
				if (found[0])
					continue;
				if(sudokuTable.optmizeByQuadrant())
					continue;
				sudokuTable.guess();
			} else {
				if(!resolved) {
					sudokuTable.printResult();
					resolved = true;
				}
			}
			break;
		} while (true);
	}

	public static class SudokuTable {
		int[][]						table;
		SudokuValue[][]		tableToResolve = new SudokuValue[ROWS][COLUMNS];
		List<SudokuValue> toResolve = new CopyOnWriteArrayList<>();

		int[]		row				= new int[ROWS];
		int[]		column		= new int[COLUMNS];
		int[]		quadrant	= new int[QUADRANTS];

		public SudokuTable(int[][] sudokuTable) {
			table = sudokuTable;
			initValues();
		}

		private void initValues() {
			for(int r, c, jj, ii, j, i, q = 0; q < QUADRANTS; q++) {
				this.quadrant[q] = ALLVALUES;
			
				ii = (q / 3)*3;
				jj = (q % 3)*3;
				
				for(i = 0; i < 3; i++) {
					r = i + ii;
					c = jj;
					for(j = 0; j < 3; j++, c++) {
						if(c == 0)
							this.row[r] = ALLVALUES;
						if(r == 0)
							this.column[c] = ALLVALUES;
						
						if(table[r][c] != 0) {
							this.row[r] -= table[r][c];
							this.column[c] -= table[r][c];
							this.quadrant[q] -= table[r][c];
						} else {
							tableToResolve[r][c] = new SudokuValue(r, c);
							toResolve.add(tableToResolve[r][c]);
						}
					}
				}
			}
			toResolve.forEach(SudokuValue::checkValues);
		}

		public boolean isResolved() {
			return toResolve.isEmpty();
		}
		
		public Set<SudokuValue> checkUnique(Function<Integer, Stream<SudokuValue>> source) {

			Set<SudokuValue> result = new HashSet<>();

			int[] uniqueValues = {0};
			int[] duplicateValues = {0};
			for (int item = 0; item < 9; item++) {
				uniqueValues[0] = 0;
				duplicateValues[0] = 0;

				// check for unique values
				source.apply(item).forEach(sudokuValue -> {
					duplicateValues[0] |= uniqueValues[0] & sudokuValue.values;
					uniqueValues[0] ^= sudokuValue.values;
				});
				uniqueValues[0] &= (ALLVALUES - duplicateValues[0]);
				
				// use the unique values
				if (uniqueValues[0] != 0)
					source.apply(item).forEach(sudokuValue -> {
						int resultValues = sudokuValue.values & uniqueValues[0];
						if (resultValues > 0 && (resultValues & (resultValues-1)) == 0) {
							sudokuValue.values = resultValues;
							result.add(sudokuValue);
						}
					});
			}

			return result;
		}

		public boolean optmizeByQuadrant() {
			boolean result = false;
			SudokuValue optimize;
			for (int chgValue, uniqueValues, duplicateValues, ii, jj, i, j, q = 0; q < QUADRANTS; q++) {
				uniqueValues = 0;
				duplicateValues = 0;

				ii = (q / 3) * 3;
				jj = (q % 3) * 3;

				// by row
				int[] byRow = {0, 0, 0};
				for (i = 0; i < 3; i++) {
					for (j = 0; j < 3; j++)
						if (tableToResolve[i + ii][j + jj] != null)
							byRow[i] |= tableToResolve[i + ii][j + jj].values;
					
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
									optimize = tableToResolve[i + ii][j];
									if (optimize != null) {
										chgValue = optimize.values; 
										optimize.values -= optimize.values & byRow[i];
										result |= optimize.values != chgValue; 
									}
								}
							}
						}
					}
					uniqueValues = 0;
				}

				// by column
				duplicateValues = 0;
				int[] byColumn = {0, 0, 0};
				for (i = 0; i < 3; i++) {
					for (j = 0; j < 3; j++)
						if (tableToResolve[j + ii][i + jj] != null)
							byColumn[i] |= tableToResolve[j + ii][i + jj].values;

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
									optimize = tableToResolve[j][i + jj];
									if (optimize != null) {
										chgValue = optimize.values; 
										optimize.values -= optimize.values & byColumn[i];
										result |= optimize.values != chgValue; 
									}
								}
							}
						}
					}
				}
			}
			return result;
		}

		public Function<Integer, Stream<SudokuValue>>	rowValues	= index -> {
			Builder<SudokuValue> builder = Stream.builder();

			for (int j = 0; j < COLUMNS; j++)
				builder.add(tableToResolve[index][j]);

			return builder.build().filter(value -> value != null);
		};

		public Function<Integer, Stream<SudokuValue>>	columnValues = index -> {
			Builder<SudokuValue> builder = Stream.builder();

			for (int j = 0; j < ROWS; j++)
				builder.add(tableToResolve[j][index]);

			return builder.build().filter(value -> value != null);
		};

		public Function<Integer, Stream<SudokuValue>>	quadrantValues = index -> {
			Builder<SudokuValue> builder = Stream.builder();

			int ii = (index / 3) * 3;
			int jj = (index % 3) * 3;

			for (int j, i = 0; i < 3; i++)
				for (j = 0; j < 3; j++)
					builder.add(tableToResolve[i + ii][j + jj]);

			return builder.build().filter(value -> value != null);
		};

		public void guess() {
			toResolve.sort(SudokuValueComparator.singleton);
			SudokuValue guessValue = toResolve.get(0);
			for (int value = 1; value < 10; value++) {
				int check = convertDec2Bin(value);
				if((check & guessValue.values) == 0)
					continue;
				
				int[][] guessTable = copyTable(table);
				
				guessTable[guessValue.iRow][guessValue.iCol] = check;
				try {
					Sudoku.resolveTable(guessTable);
					return;
				} catch (RuntimeException rte) {
					// bad guess
				}
			}
			throw new RuntimeException("unable to guess");
		}
		
		public void printResult() {
			for (int j, i = 0; i < ROWS; i++) {
				System.out.println();
				for(j = 0; j < COLUMNS; j++)
					System.out.print(convertBin2Dec(table[i][j]) + ", ");
			}
		}
		
		public class SudokuValue {
			int	iRow;
			int	iCol;
			int iQud;
			int values;

			public SudokuValue(int iRow, int iCol) {
				this.iRow = iRow;
				this.iCol = iCol;
				this.iQud = ((iRow / 3) * 3) + (iCol/3);
				this.values = ALLVALUES;
			}

			public void checkValues() {
				// intersection of the three sets
				values &= row[iRow] & column[iCol] & quadrant[iQud];
				if (values == 0)
					throw new RuntimeException("unable to resolve");
			}

			public boolean hasOnlyOnePossibility() {
				return values > 0 && (values & (values -1)) == 0;
			}

			public int getNextPossibility() {
				return values;
			}

			public void resolve() {
				if(!hasOnlyOnePossibility())
					throw new RuntimeException("Resolving with more than one value or none");
				
				int value = getNextPossibility();
				table[iRow][iCol] = value;
				tableToResolve[iRow][iCol] = null;
				toResolve.remove(this);

				row[iRow]      -= value;
				column[iCol]   -= value;
				quadrant[iQud] -= value;

				Set<SudokuValue> toUpdate = rowValues.apply(iRow).collect(Collectors.toSet());
				toUpdate.addAll(columnValues.apply(iCol).collect(Collectors.toSet()));
				toUpdate.addAll(quadrantValues.apply(iQud).collect(Collectors.toSet()));
				toUpdate.forEach(sudokuValue -> sudokuValue.checkValues());
			}

			public int guessEven() {
				return Integer.bitCount(row[iRow]) + Integer.bitCount(column[iCol]) + Integer.bitCount(quadrant[iQud]);
			}
		}

		public static class SudokuValueComparator implements Comparator<SudokuValue> {

			public static SudokuValueComparator singleton = new SudokuValueComparator();
			
			@Override
			public int compare(SudokuValue o1, SudokuValue o2) {
				if (o1.iRow == o2.iRow && o1.iCol == o2.iCol)
					return 0;

				int toGuess = Integer.bitCount(o1.values) - Integer.bitCount(o2.values);
				if (toGuess != 0)
					return toGuess;

				return o1.guessEven() - o2.guessEven();
			}
		}
	}
}
