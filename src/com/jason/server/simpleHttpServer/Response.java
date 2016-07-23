package com.jason.server.simpleHttpServer;

import java.io.*;

/**
  * this class encapsulates the socket's OutputStream 
  * can send response to the client
  * @author Jason
  */
public class Response
{
	private static int BUFFER_SIZE = 1024;
	
	private Request request = null;
	private OutputStream out = null;
	
	public Response(OutputStream out)
	{
		this.out = out;
	}
	
	public void setRequest(Request request)
	{
		this.request = request;
	}
	
	public void sendStaticResource() throws IOException
	{
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis = null;
		try
		{
			File file = new File(HttpServer.WEB_ROOT,request.getUri());
			if(file.exists())
			{
				//if exists then send the byte buffer to the socket
				fis = new FileInputStream(file);
				int count = fis.read(bytes,0,BUFFER_SIZE);
				while(count!=-1)
				{
					out.write(bytes,0,count);
					count = fis.read(bytes,0,BUFFER_SIZE);
				}
			}
			else
			{
				//write file not found
				String error = "HTTP/1.1 404 File Not Found\r\n" +
				"Content-Type: text/html\r\n" + "Content-Length:23\r\n"+
				"\r\n"+
				"<h1>File Not Fount</h1>>";
				out.write(error.getBytes());
			}
		}//try
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fis!=null)
			{
				fis.close();
			}
		}
	}
}
				