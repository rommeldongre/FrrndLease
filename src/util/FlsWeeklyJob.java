package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import util.Event.Event_Type;
import util.Event.Notification_Type;
import connect.Connect;

public class FlsWeeklyJob extends Connect implements org.quartz.Job{

	private FlsLogger LOGGER = new FlsLogger(FlsWeeklyJob.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
		LOGGER.warning("Its Saturday 11AM, Starting FlsWeeklyJob...");
		checkIdProof();
	}

    private void checkIdProof(){
		
		LOGGER.info("Send reminder to upload Id Proof");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null,rs2=null;
		
		try {
			
			String sqlSelectUsersToRemind = "SELECT * FROM `users` WHERE user_photo_id IS NULL";
			ps1 = hcp.prepareStatement(sqlSelectUsersToRemind);
			rs1 = ps1.executeQuery();
			
			String sqlSelectPrimePlaces = "SELECT * FROM `places`";
			ps2 = hcp.prepareStatement(sqlSelectPrimePlaces);
			rs2 = ps2.executeQuery();
			
			ArrayList<String> places =new ArrayList<String>();
			
			while(rs2.next()){
				places.add(rs2.getString("locality"));
			}
			
			while(rs1.next()){
				LOGGER.info("Sending a reminder to the user about uploading photo id");
				if(places.contains(rs1.getString("user_locality"))){
					try {
						Event event = new Event();
						event.createEvent(rs1.getString("user_id"), rs1.getString("user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_REMIND_PHOTO_ID, 0, "You have not uploaded your valid photo id. Please upload it to enable paid delivery service");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}catch(SQLException e){
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		}catch(Exception e){
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		}finally{
			try{
				if(rs1 != null) rs1.close();
				if(rs2 != null) rs2.close();
				if(ps1 != null) ps1.close();
				if(ps2 != null) ps2.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
