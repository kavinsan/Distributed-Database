import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/*
 * This class connects to two databases and contains the functions
 * @addName and @delName, specific to creating a "Name" table
 */
public class Platform extends Thread implements Runnable{
	
	private static Database database;
	private static Database database2;
	
	private static int NUM_OF_THREADS = 11;
	static Semaphore semaphore = new Semaphore(1);
	public static int id_counter = 0;
	int id;
	String name;
	int opt;
	
	public Platform(String name, int opt) {
		super();
		id = getNextId();
		this.name = name;
		this.opt = opt;
		
		if (opt == 0) {
			System.out.printf("Thread [%d] Adding Name: %s\n",id_counter,name);
		} else System.out.printf("Thread [%d] Deleting Name: %s\n",id_counter,name);
		
		
	}
	
	synchronized static int getNextId() {
	
		return id_counter++;
	}

	public static void main(String[] args) throws FileNotFoundException {
		//Platform platform = new Platform();
		String file = "perm.txt";

		ArrayList<String> logininfo;
		String[] nameList = {"Jo","Kelvin","Kav","Jay","Kelvin","Kelvin","Nik","Nik","Roy","Steph","Tommy"};
		int[] nameOp = {0,0,0,0,0,1,1,0,0,0,0};//0 = Add Name, 1 = Delete Name
		
		try {
			logininfo = login(file);
		
		database = new Database(logininfo.get(0),logininfo.get(1),logininfo.get(2),logininfo.get(3));
		database2 = new Database(logininfo.get(4),logininfo.get(5),logininfo.get(6),logininfo.get(7));
		
		
		
		if(database.connection() & database2.connection()) {
			//userInterface(database, database2);
			
			database.create("Name(FirstName  VARCHAR(12) NOT NULL)");
			database2.create("Name(FirstName  VARCHAR(12) NOT NULL)");
			
			try {
				Thread[] threadList = new Thread[NUM_OF_THREADS];
				
	            for (int i = 0; i < NUM_OF_THREADS; i++)
	            {
	                threadList[i] = new Platform(nameList[i], nameOp[i]);
	                threadList[i].run();
	            }

	            // wait for all threads to end
	            for (int i = 0; i < NUM_OF_THREADS; i++)
	            {
	            	threadList[i].join();
	            }
				
			} catch (Exception e) {
				System.out.println("Error with Thread\n");
			}
		}
			else System.out.println("\nBoth databases must be connected to access user interface");
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		database.drop("Name");
		database2.drop("Name");

	}
	public void run() {
		try {
			 semaphore.acquire(); // providing mutual exclusion
			 if (opt == 0) {
				 addName(name,database,database2);
			 } else {
				 delName(name,database,database2);
			 }
			 
			 semaphore.release();
			 
		} catch (Exception e) {
			System.out.printf("Error with Thread[%d]\n", id);
		}
	} 
	public static void addName(String name, Database database, Database database2) {
		
		//If the first database does not contain the name
		if(!database.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
			
			//If the Second database does not contain the name then add to both database's
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				database.populate("Name", "FirstName", name);
				database2.populate("Name", "FirstName", name);
			} else { //If the Second database contains the name and the first doesnt then add to the first only
				System.out.printf("The name %s already exists in %s database\n", name, database2.databaseName);
				database.populate("Name", "FirstName", name);
			}
		} else { //If the Second database does not contain the name
			System.out.printf("The name %s already exists in %s database\n", name, database.databaseName);
			
			//If the first database contains the name and the second doesnt then add to the first only
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				database2.populate("Name", "FirstName", name);
			} else { //If the first and second database contains the name do not add anything
				System.out.printf("The name %s already exists in %s database\n", name, database2.databaseName);
			}
		}
		
		database.printTable("Name");
		database2.printTable("Name");
	}
	public static void delName(String name, Database database, Database database2) {
		
		//If the first database does not contain the name, there is nothing to delete 
		if(!database.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
			
			//If the second database does not contain the name, there is nothing to delete
			System.out.printf("The name %s to remove does not exists in %s database\n", name, database.databaseName);
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				System.out.printf("The name %s to remove does not exists in %s database\n", name, database2.databaseName);

			} else { //If the second database contains the name then delete it
				database2.remove("Name", "FirstName", name);
			}
		} else { //If the first database contains the name
			
			//If the second database does not contain the name then delete the name in the first
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				System.out.printf("The name %s to remove does not exists in %s database\n", name, database2.databaseName);
				database.remove("Name", "FirstName", name);

			} else { //If both databases contain the name then delete in both
				database.remove("Name", "FirstName", name);
				database2.remove("Name", "FirstName", name);
			}
			
		}
		
		database.printTable("Name");
		database2.printTable("Name");
	}
	
	private static String getName(String option){
		System.out.println("Please enter a name to " + option + "\n");
		System.out.print(">> ");
		Scanner scanner = new Scanner(System.in);
		String userInput = scanner.next();
		return userInput;
	}
	private static int userInput(){
		System.out.print(">> ");
		Scanner scanner = new Scanner(System.in);
		int userInput = scanner.nextInt();
		return userInput;
	}
	private static void userInterface(Database database, Database database2){

		System.out.println("\nChoose the following Options:\n\t[0]Terminate and Credits\n\t[1]Add a Name\n\t[2]Remove a Name\n\t[3]Drop Table\n\t[4]Create Table\n\t[5]Print All Tables");
		
		int userInput = userInput();
		String name = "";
		
		while (userInput != 0) {
			switch (userInput) {
				case 1: 
					name = getName("add");
					addName(name,database, database2);
					break;
				case 2: 
					name = getName("remove");
					delName(name,database, database2);
					break;
				case 3: 
					database.drop("Name");
					database2.drop("Name");
					break;		
				case 4: 
					database.create("Name(FirstName  VARCHAR(12) NOT NULL)");
					database2.create("Name(FirstName  VARCHAR(12) NOT NULL)");
					break;
				case 5: 
					database.printTable("Name");
					database2.printTable("Name");
					break;
			}
			userInput = userInput();
		}
		if(userInput == 0){
			database.close();
			database2.close();
			System.out.println("\nSystem has been Terminated");
			System.out.println("\tJoanna Makary - 500623846");
			System.out.println("\tKavinsan Thavanesan - 500642698");
			System.out.println("\tSaumil Patel - 500641300");
			
		}
	}
	
	private static ArrayList<String> login(String file) throws FileNotFoundException {
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String line;
		ArrayList<String> login = new ArrayList<String>(8);
		
		int count = 0;
		
		try {
			while((line = br.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				String[] keys = line.split("=");
				String key = keys[0];
				String value = keys[1];
				
				if (keys[0].equals("dbname")){
					
					login.add(value);
				} if (keys[0].equals("host")) {
					
					login.add(value);
				} if (keys[0].equals("user")) {
					
					login.add(value);
				} if (keys[0].equals("pass")) {
					
					login.add(value);
				} 
				count++;
				if(count >= 8) {
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return login;
		
	}
}
