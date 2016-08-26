package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.util.exception.ExceptionUtils;
import com.jason.server.util.http.SocketInputStream;

/**
  * This processor is responsible for process socket and handle 
  * streams and lower level request & response to adapter.
  * @see tomcat's socketProcessor
  * @see tomcat's httpProcessor
  * @author lwz
  * @since JDK1.8
  */
public class HttpProcessor
{	
	private static final Logger log = LogManager.getLogger(HttpProcessor.class);
	
	//////// fields/////////
	private HttpConnector connector;
	protected MyServletRequest request;
	protected MyServletResponse response;
	protected MyAdapter adapter;
	public void setAdapter(MyAdapter adapter){ this.adapter = adapter; }
	public MyAdapter getAdapter(){ return adapter; }
	
	/**
	 * Constructor.
	 * Wrap the connector
	 * @param conn
	 */
	public HttpProcessor(HttpConnector conn)
	{
		this.connector = conn;
		adapter = new MyAdapter(this);
	}
	
	public HttpProcessor(){}
	
	/**
	 * process the socket 
	 * @param socket
	 */
	public void process(Socket socket)
	{
		//TODO: use socket wrapper,to screen lower level detail
		SocketInputStream input  = null;
		OutputStream output = null;
		try
		{
			input = new SocketInputStream(socket.getInputStream(),2048);
			output = socket.getOutputStream();
			request = new  MyServletRequest();
			response = new MyServletResponse(output);

			request.setInputStream(input);
			response.setRequest(request);
			
			adapter.service(request, response);
		}
		catch(IOException e)
		{
			log.error("IO Error while setting up request",e);
			try {
				socket.close();
			} catch (IOException e1) {
				ExceptionUtils.swallowException(e1);
			}
		}
	}
	
	//------simple hook mechanism------//
	
	/**
	 * hook method for child processors to call
	 * Note: status code should be set in response object
	 * @param actionCode action command
	 */
	public void action(ActionCode actionCode)
	{
		switch(actionCode)
		{
			case COMMIT:{
				response.commit();
			}
		}
	}
	
	public static enum ActionCode
	{
		COMMIT;
	}
}
			
			