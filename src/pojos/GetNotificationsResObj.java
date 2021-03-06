package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import util.Event.Icon_Type;

public class GetNotificationsResObj {

	int eventId, itemId;
	String datetime, fromUserId, toUserId, readStatus, notificationMsg, uid, title, profilePic, fullName, notificationType;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(datetime);
			this.datetime = Long.toString(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(String readStatus) {
		this.readStatus = readStatus;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getNotificationMsg() {
		return notificationMsg;
	}

	public void setNotificationMsg(String notificationMsg) {
		this.notificationMsg = notificationMsg;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		switch(notificationType){
			case "FLS_MAIL_REGISTER":
			case "FLS_MAIL_SIGNUP_VALIDATION":
			case "FLS_MAIL_FORGOT_PASSWORD":
				this.notificationType = Icon_Type.FLS_DEFAULT.name();
				break;
			case "FLS_MAIL_MATCH_WISHLIST_ITEM":
			case "FLS_MAIL_MATCH_POST_ITEM":
			case "FLS_MAIL_POST_ITEM":
			case "FLS_MAIL_DELETE_ITEM":
			case "FLS_MAIL_MAKE_REQUEST_FROM":
			case "FLS_MAIL_MAKE_REQUEST_TO":
				this.notificationType = Icon_Type.FLS_ITEM.name();
				break;
			case "FLS_MAIL_ADD_FRIEND_FROM":
			case "FLS_MAIL_ADD_FRIEND_FROM_NAME":
			case "FLS_MAIL_ADD_FRIEND_TO":
			case "FLS_MAIL_DELETE_FRIEND_FROM":
			case "FLS_MAIL_DELETE_FRIEND_TO":
			case "FLS_MAIL_OPS_ADD_LEAD":
			case "FLS_MAIL_ADD_LEAD":
			case "FLS_CREDITS_INVOICE":
			case "FLS_MEMBERSHIP_INVOICE":
			case "FLS_MAIL_ADMIN_PHOTO_ID_UPLOAD":
			case "FLS_MAIL_WEEKLY_DIGEST":
			case "FLS_MAIL_SHARE_ITEM_FRIEND":
			case "FLS_MAIL_SHARE_ITEM_OWNER":
				this.notificationType = Icon_Type.FLS_USER.name();
				break;
			case "FLS_MAIL_GRANT_REQUEST_FROM":
			case "FLS_MAIL_GRANT_REQUEST_TO":
			case "FLS_MAIL_GRANT_LEASE_FROM_SELF":
			case "FLS_MAIL_GRANT_LEASE_TO_SELF":
			case "FLS_MAIL_LEASE_READY_OWNER":
			case "FLS_MAIL_LEASE_READY_REQUESTOR":
			case "FLS_MAIL_GRANT_LEASE_FROM_PRIME":
			case "FLS_MAIL_GRANT_LEASE_TO_PRIME":
			case "FLS_MAIL_FROM_LEASE_STARTED":
			case "FLS_MAIL_TO_LEASE_STARTED":
			case "FLS_MAIL_OPS_PICKUP_READY":
			case "FLS_MAIL_ITEM_INSTORE_FROM":
			case "FLS_MAIL_ITEM_INSTORE_TO":
			case "FLS_MAIL_RENEW_LEASE_OWNER":
			case "FLS_MAIL_RENEW_LEASE_REQUESTOR":
			case "FLS_MAIL_ITEM_INSTORE":
			case "FLS_MAIL_USER_PHOTO_ID_VERIFIED":
				this.notificationType = Icon_Type.FLS_POSITIVE.name();
				break;
			case "FLS_MAIL_REJECT_REQUEST_FROM":
			case "FLS_MAIL_REJECT_REQUEST_TO":
			case "FLS_MAIL_DELETE_REQUEST_FROM":
			case "FLS_MAIL_DELETE_REQUEST_TO":
			case "FLS_MAIL_CLOSE_LEASE_FROM_SELF":
			case "FLS_MAIL_CLOSE_LEASE_TO_SELF":
			case "FLS_MAIL_LEASE_ENDED_OWNER":
			case "FLS_MAIL_LEASE_ENDED_REQUESTOR":
			case "FLS_MAIL_OPS_PICKUP_CLOSE":
			case "FLS_MAIL_ITEM_ON_HOLD":
			case "FLS_MAIL_OWNER_REQUEST_LIMIT":
				this.notificationType = Icon_Type.FLS_NEGATIVE.name();
				break;
			case "FLS_MAIL_GRACE_PERIOD_OWNER":
			case "FLS_MAIL_GRACE_PERIOD_REQUESTOR":
			case "FLS_MAIL_OLD_ITEM_WARN":
			case "FLS_MAIL_OLD_REQUEST_WARN":
			case "FLS_MAIL_OLD_LEASE_WARN":
			case "FLS_MAIL_UBER_WARN":
				this.notificationType = Icon_Type.FLS_TIME.name();
				break;
			case "FLS_MAIL_MESSAGE_FRIEND_FROM":
			case "FLS_MAIL_MESSAGE_ITEM_FROM":
				this.notificationType = Icon_Type.FLS_MESSAGE_FROM.name();
				break;
			case "FLS_MAIL_MESSAGE_FRIEND_TO":
			case "FLS_MAIL_MESSAGE_ITEM_TO":
				this.notificationType = Icon_Type.FLS_MESSAGE_TO.name();
				break;
				
			default:
				this.notificationType = Icon_Type.FLS_DEFAULT.name();
				break;
		}
	}

}