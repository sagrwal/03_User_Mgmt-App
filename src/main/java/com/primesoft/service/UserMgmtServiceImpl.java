package com.primesoft.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.primesoft.bindings.ActivateAccount;
import com.primesoft.bindings.Login;
import com.primesoft.bindings.User;
import com.primesoft.entity.UserMaster;
import com.primesoft.repo.UserMasterRepo;
import com.primesoft.utils.EmailUtils;

@Service
public class UserMgmtServiceImpl implements UserMgmtService {
    
	@Autowired
	private UserMasterRepo userMasterRepo;
	
	@Autowired
	private EmailUtils emailUtils;
	
	@Override
	public boolean saveUser(User user) {
		UserMaster entity= new UserMaster();
		BeanUtils.copyProperties(user, entity);
		
		entity.setPassword(generateRandomPwd());
		entity.setAccStatus("In-Active");
		UserMaster save=userMasterRepo.save(entity);
		String subject = "Your Registration Success";
		String filename ="REG-MAIL-BODY.txt";
		String body= readEmailBody(entity.getFullname(),entity.getPassword(),filename);
		
		emailUtils.sendEmail(user.getEmail(),subject,body);
		
		
		return save.getUserId() != null;
	}

	@Override
	public boolean activateUserAcc(ActivateAccount activateAcc) {
		UserMaster entity = new UserMaster();
		entity.setEmail(activateAcc.getEmail());
		entity.setPassword(activateAcc.getTempPwd());
		
		//select * from user_master where email=? and pwd=?
		Example<UserMaster> of=Example.of(entity);
		
		List<UserMaster> findAll=userMasterRepo.findAll(of);
		
		if(findAll.isEmpty()) {
			return false;
			
		}else {
			UserMaster userMaster = findAll.get(0);
			userMaster.setPassword(activateAcc.getNewPwd());
			userMaster.setAccStatus("Active");
			userMasterRepo.save(userMaster);
			return true;
			
		}
		
	}

	@Override
	public List<User> getAllUsers() {
		List<UserMaster> findAll=userMasterRepo.findAll();
		
		List<User> users=new ArrayList<>();
		for(UserMaster entity : findAll) {
			User user= new User();
			BeanUtils.copyProperties(entity, user);
			users.add(user);
		}
		return users;
	}

	@Override
	public User getUserById(Integer userId) {
		Optional<UserMaster> findById=userMasterRepo.findById(userId);
		
		if(findById.isPresent()) {
			User user =new User();
			UserMaster userMaster= findById.get();
			BeanUtils.copyProperties(userMaster, user);
			return user;
		}
		
		return null;
	}
	@Override
	public boolean deleteUserById(Integer userId) {
		
     try {
    	 userMasterRepo.deleteById(userId);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean changeAccountStatus(Integer userId, String accStatus) {
		Optional<UserMaster> findById=userMasterRepo.findById(userId);
		if(findById.isPresent()) {
			UserMaster userMaster = findById.get();
			userMaster.setAccStatus(accStatus);
			userMasterRepo.save(userMaster);
			return true;
		}
		
		return false;
	}

	@Override
	public String login(Login login) {
		
	UserMaster entity=	userMasterRepo.findByEmailAndPassword(login.getEmail(),login.getPassword());
		if(entity==null) {
			return "Invalid Credentials";
		}
			
			if(entity.getAccStatus().equals("Active")) {
				return "SUCCESS";
			}else {
				return "Account not activated";
			}
		}
		

	@Override
	public String forgotPwd(String email) {
		UserMaster entity= userMasterRepo.findByEmail(email);
		
        if(entity == null) {
        	return "Invalid Email";
        }
        
        //To do- Send pwd to user in email
        
        String subject = "Forgot Password";
        String fileName= "RECOVER-MAIL-BODY.txt";
        String body = readEmailBody(entity.getFullname(),entity.getPassword(),fileName);
        
         boolean sendEmail = emailUtils.sendEmail(email, subject, body);
		
         if(sendEmail) {
        	 return "Password sent to your registered email";
         }
		return null;
	}
	
	
	private String generateRandomPwd() {
		
		 // create a string of uppercase and lowercase characters and numbers
	    String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
	    String numbers = "0123456789";
	    String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;

	    // create random string builder
	    StringBuilder sb = new StringBuilder();
	    
	    // create an object of Random class
	    Random random = new Random();
	    
	    // specify length of random string
	    int length = 6;
	    
	    for(int i = 0; i < length; i++) {
	      // generate random index number
	      int index = random.nextInt(alphaNumeric.length());

	      // get character specified by index
	      // from the string
	      char randomChar = alphaNumeric.charAt(index);

	      // append the character to string builder
	      sb.append(randomChar);
	    }
            
	    return sb.toString();
	   
	  }
	private String readEmailBody(String fullname, String pwd, String filename) {
		String fileName="REG-EMAIL-BODY.txt";
		String url="";
		String mailBody=null;
		
		try {
		FileReader fr= new FileReader(filename);
		BufferedReader br= new BufferedReader(fr);
		StringBuffer buffer = new StringBuffer();
		
		String line= br.readLine();
		
		while(line!= null) {
			buffer.append(line);
			line= br.readLine();
		}
		br.close();
		mailBody = buffer.toString();
		mailBody = mailBody.replace("{FULLNAME}", fullname);
		mailBody = mailBody.replace("{TEMP-PWD}",pwd);
		mailBody = mailBody.replace("{URL}",url);
		mailBody = mailBody.replace("PWD",pwd);
	}catch(Exception e) {
		e.printStackTrace();
	}
		return mailBody;
	}
}


