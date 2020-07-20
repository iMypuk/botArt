package vk.botArt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vk.botArt.models.AccountInfo;

public class DatabaseWorker {
	
    private static final Logger log = LoggerFactory.getLogger(VkbotController.class);
	
	private static int id;
	private static String account_name;
	private static int added_friends;
	private static int total_friends;
	private static SQLData dbDate;

	private static DatabaseWorker instance;
	
	private static Connection conn;
	
private DatabaseWorker(){}

	static String db = "";
	static String table = "";
	
	public static DatabaseWorker getInstance(){ // #3
	    if(instance == null){		//если объект еще не создан
	        instance = new DatabaseWorker();	//создать новый объект
	    }
	    return instance;		// вернуть ранее созданный объект
	}

	public static void selectAll() {
	 
		 try (Connection connection = getConnection()){
             
             System.out.println("Connection to Store DB succesfull!");
             
             Statement statement = connection.createStatement();
             
             String sql = "SELECT * FROM "+db+"."+table;  
             
             ResultSet result = statement.executeQuery(sql);
             
             System.out.println("Результат получен:");
             
             result.first();
             
            // System.out.println(result.getString(5));
             
          }
      
      catch(Exception ex){
          System.out.println("Connection failed...");        
          System.out.println(ex);
      }
		 
	}
	
	public static void insertInfo(AccountInfo acc) {
		 
		 try (Connection connection = getConnection()){
             
             log.debug("Формируем запрос");
             
             Statement statement = connection.createStatement();
             
             String sql = "INSERT "+table+"(account_name, added_friends, total_friends, date) "
             		+ "VALUES ('"+acc.account_name+"',"
            		 +acc.added_friends+","
            		 +acc.total_friends+",'"
            		 +acc.date+"')";
             
             int rows = statement.executeUpdate(sql);
             
             log.debug("Added "+rows+" rows");
             
          }
      
      catch(Exception ex){
    	  log.debug("Ошибка при добавлении данных");        
    	  System.out.println(ex);
      }
		 
	}
	 
	public static Connection getConnection() throws SQLException, IOException{
	         
	        Properties props = new Properties();
	        
	        try(InputStream in = Files.newInputStream(Paths.get("src/main/resources/database.properties"))){
	            props.load(in);
	        }
	        String url = props.getProperty("url");
	        String username = props.getProperty("username");
	        String password = props.getProperty("password");
	        db =  props.getProperty("db");
	        table = props.getProperty("table");
	         
	        return DriverManager.getConnection(url, username, password);
	    }
	
	public static int loadFriendsCount(String name) {
		
		LocalDate date = LocalDate.now();
		
		 try (Connection connection = getConnection()){
             
			 log.debug("Connection to Store DB succesfull!");
             
             Statement statement = connection.createStatement();
             
             String sql = "SELECT * FROM "+db+"."+table+ " WHERE date = '" +date+ "' AND account_name='"+name+"'";  
             
             log.debug("ЗАПРОС:" + sql);
             
             ResultSet result = statement.executeQuery(sql);
             
             log.debug("ОТВЕТ:" + result);
             
             log.debug("test:" + result.first());
             
             if ( result.first() ) {
            	 
                 id = result.getInt(1);
                 account_name = result.getString(2);
                 added_friends = result.getInt(3);
                 total_friends = result.getInt(4);
            	 
			}
             
             else {
             
            	 added_friends = 0;
            	 
            	 insertInfo(new AccountInfo(
            			 name,
            			 added_friends,
            			 0,
            			 date
            			 ));   
             }
       
             log.debug("Добавлено за сегодня: "+added_friends);
             
          }
      
      catch(Exception ex){
    	  log.debug("Connection failed...");        
          System.out.println(ex);
      }
		 
		 return added_friends;
	}
	
	public static void saveFriendsCount(String name, int count) {
		
		LocalDate date = LocalDate.now();
		
		 try (Connection connection = getConnection()){
             
             log.debug("Формируем запрос");
             
             Statement statement = connection.createStatement();
             
             String sql = "UPDATE " +table+ 
             		" SET added_friends = " + count + 
             		" WHERE account_name='"+name+"' AND"
             		+ " date = '"+date+"';" ;
             
             int rows = statement.executeUpdate(sql);
             
             log.debug("Поле обновлено");
             
          }
      
      catch(Exception ex){
    	  log.debug("Ошибка при добавлении данных");        
    	  System.out.println(ex);
      }
	
	}

	public static void saveFriendsCount(String name, int added, int total) {
		
		LocalDate date = LocalDate.now();
		
		 try (Connection connection = getConnection()){
             
             log.debug("Формируем запрос");
             
             Statement statement = connection.createStatement();
             
             String sql = "UPDATE " +table+ 
             		" SET added_friends = " + added + 
             		", total_friends = " + total +   		 
             		" WHERE account_name='"+name+"' AND"
             		+ " date = '"+date+"';" ;
             
             int rows = statement.executeUpdate(sql);
             
             log.debug("Поле обновлено");
             
          }
      
      catch(Exception ex){
    	  log.debug("Ошибка при добавлении данных");        
    	  System.out.println(ex);
      }
	
	}
	
}
