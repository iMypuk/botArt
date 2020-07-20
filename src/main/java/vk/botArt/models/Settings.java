package vk.botArt.models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

public class Settings {

	public static String login = "";
	
	public static String password = "";
	
	public static String groupUrl = "";
	
	public static Integer friendsCountFrom = 0;

	public static Integer followersCountFrom = 0;

	public static Integer delayFrom = 0;

	public static Integer delayTo = 0;
	
	public Settings(String lgn, String pass, String gUrl,int friendsFrom, int followersFrom, int dFrom, int dTo) {
	    this.login = lgn;
	    this.password = pass;
	    this.groupUrl = gUrl;
	    this.friendsCountFrom = friendsFrom;
	    this.followersCountFrom = followersFrom;
	    this.delayFrom = dFrom;
	    this.delayTo = dTo;
	}
	
	public static Settings loadSettings() {
		
		FileInputStream fis;
        Properties prop = new Properties();
        

        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            prop.load(fis);
            
            login = prop.getProperty("login");
            password = prop.getProperty("password");
            groupUrl = prop.getProperty("groupUrl");
            friendsCountFrom = Integer.parseInt( prop.getProperty("friendsCountFrom") );
            followersCountFrom = Integer.parseInt( prop.getProperty("followersCountFrom"));
            delayFrom = Integer.parseInt( prop.getProperty("delayFrom"));
            delayTo = Integer.parseInt( prop.getProperty("delayTo"));

        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсуствует!");
        }
        
        return new Settings(
        		login,
        		password,
        		groupUrl,
        		friendsCountFrom,
        		followersCountFrom,
        		delayFrom,
        		delayTo
        		);
		
	}
	
	public static Settings saveSettings(Settings settings) {
		
		Properties prop = new Properties();
	    OutputStream output = null;
	    
	    try {

	        output = new FileOutputStream("src/main/resources/config.properties");

	       //set the properties value
	        prop.setProperty("login", login);
	        prop.setProperty("password", password);
	        prop.setProperty("groupUrl", groupUrl);
	        prop.setProperty("friendsCountFrom", friendsCountFrom.toString());
	        prop.setProperty("followersCountFrom", followersCountFrom.toString());
	        prop.setProperty("delayFrom", delayFrom.toString());
	        prop.setProperty("delayTo", delayTo.toString());

	       //save properties to project root folder
	        prop.store(output, null);
	        
	        System.out.println("Настройки сохранены");

	    } catch (IOException io) {
	        io.printStackTrace();
	    } finally {
	        if (output != null) {
	            try {
	                output.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	    }
	    
	    return loadSettings();
	}
}
