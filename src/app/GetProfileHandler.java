package app;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.GetProfileReqObj;
import pojos.GetProfileResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetProfileHandler.class.getName());

	private Response res = new Response();

	private static GetProfileHandler instance = null;

	public static GetProfileHandler getInstance() {
		if (instance == null)
			instance = new GetProfileHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		GetProfileReqObj rq = (GetProfileReqObj) req;

		GetProfileResObj rs = new GetProfileResObj();

		LOGGER.info("Inside process method " + rq.getUserId());

		try {
			getConnection();
			String sql = "SELECT * FROM users WHERE user_id=?";
			LOGGER.info("Creating Statement...");
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, rq.getUserId());

			LOGGER.info("statement created...executing select from users query");
			ResultSet result = ps.executeQuery();

			LOGGER.info(result.toString());

			if (result.next()) {
				rs.setFullName(result.getString("user_full_name"));
				rs.setMobile(result.getString("user_mobile"));
				rs.setLocation(result.getString("user_location"));
				rs.setCode(FLS_SUCCESS);

				LOGGER.info("Printing out ResultSet: " + rs.getFullName() + ", " + rs.getMobile() + ", "
						+ rs.getLocation());
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