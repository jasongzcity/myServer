package com.jason.server.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.HttpProcessor;
import com.jason.server.connector.HttpProcessor.ActionCode;
import com.jason.server.connector.MyServletRequest;
import com.jason.server.connector.MyServletResponse;
import com.jason.server.connector.OutputBuffer;
import com.jason.server.util.exception.ExceptionUtils;

/**
 * 
 * @author Administrator
 *
 */
public class StaticResourceProcessor
{
	private static final Logger log = LogManager.getLogger(ServletProcessor.class);
	private static final String[] INDEXFILES = {"index.html", "index.htm"};
	private static final int BUFFER_SIZE = 8192;//buffer for file IO
	
	private HttpProcessor httpProcessor;//use for callback
	public HttpProcessor getHttpProcessor() { return httpProcessor; }
	public void setHttpProcessor(HttpProcessor httpProcessor) { 
		this.httpProcessor = httpProcessor; 
	}

	public StaticResourceProcessor(HttpProcessor httpProcessor)
	{
		this.setHttpProcessor(httpProcessor);
	}

	public void process(MyServletRequest request,MyServletResponse response)
	{
		String requestURI = request.getRequestURI();
		if(requestURI.contains(".."))//Invalid request for moving backwards in file system
		{
			response.setError(true);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			httpProcessor.action(ActionCode.COMMIT);
			return;
		}
		File target = new File(HttpServer.WEB_ROOT,request.getRequestURI());
		if(!target.exists())
		{
			response.setError(true);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
			httpProcessor.action(ActionCode.COMMIT);
			return;
		}
		if(target.isDirectory())
		{
			File temp = null;
			for(int i=0;i<INDEXFILES.length;i++)//Search for index
			{
				if((temp = new File(target,INDEXFILES[i])).exists())
				{
					target = temp;
				}
			}
			if(target.isDirectory())   //no available index for this directory
			{
				response.setError(true);
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				httpProcessor.action(ActionCode.COMMIT);
				return;
			}
			//handle target file later
		}
		else 										//request for file
		{
			if(!(requestURI.endsWith(".html")||requestURI.endsWith(".htm")
				||requestURI.endsWith(".css")||requestURI.endsWith(".js")||requestURI.endsWith(".txt")))//only support these files
			{
				response.setError(true);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				httpProcessor.action(ActionCode.COMMIT);
				return;
			}
		}
		String fileName = target.getName();
		String fileType = fileName.substring(fileName.lastIndexOf('.')+1);
		String text = "text/";
		//MimeType easy mapping
		if(fileType.equals("htm")){
			text+="html";
		}else if (fileType.equals("txt")){
			text += "plain";
		}else if (fileType.equals("js")){
			text = "application/x-javascript";
		}else{
			text += fileType;
		}
		
		response.setContentType(text);//NOTICE: charset should be specified by file itself
		response.setContentLengthLong(target.length());
		response.setStatus(HttpServletResponse.SC_OK);
		response.setBufferSize(((Long)target.length()).intValue());
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(target);
			OutputBuffer ob = response.getOutputBuffer();
			byte[] buffer = new byte[BUFFER_SIZE];
			int byteCount = 0;
			while((byteCount=fis.read(buffer,0,BUFFER_SIZE))!=-1)
			{
				ob.realWriteBytes(buffer,0,byteCount);
			}
		} catch(IOException ioe) {
			log.error("IOException while sending response",ioe);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				ExceptionUtils.swallowException(e);
			}
		}
	}
}