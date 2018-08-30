This code resolves sudokus available at http://www.sudoku.com/ (easy to expert).
The goal here is to create a code that finds the answer. There were some optimization changes applied, 
but I decided to keep the streams and functional interfaces (JDK 1.8.0.172).

The algorithm was created by me, using my personal knowledge on how I would resolve it.
There must have better solutions out there on how to calculate the right answer - as I have already done some improvements.

Resolved 100.000 within 26s
CPU: Intel i7 2600, VMWare with 2 cores, 2GB, Windows 7 64.  

	7, 0, 9,   4, 0, 0,   0, 0, 0 
	0, 5, 0,   2, 3, 8,   9, 0, 0 
	0, 3, 6,   0, 0, 0,   0, 0, 0
	
	0, 0, 0,   0, 8, 0,   0, 0, 9 
	9, 0, 5,   0, 0, 0,   8, 0, 4 
	4, 0, 0,   0, 7, 0,   0, 0, 0
	
	0, 0, 0,   0, 0, 0,   4, 9, 0 
	0, 0, 3,   1, 5, 9,   0, 7, 0 
	0, 0, 0,   0, 0, 7,   1, 0, 3

This is the final (number version) version of my Sudokus.
New versions shall come in the future, once I get inspired! 