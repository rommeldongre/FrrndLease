package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.UsersModel;

import util.FlsSendMail;
import util.AwsSESEmail;
import util.FlsLogger;

public class Users extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Users.class.getName());

	private String userId, fullName, mobile, location, auth, activation, status, message, operation, Id = null,
			check = null, token;
	private String signUpStatus;
	private int Code;
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

		String sql = "insert into users (user_id,user_full_name,user_mobile,user_location,user_auth,user_activation,user_status,user_credit) values (?,?,?,?,?,?,?,?)";
		getConnection();

		try {
			LOGGER.info("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, fullName);
			stmt.setString(3, mobile);
			stmt.setString(4, location);
			stmt.setString(5, auth);
			stmt.setString(6, activation);
			stmt.setString(7, status);
			stmt.setInt(8, 10);
			stmt.executeUpdate();
			message = "Entry added into users table";
			LOGGER.warning(message);
			Code = 37;
			Id = userId;

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
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}

	private void Delete() {
		userId = um.getUserId();
		check = null;
		LOGGER.info("Inside delete method....");

		getConnection();
		String sql = "DELETE FROM users WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";

		try {
			LOGGER.info("Creating statement...");

			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("user_id");
			}

			if (check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);

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
		}

	}

	private void Edit() {
		userId = um.getUserId();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		check = null;

		LOGGER.info("inside edit method");
		getConnection();
		String sql = "UPDATE users SET user_full_name=?, user_mobile=?, user_location=?, user_auth=?  WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";

		try {
			LOGGER.info("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("user_id");
			}

			if (check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);

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
		}
	}

	private void getNext() {
		check = null;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM users WHERE user_id > ? ORDER BY user_id LIMIT 1";

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, token);

			ResultSet rs = stmt.executeQuery();
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
		}
	}

	private void getPrevious() {
		check = null;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM users WHERE user_id < ? ORDER BY user_id DESC LIMIT 1";

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);

			ResultSet rs = stmt.executeQuery();
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
		}
	}

	private void getUserInfo() {
		check = null;
		auth = um.getAuth();
		LOGGER.info("Inside GetPrevious method");

		getConnection();

		try {
			String select_status_sql = "Select user_status FROM users WHERE user_id=?";
			PreparedStatement ps1 = connection.prepareStatement(select_status_sql);
			ps1.setString(1, token);

			ResultSet result1 = ps1.executeQuery();

			if (result1.next()) {

				String status = result1.getString("user_status");

				if (status.equals("facebook") || status.equals("google") || status.equals("email_activated")) {

					if (status.equals(signUpStatus)) {
						String sql = "SELECT * FROM users WHERE user_id = ? AND user_auth = ?";

						LOGGER.info("Creating a statement .....");
						PreparedStatement stmt = connection.prepareStatement(sql);

						LOGGER.info("Statement created. Executing getPrevious query...");
						stmt.setString(1, token);
						stmt.setString(2, auth);

						ResultSet rs = stmt.executeQuery();
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
							PreparedStatement stmt = connection.prepareStatement(sql);

							LOGGER.info("Statement created. Executing getPrevious query...");
							stmt.setString(1, token);
							stmt.setString(2, auth);

							ResultSet rs = stmt.executeQuery();
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
								PreparedStatement stmt1 = connection.prepareStatement(update_status);

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
		}
	}
}