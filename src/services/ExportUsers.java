package services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.FlsExport;
import util.FlsLogger;
import util.FlsPlan;

@WebServlet(description = "Export Leads as CSV", urlPatterns = { "/ExportUsers" })
public class ExportUsers extends HttpServlet {
	
	private FlsLogger LOGGER = new FlsLogger(ExportUsers.class.getName());
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
		resp.setContentType("text/csv");
		LOGGER.info("Inside GET Method");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		LOGGER.info("Inside POST Method");
		
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"Users.csv\"");
		
		FlsExport exportUsers = new FlsExport();
		int verification = Integer.parseInt(request.getParameter("verification"));
		int liveStatus = Integer.parseInt(request.getParameter("liveStatus"));
		int userStatus = Integer.parseInt(request.getParameter("userStatus"));
		
        try {
        	
            OutputStream o = response.getOutputStream();
            String header = "SignUp Date,Fee Expiry Date,Id,UID,Profile Pic,Plan,Full Name,Mobile,Email,Secondary Id Status,Location,Sub Locality,Locality,Referral Code,Photo Id,Friend Referral Code,Notification,Credits,User Latitude,User Longitude,Verification,Status,SignUp Status,Item Count,Lease Count,Response Time,Response Count,About,Website,Business Email,Business Number,Business Hours\n";
            o.write(header.getBytes());
            
            StringBuffer line = new StringBuffer();
            line = exportUsers.getUsers(verification,liveStatus,userStatus);
            if(line!=null){
            	 o.write(line.toString().getBytes());
            }
            
            o.flush();
            o.close();
            LOGGER.info("Finished Generating CSV file");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
