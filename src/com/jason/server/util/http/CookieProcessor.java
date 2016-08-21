package com.jason.server.util.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

/**
 * Implements the org.apache.tomcat.util.http.CookieProcessor
 * inspired by org.apache.tomcat.util.http.LegacyCookieProcessor
 * For compliance and easier implementation,
 * this processor only support V0 cookie.
 * 
 * Because of using static SimpleDateFormat, this class is not thread-safe.
 * 
 * @author lwz
 * @since 2016-8-18 
 * @see org.apache.tomcat.util.http.CookieProcessor
 * @see org.apache.tomcat.util.http.LegacyCookieProcessor
 */
public final class CookieProcessor 
{
	public static final String COOKIE_DATE_FORMAT = "EEE DD-MON-YYYY HH:mm:ss GMT";
	public static final SimpleDateFormat COOKIE_FORMAT = 
			new SimpleDateFormat(COOKIE_DATE_FORMAT,Locale.US); 
	
	//old date to notify the client to destroy the cookie immediately
	private static final String ANCIENT_DATE = COOKIE_FORMAT.format(new Date(10000));
	
	private static final Object DATEFORMATLOCK = new Object();//lock object for formatter
	
	private CookieProcessor(){}
	/**
	 * Method for request parsing.
	 * @param headers bytes read from HTTP header.After "Cookie="
	 * @param cookies list that contains parsed cookies.
	 */
	public static void parseCookieHeader(byte[] headers, List<Cookie> cookies)
	{
		//TODO: parsing....
	}
	
	/**
	 * Generate a line of  "Set-Cookie: " header
	 * Notice: servlet 3.0 use cookie names like RFC2965.
	 * This method will transform them to V0 cookie.
	 * @param cookie servlet 3.0 spec cookie
	 * @return String to write in response's header.
	 */
	public static String generateHeader(Cookie cookie)
	{
		StringBuilder str = new StringBuilder();
		String value = cookie.getValue();
		String name = cookie.getName();

		str.append(name);
		str.append("=");
		str.append(value);//no quote
		
		int expireSec = cookie.getMaxAge();//in second NOT millisecond! 
		if(expireSec>-1)
		{
			str.append("; Expires=");
			if(expireSec==0)//destroy immediately
			{
				str.append(ANCIENT_DATE);
			}
			else
			{
				synchronized(DATEFORMATLOCK)
				{
					str.append(COOKIE_FORMAT.format(new Date(System.currentTimeMillis()+1000L*expireSec)));
				}
			}
		}
		
		String path = cookie.getPath();
		if(path!=null)
		{
			str.append("; Path="+path);
		}
		
		String domain = cookie.getDomain();
		if(domain!=null)
		{
			str.append("; Domain="+domain);
		}
		
		if(cookie.getSecure())
		{
			str.append("; Secure");
		}
 
		return str.toString();
	}
}
