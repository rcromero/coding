package sudoku;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;


/**
 * First version of Sudoku, using Sets, Streams and Functions.
 * 
 * @author Renato Romero
 */
public class Sudoku {
	
	public final static int        ROWS      = 9;
	public final static int        COLUMNS   = 9;
	public final static int        QUADRANTS = 9;
	public final static Integer [] ALLVALUES = {1, 2, 3, 4, 5, 6, 7, 8, 9};
	
	public static boolean resolved = false;

	public static void main(String ... args) {
		
		long start = System.currentTimeMillis();
		
		int[][] toResolve = 
		{
			{5, 3, 0,   2, 0, 0,   9, 0, 0},
			{0, 0, 0,   0, 3, 0,   2, 0, 5},
			{0, 2, 0,   9, 0, 0,   6, 3, 0},
				
			{0, 0, 0,   0, 0, 0,   0, 0, 0},
			{0, 1, 0,   6, 0, 3,   7, 0, 0},
			{4, 0, 0,   0, 5, 0,   0, 0, 0},
				
			{9, 0, 0,   0, 0, 0,   3, 0, 7},
			{0, 8, 0,   0, 6, 0,   0, 9, 0},
			{0, 0, 4,   8, 0, 0,   0, 0, 0}
		};
		
		// benchmark
		for(int i = 0; i < 100_000; i++)
			resolveTable(copyTable(toResolve));

		long finish = System.currentTimeMillis();
		
		System.out.println((finish - start) + "ms");
	}
	
	public static void resolveTable(int[][] init) {
		SudokuTable sudokuTable = new SudokuTable(init); 
		
		boolean found[] = {false};
		do {
			found[0] = false;
			// check 3 functions...
			sudokuTable.toResolve.forEach(sudokuValue -> {
				if(sudokuValue.hasOnlyOnePossibility()) {
					sudokuValue.resolve();
					found[0] = true;
				}
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
			
			if(!found[0] && !sudokuTable.isResolved() && sudokuTable.optmizeByQuadrant())
				continue;
			
		} while(found[0]);
		
		if(!resolved) {
			printTable(sudokuTable);
			resolved = true;
		}
	}

	public static void printTable(SudokuTable sudokuTable) {
		for(int j, i = 0; i < ROWS; i++) {
			System.out.println();
			
			for(j = 0; j < COLUMNS; j++)
				System.out.print(sudokuTable.table[i][j] + ", ");
		}
		System.out.println();
	}
	
	public static int[][] copyTable(int[][] source) {
		int[][] dest = new int[ROWS][];
		for (int i = 0; i < ROWS; i++)
			dest[i] = Arrays.copyOf(source[i], COLUMNS);
		return dest;
	}	
	
	public static class SudokuTable {
		int [][] table;
		
		SudokuValue [][]  tableToResolve = new SudokuValue[ROWS][COLUMNS];
		List<SudokuValue> toResolve = new CopyOnWriteArrayList<>();
		
		@SuppressWarnings("unchecked")
		Set<Integer> [] row = new Set[ROWS];
		
		@SuppressWarnings("unchecked")
		Set<Integer> [] column = new Set[COLUMNS];
		
		@SuppressWarnings("unchecked")
		Set<Integer> [] quadrant = new Set[QUADRANTS];
		
		public SudokuTable(int [][] sudokuTable) {
			table = sudokuTable;
			init();
		}
		
		private void init() {
			for(int r, c, jj, ii, j, i, q = 0; q < QUADRANTS; q++) {
				this.quadrant[q] = new HashSet<>(Arrays.asList(ALLVALUES));
			
				ii = (q / 3)*3;
				jj = (q % 3)*3;
				
				for(i = 0; i < 3; i++) {
					r = i + ii;
					c = jj;
					for(j = 0; j < 3; j++, c++) {
						if(c == 0)
							this.row[r] = new HashSet<>(Arrays.asList(ALLVALUES));
						if(r == 0)
							this.column[c] = new HashSet<>(Arrays.asList(ALLVALUES));
						
						if(table[r][c] != 0) {
							this.row[r].remove(table[r][c]);
							this.column[c].remove(table[r][c]);
							this.quadrant[q].remove(table[r][c]);
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
			for(int item = 0; item < 9; item++) {
				Set<Integer> uniqueValues = new HashSet<>();
				Set<Integer> duplicatedValues = new HashSet<>();
				
				// check for unique and duplicated values
				source.apply(item).forEach(sudokuValue -> {
					for(Iterator<Integer> ivalue = sudokuValue.values.iterator(); ivalue.hasNext(); ) {
						Integer value = ivalue.next();
						if(!duplicatedValues.contains(value)) {
							if(!uniqueValues.contains(value)) {
								uniqueValues.add(value);
							} else {
								uniqueValues.remove(value);
								duplicatedValues.add(value);
							}
						}
					}
				});
						
				// use the unique values
				if(!uniqueValues.isEmpty())
					source.apply(item).forEach(sudokuValue -> {
						Set<Integer> resultValues = new HashSet<>(sudokuValue.values);
						resultValues.retainAll(uniqueValues);
						if(resultValues.size() == 1) {
							sudokuValue.values = resultValues;
							result.add(sudokuValue);
						}
					});
			}
			return result;
		}
		
		
		private boolean optmizeByQuadrant() {
			boolean result = false;
			Integer value;
			Iterator<Integer> ivalue;
			Set<Integer> uniqueValues, duplicatedValues;
			@SuppressWarnings("unchecked")
			Set<Integer> [] byRow = new Set[3], byColumn = new Set[3];

			for(int jj, ii, j, i, q = 0; q < QUADRANTS; q++) {
				
				ii = (q / 3)*3;
				jj = (q % 3)*3;
				
				// by row
				uniqueValues = new HashSet<>();
				duplicatedValues = new HashSet<>();
				for(i = 0; i < 3; i++) {
					byRow[i] = new HashSet<>();
					for(j = 0; j < 3; j++)
						if(tableToResolve[i + ii][j + jj] != null)
							byRow[i].addAll(tableToResolve[i + ii][j + jj].values);

					for(ivalue = byRow[i].iterator(); ivalue.hasNext(); ) {
						value = ivalue.next();
						if(!duplicatedValues.contains(value)) {
							if(!uniqueValues.contains(value)) {
								uniqueValues.add(value);
							} else {
								uniqueValues.remove(value);
								duplicatedValues.add(value);
							}
						}
					}
				}
				
				if(!uniqueValues.isEmpty())
					for(i = 0; i < 3; i++) {
						byRow[i].retainAll(uniqueValues);
						if(!byRow[i].isEmpty())
							for(j = 0; j < COLUMNS; j++)
								if(j < jj || j >= jj + 3)
									if(tableToResolve[i + ii][j] != null)
										if(tableToResolve[i + ii][j].values.removeAll(byRow[i]))
											result = true;
					}
				
				// by column
				uniqueValues = new HashSet<>();
				duplicatedValues = new HashSet<>();
				for(i = 0; i < 3; i++) {
					byColumn[i] = new HashSet<>();
					for(j = 0; j < 3; j++)
						if(tableToResolve[j + ii][i + jj] != null)
							byColumn[i].addAll(tableToResolve[j + ii][i + jj].values);

					for(ivalue = byColumn[i].iterator(); ivalue.hasNext(); ) {
						value = ivalue.next();
						if(!duplicatedValues.contains(value)) {
							if(!uniqueValues.contains(value)) {
								uniqueValues.add(value);
							} else {
								uniqueValues.remove(value);
								duplicatedValues.add(value);
							}
						}
					}
				}
				
				if(!uniqueValues.isEmpty())
					for(i = 0; i < 3; i++) {
						byColumn[i].retainAll(uniqueValues);
						if(!byColumn[i].isEmpty())
							for(j = 0; j < ROWS; j++)
								if(j < ii || j >= ii + 3)
									if(tableToResolve[j][i + jj] != null)
										if(tableToResolve[j][i + jj].values.removeAll(byColumn[i]))
											result = true;
					}
				
			}
			return result;
		}

		private Function<Integer, Stream<SudokuValue>> rowValues = index -> {
			Builder<SudokuValue> builder = Stream.builder();
			
			for(int j = 0; j < COLUMNS; j++)
				builder.add(tableToResolve[index][j]);

			return builder.build().filter(value -> value != null);
		};		

		private Function<Integer, Stream<SudokuValue>> columnValues = index -> {
			Builder<SudokuValue> builder = Stream.builder();
			
			for(int j = 0; j < ROWS; j++)
				builder.add(tableToResolve[j][index]);

			return builder.build().filter(value -> value != null);
		};		
		
		private Function<Integer, Stream<SudokuValue>> quadrantValues = index -> {
			Builder<SudokuValue> builder = Stream.builder();
			
			int ii = (index / 3)*3;
			int jj = (index % 3)*3;
		
			for(int j, i = 0; i < 3; i++)
				for(j = 0; j < 3; j++)
					builder.add(tableToResolve[i + ii][j + jj]);

			return builder.build().filter(value -> value != null);
		};		
		
		public class SudokuValue {
			int iRow;
			int iCol;
			int iQud;
			
			Set<Integer> values;
			
			public SudokuValue(int iRow, int iCol) {
				this.iRow = iRow;
				this.iCol = iCol;
				this.iQud = ((iRow / 3) * 3) + (iCol/3);
				this.values = new HashSet<>(row[iRow]);
			}
			
			public void checkValues() {
				// intersection of the three sets
				values.retainAll(row[iRow]);
				values.retainAll(column[iCol]);
				values.retainAll(quadrant[iQud]);
			}
			
			public boolean hasOnlyOnePossibility() {
				return values.size() == 1;
			}

			public int getNextPossibility() {
				return values.iterator().next();
			}
			
			public void resolve() {
				if(!hasOnlyOnePossibility())
					throw new RuntimeException("Resolving with more than one value or none");
				
				int value = getNextPossibility();

				table[iRow][iCol] = value;
				tableToResolve[iRow][iCol] = null;
				toResolve.remove(this);

				row[iRow].remove(value);
				column[iCol].remove(value);
				quadrant[iQud].remove(value);

				Set<SudokuValue> toUpdate = rowValues.apply(iRow).collect(Collectors.toSet());
				toUpdate.addAll(columnValues.apply(iCol).collect(Collectors.toSet()));
				toUpdate.addAll(quadrantValues.apply(iQud).collect(Collectors.toSet()));
				toUpdate.forEach(sudokuValue -> sudokuValue.checkValues());
			}
		}
	}
}
