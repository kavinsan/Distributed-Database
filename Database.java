import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;


public class Database {
	
	private Connection connection = null;
	private Statement stmt = null;
	
	private String host;
	private String username;
	private String password;
	public String databaseName;
	
	
	//Database constructor
	public Database(String host, String username, String password, String databaseName) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.databaseName = databaseName;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	}
	
	//Setter methods for database parameters
	public void setHost(String value) {
		this.host = value;
	}
	public void setUser(String value) {
		this.username = value;
	}
	public void setPass(String value) {
		this.password = value;
	}
	public void setName(String value) {
		this.databaseName = value;
	}
	
	//Connection method
	public boolean connection() {
		try {
			
			connection = DriverManager.getConnection(host, username, password);
			System.out.printf("Connection to %s\'s database established!\n", databaseName);
			
		} catch (SQLException e){
			System.out.printf("Connection To %s\'s database Failed! Check output console\n", databaseName);
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	//Execute any sql statements
	public boolean command(String statement) {
		try {
			stmt = connection.createStatement();
			stmt.execute(statement);
			System.out.println("Statement executed");
			return true;
		} catch (SQLException e) {
			System.out.println("The statement you are trying to execute has failed");
			e.printStackTrace();
			return false;
		}
	}
	//Return false if no result for such query, return true otherwise
	public boolean check(String statement, String name) {
		try {
			stmt = connection.createStatement();
			stmt.execute(statement);
			ResultSet rs = stmt.executeQuery(statement);
			if (!rs.next()){

				return false;
			}
			do {
				String firstname = rs.getString("FirstName");
				if(name.equals(firstname)) {
					return true;
				}
				System.out.println(firstname);	
				
			} while(rs.next());
			
			System.out.println("Statement executed");

		} catch (SQLException e) {
			System.out.println("The statement you are trying to execute has failed");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	//Drop a table
	public boolean drop(String table) {
		
		try {
			stmt = connection.createStatement();
			String dropTable = "DROP TABLE " + table;
			stmt.execute(dropTable);
			System.out.printf("Table %s dropped\n", table);
			return true;
		} catch (SQLException e){
			System.out.println("The table you are trying to drop does not exist");
			return false;
		}
	}
	//Create a table
	public boolean create(String table) {
		/*Ex. Name(FirstName  VARCHAR(12) NOT NULL) */
		
		try {
			stmt = connection.createStatement();
			String dropTable = "CREATE TABLE " + table;
			stmt.execute(dropTable);
			System.out.printf("Table %s created in %s\n", table, this.databaseName);
			return true;
		} catch (SQLException e){
			System.out.println("The table you are trying to create already exist");
			return false;
		}
	}
	//Populate a table in a specific column with a value
	public void populate(String table, String column, String value){
		try {
			String populate = "";
			stmt = connection.createStatement();
			//Populate Client Table
			populate = "INSERT INTO " + table + " (" + column + ") VALUES ('" + value + "')";
			stmt.execute(populate);
			System.out.printf("%s has been added to the table %s in %s\n", value, column, this.databaseName);
			connection.commit();
		
		} catch (SQLException e){
			System.out.println("The tables to populate does not exist or unique data already exist");
	
		}
	}
	//Remove a record from a table in a specific column
	public void remove(String table, String column, String value){
		try {
			String populate = "";
			stmt = connection.createStatement();
			//Populate Client Table
			populate = "DELETE FROM " + table + " WHERE " + column + "='" + value + "'";
			stmt.execute(populate);
			System.out.printf("%s has been removed from the table %s in %s\n", value, column, this.databaseName);
			connection.commit();
		
		} catch (SQLException e){
			System.out.println("The tables to populate does not exist or unique data already exist");
			e.printStackTrace();
		}
	}
	//Print records of a table
	public boolean printTable(String table){
		try {
			stmt = connection.createStatement();
			String[] tables = {table};
			
			for(int j = 0; j <= tables.length - 1; j++){
				String query = "SELECT * FROM " + tables[j];
				ResultSet rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd = rs.getMetaData();
				
				int columnNumber = rsmd.getColumnCount();
				//System.out.printf("There are %d columns\n", columnNumber);
				System.out.printf("Printing %s Table from %s\n", tables[j], this.databaseName);
				while(rs.next()){
					for(int i = 1; i <= columnNumber; i++){
						System.out.print("\t" + rs.getString(i) + " ");
					}
					System.out.println("");
				}
				//System.out.println("");
			}
			return true;
		} catch (SQLException e){
			System.out.println("The table you are trying to print does not exist");
			return false;
		}
	}
	//Print all tables in a database
	public void getTablesList() {
		
        //ArrayList<String> listofTable = new ArrayList<String>();
        try {
            ResultSet rs = null;
            DatabaseMetaData meta = connection.getMetaData();
            rs = meta.getTables(null, null, null, new String[]{"TABLE"});

	        while (rs.next()) {
	            String tableName = rs.getString("TABLE_NAME");
	            System.out.println("tableName=" + tableName);
	        }
	    
        } catch (SQLException e) {
			System.out.println("The statement you are trying to execute has failed");
			
		}
        
    }
	public void close() {
        try {
            connection.close();
            System.out.printf("%s connection has been closed\n", this.databaseName);
	    
        } catch (SQLException e) {
			System.out.println("Connection could not be closed");
			
		}
	}
}
