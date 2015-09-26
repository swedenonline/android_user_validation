package com.bilal.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class UserManager {
	
	private Context context = null;
	private static final int MAX_LENGTH = 32;
	private static String RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	public UserManager(Context ctx) {
		this.context = ctx;
	}
	
	private String getString(int resId) {
		return context.getString(resId);
	}
	
	private String random() {
	    Random generator = new Random();
	    StringBuilder randomStringBuilder = new StringBuilder();
	    int randomLength = generator.nextInt(MAX_LENGTH);
	    char tempChar;
	    for (int i = 0; i < randomLength; i++){
	        tempChar = (char) (generator.nextInt(96) + 32);
	        randomStringBuilder.append(tempChar);
	    }
	    return randomStringBuilder.toString();
	}
	
	private String random1() {
		char[] chars = RANDOM_CHARS.toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < MAX_LENGTH; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
	private String getSaltString() {
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < MAX_LENGTH) {
            int index = (int) (rnd.nextFloat() * RANDOM_CHARS.length());
            salt.append(RANDOM_CHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

		
	/**
	 * function md5 encryption for passwords
	 * 
	 * @param password
	 * @return passwordEncrypted
	 */
	private final String md5(final String key) {
	    try {
	 
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(key.getBytes());
	        byte messageDigest[] = digest.digest();
	 
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
	 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	public String[] validateAccount(String phoneNum , String password, String requestUrl) {
		
		String[] response = new String[2];
		
		if(TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(password)) {
			response[0] = "-1";
			response[1] = "Enter valid phone number and password.";
			return response;
		}else {
			if(!TextUtils.isDigitsOnly(phoneNum)) {
				response[0] = "-1";
				response[1] = "Enter valid phone number.";
				return response;
			}else {
				if(!LinphoneManager.getInstance().isWifiConnected()) {
					response[0] = "-1";
					response[1] = getString(R.string.nointernet);
					return response;
				}else {
					if(!CustomFunctions.checkURL(requestUrl)){
						response[0] = "-1";
						response[1] = getString(R.string.serverdown);
						return response;
					}
					int responseCode	= 0;
					String statusMessage = "";
					
					try	{
						String challenge 				= getSaltString();
						requestUrl 						= "http://someurlhere?usr="+phoneNum+"&cm="+challenge;
						String appendKeywithPassword 	= challenge+password;
					    String localHashString       	= md5(appendKeywithPassword);
						HttpClient httpclient1 			= new DefaultHttpClient();
						HttpGet httpbalance 			= new HttpGet(requestUrl);
						HttpResponse balanceresponce 	= httpclient1.execute(httpbalance);
						StatusLine status 				= balanceresponce.getStatusLine();
						responseCode					= status.getStatusCode();
						statusMessage					= EntityUtils.toString(balanceresponce.getEntity());
						
						/*Log.i("validateAccount","//-----------------------------//");
						Log.i("validateAccount","<MD5 of bilal> "+md5("bilal"));
						Log.i("validateAccount","<Phone Number> "+phoneNum);
						Log.i("validateAccount","<Password> "+password);
						Log.i("validateAccount","<Request Url> "+requestUrl);
						Log.i("validateAccount","<Appended key with password> "+appendKeywithPassword);
						Log.i("validateAccount","<localHashString> "+localHashString);
						Log.i("validateAccount","<Response Code> "+responseCode);
						Log.i("validateAccount","<Response Message> "+statusMessage);
						Log.i("validateAccount","//-----------------------------//");*/
						
						if(responseCode == 200 || responseCode == 201) {
							if(statusMessage.equals(localHashString)) {
								response[0] = Integer.toString(responseCode);
								response[1] = "User authenticated successfully.";
							}else {
								response[0] = Integer.toString(0);
								response[1] = "Incorrect Password.";
							}	
						}
						/*else if (responseCode == 400) {
				            //Bad Request: Errmsg in response, e.g. "invalid phone number.
				        } else if (responseCode == 403) {
				            //Forbidden: Account exists but is disabled.
				        } else if (responseCode == 404) {
				            //Not found: Account doesn't exists.
				        } else if (responseCode == 500) {
				            //Internal server error
				        }
				        */
						else {
							response[0] = Integer.toString(responseCode);
							response[1] = statusMessage;	
						}
						
					}catch(Exception e) {
						e.printStackTrace();
						response[0] = "-1";
						response[1] = getString(R.string.serverdown);
					}
					return response;
				}
					
			}
		}
	}
}
