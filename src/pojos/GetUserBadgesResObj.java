package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetUserBadgesResObj extends ResObj {

	int code, userItems, userLeases, responseTime, responseCount, userCredit;
	String message, userSignupDate, userStatus;
	boolean userVerifiedFlag;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getUserItems() {
		return userItems;
	}

	public void setUserItems(int userItems) {
		this.userItems = userItems;
	}

	public int getUserLeases() {
		return userLeases;
	}

	public void setUserLeases(int userLeases) {
		this.userLeases = userLeases;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public int getResponseCount() {
		return responseCount;
	}

	public void setResponseCount(int responseCount) {
		this.responseCount = responseCount;
	}

	public int getUserCredit() {
		return userCredit;
	}

	public void setUserCredit(int userCredit) {
		this.userCredit = userCredit;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUserSignupDate() {
		return userSignupDate;
	}

	public void setUserSignupDate(String userSignupDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(userSignupDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.userSignupDate = Long.toString(date.getTime());
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public boolean isUserVerifiedFlag() {
		return userVerifiedFlag;
	}

	public void setUserVerifiedFlag(boolean userVerifiedFlag) {
		this.userVerifiedFlag = userVerifiedFlag;
	}

}
