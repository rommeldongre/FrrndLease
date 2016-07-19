package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.UsersModel;
import util.FlsSendMail;
import util.AwsSESEmail;
import util.FlsLogger;
import util.LogCredit;
import util.ReferralCode;
import app.EmailVerificationHandler;

public class Users extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Users.class.getName());

	private String userId, fullName, mobile, location, auth, activation, status, message, operation, Id = null,
			check = null, token, address, locality, sublocality, referralCode=null,profilePicture;
	private float lat, lng;
	private String signUpStatus;
	private int liveStatus, Code, offset, limit, verification;
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
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}

	private void Add() {
		userId = um.getUserId();
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

		String  referrer_code=null;
		PreparedStatement stmt = null, stmt1 = null,stmt2=null;
		ResultSet rs1 = null,rs2=null;
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
			String generated_ref_code = rc.createRandomCode(ref_code_length, userId);
			String sql = "insert into users (user_id,user_full_name,user_mobile,user_location,user_auth,user_activation,user_status,user_credit,user_lat,user_lng,user_address,user_locality,user_sublocality,user_referral_code,user_referrer_code,user_profile_picture) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, fullName);
			stmt.setString(3, mobile);
			stmt.setString(4, location);
			stmt.setString(5, auth);
			stmt.setString(6, activation);
			stmt.setString(7, status);
			stmt.setInt(8, 10);
			stmt.setFloat(9, lat);
			stmt.setFloat(10, lng);
			stmt.setString(11, address);
			stmt.setString(12, locality);
			stmt.setString(13, sublocality);
			stmt.setString(14, generated_ref_code);
			stmt.setString(15, referrer_code);
			stmt.setString(16, profilePicture);
			stmt.executeUpdate();
			message = "Entry added into users table";
			LOGGER.warning(message);
			Code = 37;
			Id = generated_ref_code;
			
			LogCredit lc = new LogCredit();
			lc.addLogCredit(userId,10,"SignUp","");
			
			if(status!="email_pending"){
				EmailVerificationHandler ev = new EmailVerificationHandler();
				int result3 = ev.updateCredits(generated_ref_code,referrer_code);	
			}
			try {
				AwsSESEmail newE = new AwsSESEmail();
				if (status.equals("email_pending"))
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_SIGNUP_VALIDATION, um);
				else
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_REGISTER, um);
			} catch (Exception e) {
				e.printStackTrace();
			}

			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			}else{
				res.setData(FLS_DUPLICATE_ENTRY, "200", "Account with this Email Id already exists");	
			}
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs1!=null) rs1.close();
				if(stmt!=null) stmt.close();
				if(stmt1!=null) stmt1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
		LOGGER.info("Inside GetPrevious method");

		PreparedStatement ps1 = null,s1 = null, stmt = null, stmt1 = null;
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
					if (status.equals("facebook") || status.equals("google") || status.equals("email_activated")) {

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
								json.put("mobile", rs.getString("user_mobile"));
								json.put("location", rs.getString("user_location"));
								json.put("referralCode", rs.getString("user_referral_code"));

								message = json.toString();
								LOGGER.warning(message);
								check = rs.getString("user_id");
							}

							if (check != null) {
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
							else
								message = "Signed up using email!! Please login with email";
						}

					} else {
						if (status.equals("email_pending")) {
							Id = "0";
							Code = FLS_INVALID_OPERATION;
							message = "Please click on the link sent to your email to activate this account!!";
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
				
			} else {
				Id = "0";
				message = "Email does not exist!!";
				Code = FLS_ENTRY_NOT_FOUND;
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
		
		LOGGER.info("Inside Get Users Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlGetUsers = "SELECT * FROM `users` WHERE user_id NOT IN ('admin@frrndlease.com', 'ops@frrndlease.com')";
			
			if(verification != -1)
				sqlGetUsers = sqlGetUsers + " AND user_verified_flag=" + verification;
			
			sqlGetUsers = sqlGetUsers + " ORDER BY user_id LIMIT " + offset + ", " + limit;
			
			ps1 = hcp.prepareStatement(sqlGetUsers);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				rs1.previous();
				JSONObject object = new JSONObject();
				JSONArray array = new JSONArray();
				while(rs1.next()){
					JSONObject obj = new JSONObject();
					
					obj.put("userId", rs1.getString("user_id"));
					obj.put("fullName", rs1.getString("user_full_name"));
					obj.put("mobile", rs1.getString("user_mobile"));
					obj.put("status", rs1.getString("user_status"));
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
}