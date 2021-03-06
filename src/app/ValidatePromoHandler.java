package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.ValidatePromoReqObj;
import pojos.ValidatePromoResObj;
import util.FlsConfig;
import util.FlsCredit;
import util.FlsLogger;
import util.OAuth;

public class ValidatePromoHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ValidatePromoHandler.class.getName());

	int CREDIT_VALUE = FlsConfig.creditValue;
	
	private static ValidatePromoHandler instance;

	public static ValidatePromoHandler getInstance() {
		if (instance == null)
			instance = new ValidatePromoHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of ValidatePromoHandler");

		ValidatePromoReqObj rq = (ValidatePromoReqObj) req;
		ValidatePromoResObj rs = new ValidatePromoResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try {
			
			FlsCredit credits = new FlsCredit();
			
			String promoCode = rq.getPromoCode();
			String userId = rq.getUserId();
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(promoCode == null || promoCode.equals("")){
				rs.setCode(FLS_INVALID_PROMO);
				rs.setMessage(FLS_INVALID_PROMO_M);
				return rs;
			}

			// Getting all the data for the promo code
			String sqlGetPromoData = "SELECT * FROM promo_credits WHERE code=?";
			ps1 = hcp.prepareStatement(sqlGetPromoData);
			ps1.setString(1, promoCode);
			
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				
				int credit = rs1.getInt("credit");
				LOGGER.info("Credits for the promo code is - " + credit);
				
				Date expiry = rs1.getDate("expiry");
				LOGGER.info("Expiry of the promo code is - " + expiry);
				if(expiry != null){
					if (credits.expired(expiry)) {
						rs.setCode(FLS_PROMO_EXPIRED);
						rs.setMessage(FLS_PROMO_EXPIRED_M);
						return rs;
					}
				}
				
				int count = rs1.getInt("count");
				LOGGER.info("Count of the promo code is - " + count);
				if(count != -1){
					if(count == 0){
						rs.setCode(FLS_PROMO_EXPIRED);
						rs.setMessage(FLS_PROMO_EXPIRED_M);
						return rs;
					}
				}
				
				String sqlGetFromOrders = "SELECT * FROM orders WHERE order_user_id=? AND promo_code=?";
				ps2 = hcp.prepareStatement(sqlGetFromOrders);
				ps2.setString(1, userId);
				ps2.setString(2, promoCode);
				
				rs2 = ps2.executeQuery();
				
				int tot = 0;
				
				while(rs2.next()){
					tot = tot + 1;
				}
				
				int perPersonCount = rs1.getInt("per_person_count");
				LOGGER.info("Per Person Count of the promo code is - " + perPersonCount);
				if(perPersonCount != -1){
					if(tot >= perPersonCount){
						rs.setCode(FLS_INVALID_PROMO);
						rs.setMessage("You cannot use this promo code more than " + perPersonCount + " time");
						return rs;
					}
				}
				
				String codeType = rs1.getString("code_type");
				if(codeType.equals("FLS_INTERNAL")){
					rs.setCode(FLS_INVALID_PROMO);
					rs.setMessage(FLS_INVALID_PROMO_M);
					return rs;
				}
				
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				rs.setDiscountAmount(CREDIT_VALUE * credit);

			} else {
				rs.setCode(FLS_INVALID_PROMO);
				rs.setMessage(FLS_INVALID_PROMO_M);
			}

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try{
				if (rs2 != null) rs2.close();
				if (rs1 != null) rs1.close();
				if (ps2 != null) ps2.close();
				if (ps1 != null) ps1.close();
				if (hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
