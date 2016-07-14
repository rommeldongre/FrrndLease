package services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONObject;

import pojos.GetCreditTimelineListResObj;
import pojos.GetCreditTimelineReqObj;
import pojos.GetCreditTimelineResObj;
import util.FlsLogger;
import app.GetCreditTimelineHandler;
import app.NotImplementedException;


/**
 * Servlet implementation class  GetCreditTimeline
 */
@WebServlet(description = "Get Credit History for a given UserId", urlPatterns = { "/GetCreditTimeline" })
public class  GetCreditTimeline extends HttpServlet {
	
	private FlsLogger LOGGER = new FlsLogger( GetCreditTimeline.class.getName());
	
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
			LOGGER.info("Inside POST method");
			//HTTP request to Service request pojo 
			ObjectMapper mapper = new ObjectMapper();
			GetCreditTimelineReqObj request = mapper.readValue(httprequest.getInputStream(),  GetCreditTimelineReqObj.class);
			httpresponse.setContentType("application/json");
			
			// application logic comes here --------		
			 GetCreditTimelineListResObj Response = null;
		
			try {
				//App handler to process request and create Service response pojo
				Response = ( GetCreditTimelineListResObj)  GetCreditTimelineHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");	
				mapper.writeValue(out, Response);
				LOGGER.info("Finished POST method");
					
			} catch (NotImplementedException e) {
				e.printStackTrace();
				LOGGER.warning("GetCreditTimeline process method not implemented");
				httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetCreditTimeline process method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
}