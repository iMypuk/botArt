package vk.botArt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWorker {
	
    private static final Logger log = LoggerFactory.getLogger(VkbotController.class);
    
    static final String PATH = "data/";

	public static ArrayList<String> getBlacklistFromFile(Long id) throws IOException{
		
		String path = PATH + id.toString() + ".txt";
		
		ArrayList<String> list = new ArrayList();
		
		list = (ArrayList<String>) Files.readAllLines(Paths.get(path));
		
		log.debug("ЧЕРНЫЙ СПИСОК:");
		log.debug(list.toString());
		
		return list;
	}
	
	public static void saveBlacklistToFile(ArrayList<String> list, Long id) throws IOException{
		
		String path = PATH + id.toString() + ".txt";
		
		Path p = Paths.get(PATH);

		if (Files.exists(p)) {
		  // file exist
		}

		if (Files.notExists(p)) {
		  // file is not exist
	          File file = new File(path);

	          file.createNewFile();
			
		}
		
	       Writer writer = null;

	        try {
	            writer = new FileWriter(path);
	            for (String line : list) {
	                writer.write(line);
//	                тут мог бы быть пробел если надо в одну строку
	                writer.write(System.getProperty("line.separator"));
	            }
	            writer.flush();
	        } catch (Exception e) {
	        	log.info(e.getMessage());
	        } finally {
	            if (writer != null) {
	                try {
	                    writer.close();
	                } catch (IOException ex) {
	                }
	            }
	        }
	        
			log.debug("ЧЕРНЫЙ СПИСОК:");
			log.debug(list.toString());		
	}

    public static void newFile(int a) throws Exception {
    	
        FileWriter nFile = new FileWriter(PATH);

        nFile.write("test");

        nFile.close();
    }
	
    // Если пользователь есть в ЧС, то вернуть true, иначе false
    public static boolean checkFriend(String id, ArrayList blackList) {
    	
    	int index = blackList.indexOf(id);
    	
    	if (index == -1) {
    		log.debug("ID " + id + " не находится в черном списке. INDEX = " + index);
    		return false;
    	}
    	else {
    		log.debug("ID " + id + " находится в черном списке. INDEX = " + index);
    		return true;
    	}
    }
    
    public static void test() {
    	Long id = 329063457L;
    	ArrayList<String> s = new ArrayList<String>();
    	
    	try {
			s = getBlacklistFromFile(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String testId = "1";
    	
    	checkFriend(testId, s);
    	
    	
    	
    }
}
