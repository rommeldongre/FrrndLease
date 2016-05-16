package app;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.EditProfileReqObj;
import pojos.EditProfileResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class EditProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EditProfileHandler.class.getName());

	private Response res = new Response();

	private static EditProfileHandler instance = null;

	public static EditProfileHandler getInstance() {
		if (instance == null)
			instance = new EditProfileHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		EditProfileReqObj rq = (EditProfileReqObj) req;

		EditProfileResObj rs = new EditProfileResObj();

		LOGGER.info("Inside Process Method " + rq.getUserId());

		try {
			String sql = "UPDATE users SET user_full_name=?, user_mobile=?, user_location=?  WHERE user_id=?";
			LOGGER.info("Creating Statement...");
			PreparedStatement ps = getConnectionFromPool().prepareStatement(sql);
			ps.setString(1, rq.getFullName());
			ps.setString(2, rq.getMobile());
			ps.setString(3, rq.getLocation());
			ps.setString(4, rq.getUserId());

			LOGGER.info("statement created...executing update to users query");
			int result = ps.executeUpdate();

			LOGGER.info("Update Query Result : " + result);

			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				LOGGER.warning("Profile Updated!!");
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
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
