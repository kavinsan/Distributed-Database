This program uses either Oracle 11c, 12c, Oracle XE or a virtual machine containing Oracle 11c.

NOTE: For ease of access use SQL Developer to connect to the databases before running the program

- Edit the perm.txt file and fill and choose which two of the four forms you need for the databases
- OR edit the following lines in the Platform.java file and replace it with the information needed:
	On Line 61-62
	
	database = new Database(logininfo.get(0),logininfo.get(1),logininfo.get(2),logininfo.get(3));
	database2 = new Database(logininfo.get(4),logininfo.get(5),logininfo.get(6),logininfo.get(7));

	replace the four parameters with:
		1. Host Info (in java syntax)
		2. Username
		3. Password
		4. Server Name (This can be anything)