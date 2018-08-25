This code resolves sudokus available at http://www.sudoku.com/ (easy to hard).
The goal here is to create a code that finds the answer. There were some optimization changes applied, 
but I decided to keep the Sets, decimal operations, streams and functional interfaces (JDK 1.8.0.172).

The algorithm was created by me, using my personal knowledge on how I would resolve it.
There must have better solutions out there on how to calculate the right answer - as I have already done some improvements.

Resolved 100.000 within 60.544ms (low as 58.500ms)
CPU: Intel i7 2600, VMWare with 2 cores, 2GB, Windows 7 64.  

	5, 3, 0,   2, 0, 0,   9, 0, 0
	0, 0, 0,   0, 3, 0,   2, 0, 5
	0, 2, 0,   9, 0, 0,   6, 3, 0
		
	0, 0, 0,   0, 0, 0,   0, 0, 0
	0, 1, 0,   6, 0, 3,   7, 0, 0
	4, 0, 0,   0, 5, 0,   0, 0, 0
		
	9, 0, 0,   0, 0, 0,   3, 0, 7
	0, 8, 0,   0, 6, 0,   0, 9, 0
	0, 0, 4,   8, 0, 0,   0, 0, 0
