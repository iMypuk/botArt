package vk.botArt;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import vk.botArt.models.AuthModel;
import vk.botArt.models.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiWallLinksForbiddenException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.friends.FriendStatus;
import com.vk.api.sdk.objects.friends.FriendStatusFriendStatus;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.groups.responses.GetMembersResponse;
import com.vk.api.sdk.objects.users.LastSeen;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.objects.wall.*;
import com.vk.api.sdk.objects.wall.responses.*;
import com.vk.api.sdk.queries.users.UserField;

/**
 * @author Sergey
 *
 */
public class VkbotController
{
    private static final Logger log = LoggerFactory.getLogger(VkbotController.class);
    
    // Сколько дней пользователь не заходил
    private static final int DIFFERENCE = 2; 
    
    // Сколько добавить друзей
    private static final int LIMIT = 30; 
    
    AuthModel authmodel = new AuthModel();
    UserActor actor;
    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);
    
    ArrayList<String> blackList = new ArrayList();
    
    // флаг запуска/остановки
    boolean on = false;
    
    // Основной поток для http
    Thread myThread;
    
    // Основные настройки программы
    Settings settings;
    
    // Сколько друзей добавлено (счётчик)
    int countFriends;
    
    // Основной профиль
    List<UserXtrCounters> user;

    @FXML private TextField loginField; // логин
    @FXML private TextField passField; // пароль
    @FXML private TextField friendsCount; // Кол-во друзей, от
    @FXML private TextField followersCount; // Кол-во подписчиков, от
    @FXML private TextField groupUrl; // Адрес группы
    @FXML private Label messageLabel; // 
    @FXML private Label ratioFrFol; // Соотношение друзей к подписчикам
    @FXML private Label totalFriends; // Добавлено друзей
    @FXML private TextField delay_from; // Задержка, от
    @FXML private TextField delay_to; // Задержка, до
    @FXML private Button start; // кнопка Пуск
    @FXML private Button stop; // кнопка Стоп

    
    public void initialize() {
    	loadSettings();
    }
    
    public void loadSettings() {
    	settings = Settings.loadSettings();
    	loginField.setText(settings.login);
    	passField.setText(settings.password);
    	groupUrl.setText(settings.groupUrl);
    	friendsCount.setText(settings.friendsCountFrom.toString());
    	followersCount.setText(settings.followersCountFrom.toString());
    	delay_from.setText(settings.delayFrom.toString());
    	delay_to.setText(settings.delayTo.toString());	
    	log.debug("Настройки загружены");
    }
        
    public void auth() throws IOException {
    	
        String login = loginField.getText();
        String pass = passField.getText();
                
        Integer userId;
        String accessToken;
        
        String url = "https://oauth.vk.com/token?grant_type=password&client_id=2274003"
        		+"&client_secret=hHbZxrka2uZ6jB1inYsH&username=" + login + "&password=" + pass+"&v=5.103";
        
        String resp = getResp(url);
        
        StringBuilder builder = new StringBuilder();

        if (!StringUtils.isEmpty(login)) {
            builder.append(login);
        }

        if (!StringUtils.isEmpty(pass)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(pass);
        }

        if (builder.length() > 0) {
        	  	
            String name = "Успешно";
            messageLabel.setText("Авторизация: " + name);
        } else {
            messageLabel.setText("Введите данные");
        }
        
        parseToken(resp);
    }

    public void ratioCount() {
    	
    	int friend = 1;
    	int followers = 1;
    	
    	if ((!friendsCount.getText().equals("")) && (!followersCount.getText().equals("")) ) {
        	friend = Integer.parseInt(friendsCount.getText());
        	followers = Integer.parseInt(followersCount.getText());
		}
  	   	
    	if ((friend > 1) && (followers > 1)) {
        	ratioFrFol.setText("Соотношение друзей к подписчикам: " + (friend/followers) ); 
		} else {
	    	ratioFrFol.setText("Соотношение друзей к подписчикам: "); 
		}

    }
    
    public void parseToken(String resp) {
    	
    	JSONParser parser = new JSONParser();
    	Object obj=null;
		try {
			obj = parser.parse(resp);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}

		 JSONObject jsonObject = (JSONObject) obj;

		 authmodel.access_token = (String) jsonObject.get("access_token");
		 authmodel.user_id = (Long) jsonObject.get("user_id");
		 authmodel.expires_in = (Long) jsonObject.get("expires_in");
		 createActor(authmodel);
		 
    }

    public void createActor(AuthModel m) {

        actor = new UserActor(m.user_id.intValue(), m.access_token);
        
        log.debug(actor.toString());  
        
        try {
        	user = vk.users().get(actor)
        		.fields(UserField.FOLLOWERS_COUNT)
                .execute();
        	
        	String acc_name = user.get(0).getFirstName()
        			+ " "+ user.get(0).getLastName();
        	
        	countFriends = DatabaseWorker.loadFriendsCount(acc_name);
        	
        	log.info("Авторизовано как: " + acc_name);
        	
        	getTotalCountOfFriends(authmodel.user_id);
        	
        	blackList = FileWorker.getBlacklistFromFile(authmodel.user_id);
        	
        	totalFriends.setText("Добавлено: "+countFriends);
        	
        	log.debug("countFriends="+countFriends);

        } catch (ApiWallLinksForbiddenException e) {
            // Links posting is prohibited
        	System.out.println(e);
        } catch (ApiException e) {
            // Business logic error
        	System.out.println(e);
        } catch (ClientException e) {
            // Transport layer error
        	System.out.println(e);
        }
        
        catch(IOException e) {
        	System.out.println(e);
        }
        
    }

    // Получить список всех друзей аккаунта и добавить его в БД
    public int getTotalCountOfFriends(Long id) throws ApiException, ClientException {
    	
    	int totalCount = 0;
    	
    	totalCount = vk.friends().get(actor).execute().getCount();
    	
    	log.debug("getTotalCountOfFriends, COUNT="+totalCount);
    	
    	String acc_name = user.get(0).getFirstName()
    			+ " "+ user.get(0).getLastName();
		
		DatabaseWorker.saveFriendsCount(acc_name, countFriends, totalCount);
    	
    	return totalCount;
    	
    }
    
    public String getResp(String url) throws IOException {
    	
    	String s = "";

    	URL obj = new URL(url);
    	HttpURLConnection connection = null;

    	try {
    		connection = (HttpURLConnection) obj.openConnection();
        	connection.setRequestMethod("GET");

		} catch (Exception e) {
			System.out.println("===========");
			System.out.println(e);
			System.out.println("===========");
		}
    	
    	try {
        	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        	String inputLine;
        	StringBuffer response = new StringBuffer();
        	while ((inputLine = in.readLine()) != null) {
        	    response.append(inputLine);
        	}
        	in.close();
        	s = response.toString();
		} catch (Exception e) {
			System.out.println("===========");
			System.out.println(e.toString());
			System.out.println("===========");
		}

    	return s;
    }
    
    // Получение имени паблика/группы
    public String getPublicNameFromUrl(String url) {
 	    	
    	String[] temp = url.split("/");	
    	String screen_name = temp[temp.length-1];  	
    	log.info("Адрес группы: " + screen_name);
    	
		return screen_name;
    	
    }
    
    // Сохранить настройки
    public void saveSettings() {
    	settings = Settings.saveSettings(new Settings(
    			loginField.getText(),
    			passField.getText(),
    			groupUrl.getText(),
    			Integer.parseInt( friendsCount.getText() ), 
    			Integer.parseInt( followersCount.getText()), 
    			Integer.parseInt( delay_from.getText()), 
    			Integer.parseInt( delay_to.getText())));  	
    }
    
    // Основной метод, который запускается при нажатии кнопки Старт
    public void addFromGroup() throws InterruptedException {
    	
    	start.setDisable(true);
    	stop.setDisable(false);

    	on = true;
    	 	
    	// Сохранить настройки
    	saveSettings();
    	
    	// Если не авторизован
    	if (actor == null) {
    		try {
				auth();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
    	}
    	
    	// Получение имени паблика/группы
    	String screen_name = getPublicNameFromUrl( groupUrl.getText() );
    	
    	
    	myThread = new Thread(new Runnable() {
    	    @Override
    	    public void run() {
    	
    	while ( (countFriends < LIMIT) && (on) ) {
    	
    	////////////
    		
        String user_id;
    	
    	try {
    		
    		user_id = null;
    		
    		while (user_id == null) {
    			
    			user_id = parseUserFromGroup(screen_name);
    			log.debug("user_id = "+user_id);
    			Thread.sleep(2000);			
			}
    				
			int status = friendsAdd(user_id);
			
			switch (status) {
			case 1:
				log.info("1 — заявка на добавление данного пользователя в друзья отправлена");
				countFriends++;
				
			//	totalFriends.setText("Добавлено: "+countFriends);
				
	        	String acc_name = user.get(0).getFirstName()
	        			+ " "+ user.get(0).getLastName();
				
				DatabaseWorker.saveFriendsCount(acc_name, countFriends);
				
				blackList.add(user_id);
				
				FileWorker.saveBlacklistToFile(blackList, authmodel.user_id);
				
				break;
				
			case 2:
				log.info("2 — заявка на добавление в друзья от данного пользователя одобрена");
				break;
				
			case 4:
				log.info("4 — повторная отправка заявки");
				break;
				
			default:
				break;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		} catch (ApiException e) {

			int code = e.getCode();
			String message = e.getMessage();			
			log.error("code:" + code);
			log.error(message);
			
		} catch (InterruptedException e) {
			System.out.println("Поток остановлен через Thread.interrupt()");
		//	e.printStackTrace();
		}
    	
    	Random rand = new Random();
    	int timeout = rand.nextInt(settings.delayTo - settings.delayFrom) + settings.delayFrom;
    	log.info("Задержка " + timeout + " с");
    	
    	try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	}
    	////////////////////////
    	
    	
        }
    	    });
    	
    		myThread.setDaemon(true);

    	    myThread.start();
    	log.info("Завершено");
    }

    // Действие на нажатие кнопки Стоп
    public void stopped() {
    	on = false;
    	
    	start.setDisable(false);
    	stop.setDisable(true);
    	
    	myThread.interrupt();
    	
    	log.info("Остановлено");
    }
    
    // Отправка заявки на добавление в друзья
    public int friendsAdd(String user_id) throws NumberFormatException, ApiException, ClientException {
    	
    	int result = 0;

    	result = vk.friends().add(actor, Integer.parseInt(user_id)).execute().getValue();

    	return result;
    }
    
    // Получение ID, пригодного для добавления в друзья
    public String parseUserFromGroup(String screen_name) throws IOException, ClientException, ApiException, InterruptedException {
    	
    	// Получаем рандомный ид участника группы
    	String id = getRandomId(screen_name);	
    	
    	Thread.sleep(500);
    	
    	// Если пользователь в ЧС, то вернуть null
    	if ( FileWorker.checkFriend(id, blackList) == true ) {
    		return null;
    	}
    	
    	// Информация об участнике
    	List<UserXtrCounters> userInfo = vk.users().get(actor)
    			.userIds(id)
        		.fields(UserField.FOLLOWERS_COUNT, UserField.LAST_SEEN) // Количество подписчиков, дата последнего посещения
                .execute();
    	
    	// Если пользователь забанен, то вернуть null
    	if ( userInfo.get(0).getDeactivated() != null) {
    		return null;
    	}
    	
    	// Сколько дней не заходил пользователь
    	int last_seen = userInfo.get(0).getLastSeen().getTime();    
    //	System.out.println("last_seen="+last_seen);	
    	java.util.Date date=new java.util.Date((long)last_seen*1000);
    	
    	// Если пользователь заходил менее "DIFFERENCE" дней назад и профиль не закрыт
    	if ((differenceBtwTwoDates(date) < DIFFERENCE) &&(!isClosed(id))) {
    		
        	// Количество подписчиков
        	int followers = userInfo.get(0).getFollowersCount();
        	
        	// Количество друзей
        	int friends = vk.friends().get(actor)
        			.userId(Integer.parseInt(id))    			
        			.execute().getCount();
        	
        	log.debug("Подписчиков="+followers);
        	log.debug("Друзей="+friends);
        	log.info(String.format("ID:%s, %s %s, %d/%d", id, userInfo.get(0).getFirstName(), userInfo.get(0).getLastName(), friends, followers));
   
            // Если кол-во друзей пользователя больше заданного, а число подписчиков меньше заданного
            if ( (friends > Integer.parseInt(friendsCount.getText())) && (followers < Integer.parseInt(followersCount.getText())) ) {
            	
            	Thread.sleep(1000);
            	
            	// Является ли пользователь другом или отправлена ли заявка
            	if (!areFriends(id)) {
					
            		// Если не друг, то возвращаем ид
            		return id;
				}
            
			}

		}
    	
    	return null;
    }
    
    // Открытый или закрытый профиль
    public boolean isClosed(String id) throws IOException {
    	
    	JSONParser parser = new JSONParser();
    	Object obj=null;
    	
    	String request = "https://api.vk.com/method/"
    			+ "users.get?"
    			+ "user_ids=" + id
    			+ "&access_token=" + authmodel.access_token
    			+ "&v=5.103";
    	
    	String resp = getResp(request);
    	
    	log.debug(resp);
    	

		try {
			obj = parser.parse(resp);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = (JSONObject) obj;
		
		JSONArray items = (JSONArray) jsonObject.get("response");		
		
		String item = items.get(0).toString();
		
		int index = item.indexOf("is_closed");
		
		String s = item.substring(index+11, item.length());
		
		s = s.substring(0, s.length()-1);
		
		boolean result=false;
		
		switch (s) {
		case "true":
			result = true;
			break;
			
		case "false":
			result = false;
			break;

		default:
			break;
		}

    	return result;
    }
         
    // Расчет разницы между заданной датой и текущей (Now) 
    public int differenceBtwTwoDates(Date dateLastSeen) {

    	Date dateNow = new Date();

        log.debug("Дата посещения: " + dateLastSeen);

        long milliseconds = dateNow.getTime() - dateLastSeen.getTime();

        // 24 часа = 1 440 минут = 1 день
        int days = (int) (milliseconds / (24 * 60 * 60 * 1000));
        log.debug("Разница между датами в днях: " + days);
    	
    	return days;
    }
    
    // Получение рандомного ID из первой тысячи участников паблика
    public String getRandomId(String screen_name) throws IOException, ClientException, ApiException {

    	List<Integer> ids = getLinksList(screen_name);
    	
    	String id;
    	
    	Random random = new Random();
    	
    	int c = random.nextInt(ids.size());
    	
    	id = ids.get(c).toString();

    	return id;
    }
    
    // Рандомное смещение offset
    public int randomOffset(String screen_name) throws ApiException, ClientException {
    	
    	int count = getMembersCount(screen_name);
    	
    	log.debug("Количество людей в паблике: "+ count);
    	
    	Random rand = new Random();
    	
    	int offset = rand.nextInt( (int) count/1000 );
    	
    	return offset;
    }
    
    // Получение списка ID
    public List<Integer> getLinksList(String screen_name) throws IOException, ClientException, ApiException {
    	int offset = randomOffset(screen_name);
        List<Integer> linkList = vk.groups().getMembers(actor).groupId(screen_name).offset(offset).execute().getItems();
        return linkList;
    }
    
    // Получение количества участников
    public Integer getMembersCount(String screen_name) throws ClientException, ApiException {
        return vk.groups().getMembers(actor).groupId(screen_name).execute().getCount();
    }
    
    // Статус дружбы, друзья или не друзья
    public boolean areFriends(String id) throws NumberFormatException, ApiException, ClientException {
    	
    	boolean result = false;
    	
    	List<FriendStatus> resp = vk.friends().areFriends(actor, Integer.parseInt(id)).execute();
    	
    	int status = resp.get(0).getFriendStatus().getValue();
    	
    	switch (status) {
		case 0:
			result = false;
			log.debug("0 – пользователь не является другом");
			break;
			
		case 1:
			result = true;
			log.debug("1 – отправлена заявка/подписка пользователю");
			break;
			
		case 2:
			result = false;
			log.debug("2 – имеется входящая заявка/подписка от пользователя");
			break;
			
		case 3:
			result = true;
			log.debug("3 – пользователь является другом");
			break;
			
		default:
			break;
		}
    	
    	return result;
    }

}
