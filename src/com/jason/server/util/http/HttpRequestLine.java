package com.jason.server.util.http;

/**
 * this class stores the received http request line's property
 * @author lwz
 * @since 2016-7-26
 */
public class HttpRequestLine 
{
	private String wholeLine;
		
	public String method;
	public String protocol;
	public String uri;
	
	public void setWholeLine(String str)
	{
		this.wholeLine = str;
	}
	
	public String getWholeLine()
	{
		return wholeLine;
	}
	
	public int indexOf(int ch)
	{
		return wholeLine.indexOf(ch);
	}
}
