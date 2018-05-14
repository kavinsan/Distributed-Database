package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Controller {

	private Database database;
	private Database database2;
	public String cursor = ">>";
	public int conToggle = 0; //If connection toggle is 1 then user can create or drop the table
	public int opToggle = 0;//If operation toggle is 1 then user can add or delete from table
	
	@FXML
	TextArea console;
	
	@FXML
	Button clear, add, del, create, drop, disconnect;
	
	@FXML
	TextField name1, host1, user1, pass1, name2, host2, user2, pass2, addField, delField;
	
	@FXML
	Label connection1, connection2;
	
	@FXML
    protected void initialize() throws FileNotFoundException {
		connection1.setText("Not Connected");
		connection1.setTextFill(Color.RED);
		
		connection2.setText("Not Connected");
		connection2.setTextFill(Color.RED);
		
		String file = "perm.txt";

		ArrayList<String> logininfo = login(file);
		host1.setText(logininfo.get(0));
		user1.setText(logininfo.get(1));
		pass1.setText(logininfo.get(2));
		name1.setText(logininfo.get(3));

		host2.setText(logininfo.get(4));
		user2.setText(logininfo.get(5));
		pass2.setText(logininfo.get(6));
		name2.setText(logininfo.get(7));

		
    }

	@FXML
	public void clearAction() {
		
		System.out.println("Text fields cleared");
		name1.setText("");
		host1.setText("");
		user1.setText("");
		pass1.setText("");
		
		name2.setText("");
		host2.setText("");
		user2.setText("");
		pass2.setText("");
	}
	@FXML
	public void connect() {
		if (conToggle == 2) {
			console.appendText(cursor + " Connection to both database already exist!\n");
			return;
		}
		this.database = new Database(host1.getText(), user1.getText(), pass1.getText(), name1.getText());
		this.database2 = new Database(host2.getText(), user2.getText(), pass2.getText(), name2.getText());
		
		if(database.connection()) {
			console.appendText(String.format(cursor + " Connection to %s\'s database established!\n", database.databaseName));
			connection1.setText("Connected");
			connection1.setTextFill(Color.GREEN);
			conToggle++;
			if(database2.connection()) {
				console.appendText(String.format(cursor + " Connection to %s\'s database established!\n", database2.databaseName));
				connection2.setText("Connected");
				connection2.setTextFill(Color.GREEN);
				conToggle++;
			} else {
				console.appendText(String.format(cursor + " Connection to %s\'s database has failed!\n", database2.databaseName));
				conToggle = 3; //Only first db connected
			}
			
		} else {
			console.appendText(String.format(cursor + " Connection to %s\'s database has failed!\n", database.databaseName));
			if(database2.connection()) {
				console.appendText(String.format(cursor + " Connection to %s\'s database established!\n", database2.databaseName));
				connection2.setText("Connected");
				connection2.setTextFill(Color.GREEN);
				conToggle = 4; //Only second db connected
			} else console.appendText(String.format(cursor + " Connection to %s\'s database has failed!\n", database2.databaseName));
		}
	}
	@FXML
	public void disconnect() {
		if (conToggle == 2) {
			database.close();
			console.appendText(String.format(cursor + " Connection to %s\'s database closed!\n", database.databaseName));
			connection1.setText("Not Connected");
			connection1.setTextFill(Color.RED);
			
			database2.close();
			console.appendText(String.format(cursor + " Connection to %s\'s database closed!\n", database2.databaseName));
			connection2.setText("Not Connected");
			connection2.setTextFill(Color.RED);
			conToggle = 0;
		} else if (conToggle == 3){//Drop first cause thats the only one that connected
			database.close();
			console.appendText(String.format(cursor + " %s has failed to connected, closing %s\'s database!\n", database2.databaseName, database.databaseName));
			connection1.setText("Not Connected");
			connection1.setTextFill(Color.RED);
			conToggle = 0;
		} else if (conToggle == 4){//Drop second cause that the only one that connected
			database2.close();
			console.appendText(String.format(cursor + " %s has failed to connected, closing %s\'s database!\n", database.databaseName, database2.databaseName));
			connection2.setText("Not Connected");
			connection2.setTextFill(Color.RED);
			conToggle = 0;			
		} else {
			console.appendText(cursor + " Can not disconnect databases that are not connected\n");
		}
	}
	@FXML
	public void createTable() {
		if(conToggle != 2) {
			console.appendText(cursor + "Can only add a table when a connection exists\n");
			return;
		}
		
		
		if(database.create("Name(FirstName  VARCHAR(12) NOT NULL)")) {
			console.appendText(String.format(cursor + " Table %s created in %s\n", "Name", database.databaseName));
			if(database2.create("Name(FirstName  VARCHAR(12) NOT NULL)")) {
				console.appendText(String.format(cursor + " Table %s created in %s\n", "Name", database2.databaseName));
				opToggle = 1;
			} else {
				console.appendText(String.format(cursor + " The table you are trying to create already exist in %s\n", database2.databaseName));
			}
		} else {
			console.appendText(String.format(cursor + " The table you are trying to create already exist in %s\n", database.databaseName));
			if(database2.create("Name(FirstName  VARCHAR(12) NOT NULL)")) {
				console.appendText(String.format(cursor + " Table %s created in %s\n", "Name", database2.databaseName));
			} else {
				console.appendText(String.format(cursor + " The table you are trying to create already exist in %s\n", database2.databaseName));
			}
		}

	}
	@FXML
	public void dropTable() {
		if(conToggle != 2) {
			console.appendText(cursor + "Can only drop a table when a connection exists\n");
			return;
		}
		
		if(database.drop("Name")) {
			console.appendText(String.format(cursor + " Table %s dropped from %s\n", "Name", database.databaseName));
			if(database2.drop("Name")) {
				console.appendText(String.format(cursor + " Table %s dropped from %s\n", "Name", database2.databaseName));
				opToggle = 0;
			} else {
				console.appendText(String.format(cursor + " The table you are trying to drop does not exist in %s\n", database2.databaseName));
			}
		} else {
			console.appendText(String.format(cursor + " The table you are trying to drop does not exist in %s\n", database.databaseName));
			if(database2.drop("Name")) {
				console.appendText(String.format(cursor + " Table %s dropped from %s\n", "Name", database2.databaseName));
			} else {
				console.appendText(String.format(cursor + " The table you are trying to drop does not exist in %s\n", database2.databaseName));
			}
		}
		
	}
	
	@FXML
	public void addName() {
		if((conToggle != 2) || (opToggle == 0)) {
			console.appendText(cursor + "Can only add a name when a connection exists or table is created\n");
			return;
		}
		String name = addField.getText();
		//If the first database does not contain the name
		if(!database.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
			
			//If the Second database does not contain the name then add to both database's
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				console.appendText(cursor + " " + database.populate("Name", "FirstName", name));
				console.appendText(cursor + " " + database2.populate("Name", "FirstName", name));
			} else { //If the Second database contains the name and the first doesnt then add to the first only
				
				console.appendText(String.format(cursor + " " + "The name %s already exists in %s database\n", name, database2.databaseName));
				console.appendText(cursor + " " + database.populate("Name", "FirstName", name));
			}
		} else { //If the Second database does not contain the name
			
			console.appendText(String.format(cursor + " " + "The name %s already exists in %s database\n", name, database.databaseName));
			//If the first database contains the name and the second doesnt then add to the first only
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				console.appendText(cursor + " " + database2.populate("Name", "FirstName", name));
			} else { //If the first and second database contains the name do not add anything

				console.appendText(String.format(cursor + " " + "The name %s already exists in %s database\n", name, database2.databaseName));
			}
		}
		
	}
	public void delName() {
		if((conToggle != 2) || (opToggle == 0)) {
			console.appendText(cursor + "Can only delete a name when a connection exists or table is created\n");
			return;
		}
		
		String name = delField.getText();
		//If the first database does not contain the name, there is nothing to delete 
		if(!database.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
			
			//If the second database does not contain the name, there is nothing to delete
			console.appendText(String.format(cursor + " " + "The name %s to remove does not exists in %s database\n", name, database.databaseName));
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				console.appendText(String.format(cursor + " " + "The name %s to remove does not exists in %s database\n", name, database2.databaseName));

			} else { //If the second database contains the name then delete it
				console.appendText(cursor + " " + database2.remove("Name", "FirstName", name));
			}
		} else { //If the first database contains the name
			
			//If the second database does not contain the name then delete the name in the first
			if(!database2.check("SELECT FirstName FROM Name WHERE FirstName = '" + name + "'", name)) {
				console.appendText(String.format(cursor + " " + "The name %s to remove does not exists in %s database\n", name, database2.databaseName));
				console.appendText(cursor + " " + database.remove("Name", "FirstName", name));

			} else { //If both databases contain the name then delete in both
				console.appendText(cursor + " " + database.remove("Name", "FirstName", name));
				console.appendText(cursor + " " + database2.remove("Name", "FirstName", name));
			}
			
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
