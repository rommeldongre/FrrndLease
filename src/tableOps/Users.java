package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import adminOps.Response;
import connect.Connect;
import pojos.UsersModel;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.Event.User_Notification;
import util.FlsCredit.Credit;
import util.FlsConfig;
import util.FlsCredit;
import util.FlsLogger;
import util.FlsPlan;
import util.MailChimp;
import util.OAuth;
import util.ReferralCode;

public class Users extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Users.class.getName());

	private String userId, userUid, email, fullName, mobile, location, auth, activation, status, message, operation, Id = null,
			check = null, token, address, locality, sublocality, referralCode=null,profilePicture,friendId;
	private float lat, lng;
	private String signUpStatus;
	private int liveStatus, Code, offset, limit, verification,userStatus;
	private UsersModel um;
	private Response res = new Response();

	public Response selectOp(String Operation, UsersModel usm, JSONObject obj) {
		operation = Operation.toLowerCase();
		um = usm;

		switch (operation) {

		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		case "edit":
			LOGGER.info("Edit operation is selected.");
			Edit();
			break;

		case "getnext":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getprevious":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "info":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				signUpStatus = obj.getString("signUpStatus");
				getUserInfo();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getusers":
			LOGGER.info("Get Users operation is selected");
			try{
				offset = obj.getInt("cookie");
				limit = obj.getInt("limit");
				getUsers();
			}catch(JSONException e){
				res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "editlivestatus":
			LOGGER.info("Edit Live Status is selected");
			editLiveStatus();
			break;
				
		case "editverification":
			LOGGER.info("Edit Verification is selected");
			editVerification();
			break;
			
		case "resetpassword":
			LOGGER.info("Reset Password is selected");
			try {
				token = obj.getString("token");
				resetPassword();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "secuserid":
			LOGGER.info("Saving the secondary user id");
			saveSecUserId();
			break;
			
		case "userfromuid":
			LOGGER.info("Getting user data from user uid");
			getUserDataFromUid();
			break;
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}

	private void getUserDataFromUid() {
		LOGGER.info("Inside getDataFromUid Method");
		userUid = um.getUserUid();
		
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String sqlGetData = "SELECT * FROM users WHERE user_uid=?";
			ps1 = hcp.prepareStatement(sqlGetData);
			ps1.setString(1, userUid);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				JSONObject json = new JSONObject();
				json.put("userId", rs1.getString("user_id"));
				json.put("userName", rs1.getString("user_full_name"));
				
				message = json.toString();
				res.setData(FLS_SUCCESS, "0", message);
			}else{
				res.setData(FLS_INVALID_USER_I, "0", FLS_INVALID_USER_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void saveSecUserId(){
		userId = um.getUserId();
		email = um.getEmail();
		mobile = um.getMobile();
		activation = um.getActivation();
		
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String sqlCheckStatus = "SELECT user_email, user_mobile, user_status, user_sec_status FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlCheckStatus);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				switch(rs1.getString("user_status")){
					case "facebook":
					case "google":
					case "email_activated":
					case "email_pending":
						if(!mobile.equals(rs1.getString("user_mobile"))){
							Random rnd = new Random();
							activation =  100000 + rnd.nextInt(900000)+"";
							String sqlUpdateMobile = "UPDATE users SET user_mobile=?, user_activation=?, user_sec_status=?, user_notification=? WHERE user_id=?";
							ps2 = hcp.prepareStatement(sqlUpdateMobile);
							ps2.setString(1, mobile);
							ps2.setString(2, activation+"_n");
							ps2.setString(3, "0");
							ps2.setString(4, User_Notification.EMAIL.name());
							ps2.setString(5, userId);
							ps2.executeUpdate();
							try {
								Event event = new Event();
								event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MOBILE_VERIFICATION, 0, "Here is the OTP for your mobile verification: " + activation);
								res.setData(FLS_SUCCESS, "0", "An OTP has been sent to this number");
							} catch (Exception e) {
								e.printStackTrace();
								res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
							}
						}else{
							if(rs1.getString("user_sec_status").equals("0")){
								Random rnd = new Random();
								activation =  100000 + rnd.nextInt(900000)+"";
								String sqlUpdateOtp = "UPDATE users SET user_activation=?, user_notification=? WHERE user_id=?";
								ps3 = hcp.prepareStatement(sqlUpdateOtp);
								ps3.setString(1, activation+"_n");
								ps3.setString(2, User_Notification.EMAIL.name());
								ps3.setString(3, userId);
								ps3.executeUpdate();
								try {
									Event event = new Event();
									event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MOBILE_VERIFICATION, 0, "Here is the OTP for your mobile verification: " + activation);
									res.setData(FLS_SUCCESS, "0", "An OTP has been sent to this number");
								} catch (Exception e) {
									e.printStackTrace();
									res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
								}
							}else{
								res.setData(FLS_DUPLICATE_ENTRY, "0", "This mobile number is already activated.");
							}
						}
						break;
					case "mobile_activated":
					case "mobile_pending":
						if(!email.equals(rs1.getString("user_email"))){
							String sqlUpdateEmail = "UPDATE users SET user_email=?, user_activation=?, user_sec_status=?, user_notification=? WHERE user_id=?";
							ps2 = hcp.prepareStatement(sqlUpdateEmail);
							ps2.setString(1, email);
							ps2.setString(2, activation+"_n");
							ps2.setString(3, "0");
							ps2.setString(4, User_Notification.SMS.name());
							ps2.setString(5, userId);
							ps2.executeUpdate();
							try {
								Event event = new Event();
								event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_EMAIL_VERIFICATION, 0, "Click on the link sent to this email id.");
								res.setData(FLS_SUCCESS, "0", "Click on the link sent to this email id.");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else{
							if(rs1.getString("user_sec_status").equals("0")){
								String sqlUpdateActivation = "UPDATE users SET user_activation=?, user_notification=? WHERE user_id=?";
								ps3 = hcp.prepareStatement(sqlUpdateActivation);
								ps3.setString(1, activation+"_n");
								ps3.setString(2, User_Notification.SMS.name());
								ps3.setString(3, userId);
								ps3.executeUpdate();
								try {
									Event event = new Event();
									event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_EMAIL_VERIFICATION, 0, "Click on the link sent to this email id.");
									res.setData(FLS_SUCCESS, "0", "Click on the link sent to this email id.");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}else{
								res.setData(FLS_DUPLICATE_ENTRY, "0", "This email is already activated.");
							}
						}
						break;
				}
				
			}else{
				res.setData(FLS_END_OF_DB, "0", FLS_END_OF_DB_M);	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}catch(NullPointerException e){
			e.printStackTrace();
			res.setData(FLS_NULL_POINT, "0", FLS_NULL_POINT_M);
		}finally{
			try {
				if(ps3 != null)	ps3.close();
				if(ps2 != null) ps2.close();
				if(rs1!=null) rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void Add() {
		userId = um.getUserId();
		email = um.getEmail();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		activation = um.getActivation();
		status = um.getStatus();
		address = um.getAddress();
		locality = um.getLocality();
		sublocality = um.getSublocality();
		lat = um.getLat();
		lng = um.getLng();
		referralCode = um.getReferralCode();
		profilePicture = um.getProfilePicture();
		friendId = um.getFriendId();
		 
		String userNotification = User_Notification.EMAIL.name();
		String user_fb_id= null;
		String ENV_CONFIG = FlsConfig.env;
		
		Map<String, String> usermap = new HashMap<String, String>();
		
		if(friendId.contains("@fb")){
			user_fb_id=friendId;
		}
		
		if(status.equals("mobile_pending")){
			Random rnd = new Random();
			activation =  100000 + rnd.nextInt(900000)+"";
			userNotification = User_Notification.SMS.name();
		}
		
		String  referrer_code=null;
		PreparedStatement stmt = null, stmt1 = null,stmt2=null,stmt3 = null, stmt4 = null, stmt5=null;
		ResultSet rs1 = null,rs2=null,rs3=null;
		Connection hcp = getConnectionFromPool();

		try {
			String checkUserSql = "SELECT * FROM `users` WHERE user_id=?";
			
			LOGGER.info("Creating select statement to check existing users.....");
			stmt1 = hcp.prepareStatement(checkUserSql);
			
			LOGGER.info("Statement created. Executing query.....");
			stmt1.setString(1, userId);
			rs1 = stmt1.executeQuery();
			
			if(referralCode!=null){
				String checkReferrerSql = "SELECT * FROM `users` WHERE user_referral_code=?";
				LOGGER.info("Creating select statement to check Referrer exists or not.....");
				stmt2 = hcp.prepareStatement(checkReferrerSql);
				LOGGER.info("Statement created. Executing query.....");
				stmt2.setString(1, referralCode);
				rs2 = stmt2.executeQuery();
				
				if(rs2.next()){
					referrer_code = rs2.getString("user_referral_code");
				}
			}
			
			if(!rs1.next()){
			LOGGER.info("Creating statement.....");
			int ref_code_length = 8;
			ReferralCode rc = new ReferralCode();
			String generated_ref_code = rc.createRandomCode(ref_code_length);
			String sql = "insert into users (user_id,user_full_name,user_mobile,user_email,user_location,user_auth,user_activation,user_status,user_credit,user_lat,user_lng,user_address,user_locality,user_sublocality,user_referral_code,user_referrer_code,user_profile_picture,user_live_status,user_notification,user_fb_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, fullName);
			stmt.setString(3, mobile);
			stmt.setString(4, email);
			stmt.setString(5, location);
			stmt.setString(6, auth);
			stmt.setString(7, activation+"_u");
			stmt.setString(8, status);
			stmt.setInt(9, 0);
			stmt.setFloat(10, lat);
			stmt.setFloat(11, lng);
			stmt.setString(12, address);
			stmt.setString(13, locality);
			stmt.setString(14, sublocality);
			stmt.setString(15, generated_ref_code);
			stmt.setString(16, referrer_code);
			stmt.setString(17, profilePicture);
			stmt.setInt(18, 1);
			stmt.setString(19, userNotification);
			stmt.setString(20, user_fb_id);
			stmt.executeUpdate();
			message = "Entry added into users table";
			LOGGER.warning(message);
			Code = 37;
			Id = generated_ref_code;
			
			FlsCredit credits = new FlsCredit();
			credits.logCredit(userId, 10, "Signed Up", "", Credit.ADD);
			
			String sqlChangeFriendStatus = "UPDATE friends SET friend_id=?, friend_status=? , friend_full_name=? WHERE friend_id=?";
			stmt3 = hcp.prepareStatement(sqlChangeFriendStatus);
			stmt3.setString(1, userId);
			stmt3.setString(2, "signedup");
			stmt3.setString(3, fullName);
			stmt3.setString(4, friendId);
			stmt3.executeUpdate();
			
			try {
				if(status.equals("facebook")) {
					// Grab the Scheduler instance from the Factory
					Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
					JobKey matchjobKey = JobKey.jobKey("FlsMatchFbIdJob", "FlsMatchFbIdGroup");
					scheduler.triggerJob(matchjobKey);
				}
			} catch(SchedulerException e){
				LOGGER.warning("Not able to start match fb id job");
				e.printStackTrace();
			}
			
			FlsPlan plan = new FlsPlan();
			plan.checkPlan(userId);
			
			Random rnd = new Random();
			int r = 10000 + rnd.nextInt(90000);
			
			String userUid = fullName + " " + r;
			userUid = userUid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
			
			String sqlUpdateUserUID = "UPDATE users SET user_uid=? WHERE user_id=?";
			stmt4 = hcp.prepareStatement(sqlUpdateUserUID);
			stmt4.setString(1, userUid);
			stmt4.setString(2, userId);
			stmt4.executeUpdate();
			
			//sending values to mail chimp
			LOGGER.info("Before calling mailchimp method ");
			if(!ENV_CONFIG.equals("dev") && (!email.equals("") || !email.equals("null")|| !email.equals(null)) ){
				LOGGER.info("Select statement for getting row from users table for mailchimp .....");
				String sqlGetUserInfo = "select * from users where user_id=?";
				stmt5 = hcp.prepareStatement(sqlGetUserInfo);
				stmt5.setString(1, email);
				rs3 = stmt5.executeQuery();
				
				if(rs3.next()){
					usermap.put("SIGNUP_DAT", rs3.getString("user_signup_date"));
					usermap.put("FEE_EXPIRY", rs3.getString("user_fee_expiry"));
					usermap.put("UID", rs3.getString("user_uid"));
					usermap.put("PROFILE", rs3.getString("user_profile_picture"));
					usermap.put("PLAN", rs3.getString("user_plan"));
					usermap.put("FULL_NAME", rs3.getString("user_full_name"));
					usermap.put("MOBILE", String.valueOf(rs3.getInt("user_mobile")));
					usermap.put("SEC_ID", rs3.getString("user_sec_status"));
					usermap.put("SUB_LOC", rs3.getString("user_sublocality"));
					usermap.put("LOCALITY", rs3.getString("user_locality"));
					usermap.put("REF_CODE", rs3.getString("user_referral_code"));
					usermap.put("PHOTO_ID", rs3.getString("user_photo_id"));
					usermap.put("FRIEND_REF", rs3.getString("user_referrer_code"));
					usermap.put("CREDITS", String.valueOf(rs3.getInt("user_credit")));
					usermap.put("VERIFY", String.valueOf(rs3.getInt("user_verified_flag")));
					usermap.put("STATUS",  String.valueOf(rs3.getInt("user_live_status")));
					usermap.put("SIGN_SOURC", rs3.getString("user_status"));
					usermap.put("ITEM_COUNT", String.valueOf(rs3.getInt("user_items")));
					usermap.put("LEASE_COUN", String.valueOf(rs3.getInt("user_leases")));
					usermap.put("ABOUT", rs3.getString("about"));
					usermap.put("WEBSITE", rs3.getString("website"));
					usermap.put("BUS_EMAIL", rs3.getString("email"));
					usermap.put("LOCATION", rs3.getString("user_location"));
					usermap.put("BUS_NUM", rs3.getString("phone_no"));
					usermap.put("BUS_HOURS", rs3.getString("business_hours"));

					
					MailChimp mce = new MailChimp();
					mce.addUserToList(email,usermap);
					LOGGER.info("after calling mailchimp method ");
				}
				
			}else{
				LOGGER.warning("Dev environment for MailChimp");
			}
			
			try {
				Event event = new Event();
				if (status.equals("email_pending")){
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_SIGNUP_VALIDATION, 0, "Click on the link sent to your registered email account.");
				}else if(status.equals("mobile_pending")){
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_SMS_SIGNUP_VALIDATION, 0, "Here is the OTP for your mobile verification of FrrndLease account: " + activation);
				}else{
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_REGISTER, 0, "Your email has been registered to FrrndLease.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			JSONObject jObj = new JSONObject();

			try {
				OAuth oauth = new OAuth();
				String access_token = oauth.generateOAuth(userId);
				jObj.put("access_token", access_token);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			message = jObj.toString();
			
			res.setData(FLS_SUCCESS, Id, message);
			}else{
				res.setData(FLS_DUPLICATE_ENTRY, "200", "Account with this User Id already exists");	
			}
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(stmt5 != null) stmt5.close();
				if(stmt4 != null) stmt4.close();
				if(rs1!=null) rs1.close();
				if(rs2 != null)rs2.close();
				if(rs3 != null)rs3.close();
				if(stmt3 != null) stmt3.close();
				if(stmt2 != null)stmt2.close();
				if(stmt!=null) stmt.close();
				if(stmt1!=null) stmt1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void Delete() {
		userId = um.getUserId();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "DELETE FROM users WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, userId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("user_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, userId);
				stmt.executeUpdate();
				message = "operation successfull deleted user id : " + userId;
				LOGGER.warning(message);
				Code = 38;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			
			try {
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(stmt2!=null) stmt2.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Edit() {
		userId = um.getUserId();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		check = null;

		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		LOGGER.info("inside edit method");
		String sql = "UPDATE users SET user_full_name=?, user_mobile=?, user_location=?, user_auth=?  WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, userId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("user_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, fullName);
				stmt.setString(2, mobile);
				stmt.setString(3, location);
				stmt.setString(4, auth);
				stmt.setString(5, userId);
				stmt.executeUpdate();
				message = "operation successfull edited user id : " + userId;
				LOGGER.warning(message);
				Code = 39;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			
			try {
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(stmt2!=null) stmt2.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNext() {
		check = null;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM users WHERE user_id > ? ORDER BY user_id LIMIT 1";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("userId", rs.getString("user_id"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("mobile", rs.getString("user_mobile"));
				json.put("location", rs.getString("user_location"));
				json.put("auth", rs.getString("user_auth"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getString("user_id");
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getPrevious() {
		check = null;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM users WHERE user_id < ? ORDER BY user_id DESC LIMIT 1";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("userId", rs.getString("user_id"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("mobile", rs.getString("user_mobile"));
				json.put("location", rs.getString("user_location"));
				json.put("auth", rs.getString("user_auth"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getString("user_id");
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getUserInfo() {
		check = null;
		auth = um.getAuth();
		profilePicture = um.getProfilePicture();
		friendId =null;
		String dbProfilePicture = null, user_fb_id=null;
		int profilePicrs=0,FbIdrs=0;
		LOGGER.info("Inside getUserInfo method");

		if(um.getFriendId().contains("@fb")){
			friendId = um.getFriendId();
		}
		
		PreparedStatement ps1 = null,s1 = null, stmt = null, stmt1 = null,
				profilepicstmt=null,FbIdstmt=null;
		ResultSet result1 = null, rs = null;
		Connection hcp = getConnectionFromPool();

		try {
			String select_status_sql = "Select user_live_status, user_status FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(select_status_sql);
			ps1.setString(1, token);

			result1 = ps1.executeQuery();

			if (result1.next()) {

				int liveStatus = result1.getInt("user_live_status");
				String status = result1.getString("user_status");
				
				if(liveStatus == 1){
					if (status.equals("facebook") || status.equals("google") || status.equals("email_activated") || status.equals("mobile_activated")) {

						if (status.equals(signUpStatus)) {
							String sql = "SELECT * FROM users WHERE user_id = ? AND user_auth = ?";

							LOGGER.info("Creating a statement .....");
							stmt = hcp.prepareStatement(sql);

							LOGGER.info("Statement created. Executing getPrevious query...");
							stmt.setString(1, token);
							stmt.setString(2, auth);

							rs = stmt.executeQuery();
							while (rs.next()) {
								JSONObject json = new JSONObject();
								json.put("userId", rs.getString("user_id"));
								json.put("fullName", rs.getString("user_full_name"));
								json.put("email", rs.getString("user_email"));
								json.put("mobile", rs.getString("user_mobile"));
								json.put("location", rs.getString("user_location"));
								json.put("referralCode", rs.getString("user_referral_code"));
								dbProfilePicture = rs.getString("user_profile_picture");
								user_fb_id = rs.getString("user_fb_id");
								message = json.toString();
								LOGGER.warning(message);
								check = rs.getString("user_id");
							}

							if (check != null) {
								if(profilePicture!=null && dbProfilePicture==null){
									String UpdatePicsql = "UPDATE users SET user_profile_picture=? WHERE user_id = ? AND user_auth = ?";
									LOGGER.info("Creating update Profile Pic statement .....");
									profilepicstmt = hcp.prepareStatement(UpdatePicsql);

									LOGGER.info("update Profile Pic Statement created. Executing  query...");
									profilepicstmt.setString(1, profilePicture);
									profilepicstmt.setString(2, token);
									profilepicstmt.setString(3, auth);

									profilePicrs = profilepicstmt.executeUpdate();
									if(profilePicrs!=0){
										Code = FLS_SUCCESS;
										Id = check;
									}else{
										Id = "0";
										message = FLS_LOGIN_USER_F;
										Code = FLS_END_OF_DB;
									}
							    }else{
							    	Code = FLS_SUCCESS;
									Id = check;
							    }
								
								if(friendId!=null && user_fb_id==null){
									String UpdateFbIdsql = "UPDATE users SET user_fb_id=? WHERE user_id = ? AND user_auth = ?";
									LOGGER.info("Creating update Fb Id statement .....");
									FbIdstmt = hcp.prepareStatement(UpdateFbIdsql);

									LOGGER.info("update Fb Id Statement created. Executing  query...");
									FbIdstmt.setString(1, friendId);
									FbIdstmt.setString(2, token);
									FbIdstmt.setString(3, auth);

									FbIdrs = FbIdstmt.executeUpdate();
									if(FbIdrs!=0){
										Code = FLS_SUCCESS;
										Id = check;
									}else{
										Id = "0";
										message = FLS_LOGIN_USER_F;
										Code = FLS_END_OF_DB;
									}
							    }else{
							    	Code = FLS_SUCCESS;
									Id = check;
							    }
								Code = FLS_SUCCESS;
								Id = check;
							} else {
								Id = "0";
								message = FLS_LOGIN_USER_F;
								Code = FLS_END_OF_DB;
							}
						} else {
							Id = "0";
							Code = FLS_END_OF_DB;
							if (status.equals("facebook") || status.equals("google"))
								message = "Signed up using " + status + "!! Please continue with " + status;
							else if (status.equals("email_activated"))
								message = "Signed up using email!! Please login with email";
							else if (status.equals("mobile_activated"))
								message = "Signed up using mobile!! Please login with phone number";
						}

					} else {
						if (status.equals("email_pending")) {
							Id = "0";
							Code = FLS_INVALID_OPERATION;
							message = "Please click on the link sent to your email to activate this account!!";
						} else if (status.equals("mobile_pending")) {
							Id = "0";
							Code = FLS_INVALID_OPERATION;
							message = "Your account is not activated using the OTP!!";
						} else {
							if (signUpStatus.equals("facebook") || signUpStatus.equals("google")) {
								String sql = "SELECT * FROM users WHERE user_id = ? AND user_auth = ?";

								LOGGER.info("Creating a statement .....");
								stmt = hcp.prepareStatement(sql);

								LOGGER.info("Statement created. Executing getPrevious query...");
								stmt.setString(1, token);
								stmt.setString(2, auth);

								rs = stmt.executeQuery();
								while (rs.next()) {
									JSONObject json = new JSONObject();
									json.put("userId", rs.getString("user_id"));
									json.put("email", rs.getString("user_email"));
									json.put("fullName", rs.getString("user_full_name"));
									json.put("mobile", rs.getString("user_mobile"));
									json.put("location", rs.getString("user_location"));

									message = json.toString();
									LOGGER.warning(message);
									check = rs.getString("user_id");
								}

								if (check != null) {
									String update_status = "UPDATE users SET user_status=? WHERE user_id=?";
									stmt1 = hcp.prepareStatement(update_status);

									stmt1.setString(1, signUpStatus);
									stmt1.setString(2, token);

									stmt1.executeUpdate();

									Code = FLS_SUCCESS;
									Id = check;
								} else {
									Id = "0";
									message = FLS_LOGIN_USER_F;
									Code = FLS_END_OF_DB;
								}
							} else {
								Id = "0";
								message = FLS_LOGIN_USER_F;
								Code = FLS_END_OF_DB;
							}
						}
					}
				}else{
					Id = "0";
					message = "Your account is kept on hold";
					Code = FLS_ENTRY_NOT_FOUND;
				}
				
				try {
					if(status.equals("facebook")) {
						// Grab the Scheduler instance from the Factory
						Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
						JobKey matchjobKey = JobKey.jobKey("FlsMatchFbIdJob", "FlsMatchFbIdGroup");
						scheduler.triggerJob(matchjobKey);
					}
				} catch(SchedulerException e){
					LOGGER.warning("Not able to start match fb id job");
					e.printStackTrace();
				}
				
			} else {
				Id = "0";
				message = "Email does not exist!!";
				Code = FLS_ENTRY_NOT_FOUND;
			}
			
			if(Code == FLS_SUCCESS){
				JSONObject jObj = new JSONObject(message);
				OAuth oauth = new OAuth();
				jObj.put("access_token", oauth.generateOAuth(token));
				message = jObj.toString();
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(result1!=null) result1.close();
				if(ps1!=null) ps1.close();
				if(rs!=null) rs.close();
				if(s1!=null) s1.close();
				if(profilepicstmt!=null) profilepicstmt.close();
				if(FbIdstmt!=null) FbIdstmt.close();
				if(stmt!=null) stmt.close();
				if(stmt1!=null) stmt1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void getUsers(){
		
		verification = um.getVerification();
		liveStatus = um.getLiveStatus();
		userStatus = um.getUserStatus();
		
		LOGGER.info("Inside Get Users Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlGetUsers = "SELECT * FROM `users` WHERE user_id NOT IN ('admin@frrndlease.com', 'ops@frrndlease.com')";
			
			if(verification != -1)
				sqlGetUsers = sqlGetUsers + " AND user_verified_flag=" + verification;
			
			if(liveStatus != -1)
				sqlGetUsers = sqlGetUsers + " AND user_live_status=" + liveStatus;
			
			if(userStatus == 0){
				sqlGetUsers = sqlGetUsers + " AND user_fee_expiry IS NULL";
			}else if(userStatus == 1){
				sqlGetUsers = sqlGetUsers + " AND user_fee_expiry IS NOT NULL";
			}
				
			
			sqlGetUsers = sqlGetUsers + " ORDER BY user_signup_date DESC LIMIT " + offset + ", " + limit;
			
			ps1 = hcp.prepareStatement(sqlGetUsers);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				rs1.previous();
				JSONObject object = new JSONObject();
				JSONArray array = new JSONArray();
				while(rs1.next()){
					JSONObject obj = new JSONObject();
					
					obj.put("userId", rs1.getString("user_id"));
					obj.put("userUid", rs1.getString("user_uid"));
					obj.put("fullName", rs1.getString("user_full_name"));
					obj.put("mobile", rs1.getString("user_mobile"));
					obj.put("status", rs1.getString("user_status"));
					obj.put("userActivation", rs1.getString("user_activation"));
					obj.put("credit", rs1.getInt("user_credit"));
					obj.put("lat", rs1.getFloat("user_lat"));
					obj.put("lng", rs1.getFloat("user_lng"));
					obj.put("address", rs1.getString("user_address"));
					obj.put("locality", rs1.getString("user_locality"));
					obj.put("sublocality", rs1.getString("user_sublocality"));
					obj.put("referral", rs1.getString("user_referral_code"));
					obj.put("referrer", rs1.getString("user_referrer_code"));
					obj.put("photoId", rs1.getString("user_photo_id"));
					obj.put("verification", rs1.getInt("user_verified_flag"));
					obj.put("profilePic", rs1.getString("user_profile_picture"));
					obj.put("liveStatus", rs1.getInt("user_live_status"));
					obj.put("signupDate", CalDate(rs1.getString("user_signup_date")));
					obj.put("userFeeExpiry", CalDate(rs1.getString("user_fee_expiry")));
					
					
					array.put(obj);
					offset = offset + 1;
				}
				object.put("users", array);
				object.put("offset", offset);
				Code = FLS_SUCCESS;
				message = object.toString();
			}else{
				Code = FLS_END_OF_DB;
			}
			res.setData(Code, Id, message);
		}catch(SQLException e){
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void editLiveStatus(){

		userId = um.getUserId();
		liveStatus = um.getLiveStatus();
		
		LOGGER.info("Inside Edit Live Status Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		
		try{
			
			String sqlEditLiveStatus = "UPDATE users SET user_live_status=?  WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlEditLiveStatus);
			ps1.setInt(1, liveStatus);
			ps1.setString(2, userId);
			
			ps1.executeUpdate();
			
			String sqlGetLivestatus = "SELECT user_live_status FROM users WHERE user_id=?";
			ps2 = hcp.prepareStatement(sqlGetLivestatus);
			ps2.setString(1, userId);
			
			rs2 = ps2.executeQuery();
			
			if(rs2.next()){
				if(rs2.getInt("user_live_status") == 0)
					message = "0";
				else
					message = "1";
			}
			
			res.setData(FLS_SUCCESS, "0", message);
			
		}catch(SQLException e){
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			try {
				if(rs2 != null)rs2.close();
				if(ps2 != null)ps2.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void editVerification(){

		userId = um.getUserId();
		verification = um.getVerification();
		
		LOGGER.info("Inside Edit Verification Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		
		try{
			
			FlsCredit credits = new FlsCredit();
			
			String sqlEditVerification = "UPDATE users SET user_verified_flag=?  WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlEditVerification);
			ps1.setInt(1, verification);
			ps1.setString(2, userId);
			
			ps1.executeUpdate();
			
			String sqlGetVerification = "SELECT user_verified_flag FROM users WHERE user_id=?";
			ps2 = hcp.prepareStatement(sqlGetVerification);
			ps2.setString(1, userId);
			
			rs2 = ps2.executeQuery();
			
			if(rs2.next()){
				if(rs2.getInt("user_verified_flag") == 0){
					message = "0";
					credits.logCredit(userId, 20, "Photo Id Unverified", "", Credit.SUB);
				}else{
					message = "1";
					credits.logCredit(userId, 20, "Photo Id Verified", "", Credit.ADD);
					
					try{
						Event event = new Event();
						event.createEvent("admin@frrndlease.com", userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_USER_PHOTO_ID_VERIFIED, 0, "Congratulations!! Your Photo Id is verified.");
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			FlsPlan plan = new FlsPlan();
			plan.checkPlan(userId);
			
			res.setData(FLS_SUCCESS, "0", message);
			
		}catch(SQLException e){
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			try {
				if(rs2 != null)rs2.close();
				if(ps2 != null)ps2.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void resetPassword(){
		
		auth = um.getAuth();
		activation = um.getActivation();
		
		LOGGER.info("Inside Reset Password Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		PreparedStatement ps2 = null;
		
		try{
			
			String sqlCheckActivation = "SELECT user_activation, user_status FROM users WHERE user_activation=?";
			ps1 = hcp.prepareStatement(sqlCheckActivation);
			ps1.setString(1, activation);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				if(rs1.getString("user_status").equals("mobile_activated")){
					Random rnd = new Random();
					token = 100000 + rnd.nextInt(900000)+"";
				}
				
				String sqlUpdateUserPassword = "UPDATE users SET user_auth=?,user_activation=? WHERE user_activation=?";
				ps2 = hcp.prepareStatement(sqlUpdateUserPassword);
				ps2.setString(1, auth);
				ps2.setString(2, token+"_u");
				ps2.setString(3, activation);
				
				ps2.executeUpdate();
				
				res.setData(FLS_SUCCESS, "0", "Your password has been reset");
			}else{
				res.setData(FLS_ENTRY_NOT_FOUND, "0", "This link is expired. To reset the password go back to ");
			}
			
		}catch(SQLException e){
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			try {
				if(ps2 != null)ps2.close();
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String CalDate(String Date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String Userdate=null;
		Date date = new Date();
		if(Date !=null){
			try {
				date = sdf.parse(Date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Date = Long.toString(date.getTime());
			Userdate = Date;
		}else{
			Userdate ="";
		}
		
	    return Userdate;
	}
	
}