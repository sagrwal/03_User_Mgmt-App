package com.primesoft.service;

import java.util.List;

import com.primesoft.bindings.ActivateAccount;
import com.primesoft.bindings.Login;
import com.primesoft.bindings.User;

public interface UserMgmtService {
	
	public boolean saveUser(User user);
	
	public boolean activateUserAcc(ActivateAccount activateAcc);
	
	public List<User> getAllUsers();
	
	public User getUserById(Integer userId);
	
	
	
	public boolean deleteUserById(Integer userId);
	
	public boolean changeAccountStatus(Integer userId, String accStatus);
	
	public String login(Login login);
	
	public String forgotPwd(String email);

}
