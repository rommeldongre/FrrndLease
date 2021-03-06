package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.DeleteRequestReqObj;
import pojos.DeleteRequestResObj;
import pojos.ItemsModel;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.ReqObj;
import pojos.ResObj;
import util.OAuth;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;

public class DeleteRequestHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(DeleteRequestHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private String item_Id = null;

	private static DeleteRequestHandler instance = null;

	public static DeleteRequestHandler getInstance() {
		if (instance == null)
			instance = new DeleteRequestHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		DeleteRequestReqObj rq = (DeleteRequestReqObj) req;
		DeleteRequestResObj rs = new DeleteRequestResObj();
		LOGGER.info("Inside process method " + rq.getRequest_Id() + ", " + rq.getUserId());
		// TODO: Core of the processing takes place here

		LOGGER.info("inside DeleteRequestHandler method");
		String sql2 = "SELECT * FROM requests WHERE request_id=?"; //

		Connection hcp = getConnectionFromPool();

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			LOGGER.info("Creating Statement....");
			PreparedStatement stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, rq.getRequest_Id());
			ResultSet rs1 = stmt2.executeQuery();
			while (rs1.next()) {
				item_Id = rs1.getString("request_item_id");
			}
			stmt2.close();
			
			if (item_Id != null) {

				// code for populating item pojo for sending requester email
				ItemsModel im = new ItemsModel();
				String sql1 = "SELECT * FROM items WHERE item_id=?";
				LOGGER.info("Creating a statement .....");
				PreparedStatement stmt1 = hcp.prepareStatement(sql1);

				LOGGER.info("Statement created. Executing select row query of FLS_MAIL_REJECT_REQUEST_TO...");
				stmt1.setString(1, item_Id);

				ResultSet dbResponse = stmt1.executeQuery();
				

				LOGGER.info("Query to request pojos fired into requests table");
				if (dbResponse.next()) {

					if (dbResponse.getString("item_id") != null) {
						LOGGER.info("Inside Nested check1 statement of FLS_MAIL_REJECT_REQUEST_TO");

						// Populate the response
						try {
							JSONObject obj1 = new JSONObject();
							obj1.put("title", dbResponse.getString("item_name"));
							obj1.put("description", dbResponse.getString("item_desc"));
							obj1.put("category", dbResponse.getString("item_category"));
							obj1.put("userId", dbResponse.getString("item_user_id"));
							obj1.put("leaseTerm", dbResponse.getString("item_lease_term"));
							obj1.put("id", dbResponse.getString("item_id"));
							obj1.put("leaseValue", dbResponse.getString("item_lease_value"));
							obj1.put("status", "InStore");
							if(dbResponse.getString("item_primary_image_link") == null || dbResponse.getString("item_primary_image_link").equals("null"))
								obj1.put("primaryImageLink", "");
							else
								obj1.put("primaryImageLink", dbResponse.getString("item_primary_image_link"));
							obj1.put("uid", dbResponse.getString("item_uid"));

							im.getData(obj1);
							LOGGER.info("Json parsed for FLS_MAIL_REJECT_REQUEST_TO");
						} catch (JSONException e) {
							LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_REJECT_REQUEST_TO");
							e.printStackTrace();
						}

					}
				}
				// code for populating item pojo for sending requester email
				// ends here

				String sql = "UPDATE requests SET request_status=? WHERE request_id=?"; //
				String status = "Archived";
				PreparedStatement stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + rq.getRequest_Id());
				stmt.setString(1, status);
				stmt.setInt(2, rq.getRequest_Id());
				stmt.executeUpdate();
				stmt.close();
				LOGGER.warning("Deleted request id : " + rq.getRequest_Id());

				try {
					Event event = new Event();
					event.createEvent(im.getUserId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_REQUEST_FROM, im.getId(), "Your Request for item having id <a href=\"" + URL + "/ItemDetails?uid=" + im.getUid() + "\">" + im.getTitle() + "</a> has been removed. ");
					event.createEvent(rq.getUserId(), im.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_REQUEST_TO, im.getId(), "Request for item having id <a href=\"" + URL + "/ItemDetails?uid=" + im.getUid() + "\">" + im.getTitle() + "</a> has been removed by the Requestor. ");
					rs.setMessage(FLS_DELETE_REQUEST);
					rs.setCode(FLS_SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				stmt1.close();
			} else {
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				LOGGER.warning("Entry not found in database!!");
			}
		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} finally {
			hcp.close();
		}

		LOGGER.info("Finished process method ");
		// return the response
		return rs;

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
