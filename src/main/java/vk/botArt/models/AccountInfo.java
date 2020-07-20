package vk.botArt.models;

import java.time.LocalDate;

public class AccountInfo {

	public String account_name;
	
	public int added_friends;
	
	public int total_friends;
	
	public LocalDate date;
	
	public AccountInfo(){
		
	}
	
	public AccountInfo(String account_name, int added_friends, int total_friends, LocalDate date){
		this.account_name = account_name;
		this.added_friends = added_friends;
		this.total_friends = total_friends;
		this.date = date;
	}
}
