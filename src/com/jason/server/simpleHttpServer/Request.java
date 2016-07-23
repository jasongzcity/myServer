package com.jason.server.simpleHttpServer;

import java.io.*;

/**
  * the server encapsulate the client's request into the Request
  * and the Request parse the content of every request
  * @author Jason 
  */
  
  
public class Request
{
	private String uri;
	private InputStream in;
	
	//the only constructor
	public Request(InputStream in)
	{
		this.in = in;
	}
	
	/**
	  * @return the request's target URI
	  */
	public String getUri()
	{
		return uri;
	}
	
	/**
	  * parse the request
	  */
	public void parse()
	{
		StringBuffer sb = new StringBuffer(8096);
		int i = 0;
		byte[] buffer = new byte[2048];
		try
		{
			i = in.read(buffer);	//read the byte into buffer,i represents the bytes have been written
		}
		catch(Exception e)
		{
			e.printStackTrace();
			i = -1;
		}
		for(int j=0;j<i;j++)
		{
			sb.append((char)buffer[j]);//transform byte into char
		}
		System.out.println(sb.toString());//print request in console
		uri = parseUri(sb.toString());
	}
	
	//the 'parse'method deliver the request String here
	private String parseUri(String requestString)
	{
		//could find the String using regex later...
		//this method seems awkward
		int index1 = 0,index2 = 0;
		index1 = requestString.indexOf(" ");
		if(index1!=-1)
		{
			index2 = requestString.indexOf(" ",index1+1);
			if(index2>index1)
			{
				return requestString.substring(index1+1,index2);
			}
		}
		return null;
	}
}
	
  	