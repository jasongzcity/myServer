package com.jason.server.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.jason.server.util.ByteHelper;
import com.jason.server.util.ParameterMap;
import com.jason.server.util.http.SocketInputStream;

/**
 * simple implementation of HttpServletRequest
 * 
 * @author lwz
 * @since 2016-8-4 
 */
public class MyServletRequest implements HttpServletRequest 
{
	protected Map<String,Object> headers = new HashMap<String,Object>();
	protected Map<String,List<String>> parameterMap = new HashMap<>();
	protected String method;
	protected String protocol;
	protected String requestUri;
	protected String queryString;
	protected String requestSessionId;
	protected ArrayList<Cookie> cookies = new ArrayList<>();//transform to array when user call getter
	protected SocketInputStream input;//underlying socket's inputStream
	protected boolean requestedSessionIdFromURL;
	protected Map<String,Object> attributes = new HashMap<String,Object>();
	protected String contentType;
	protected Long contentLength = -1L;
	protected String characterEncoding;//null if not parse,parse from content-type
	protected int serverPort = 0;
	protected String serverName;
	protected boolean isParameterParsed;
	
	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new AttributeNameEnum(attributes);
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public int getContentLength() {
		if(contentLength>Integer.MAX_VALUE)
		{
			return -1;
		}
		return contentLength.intValue();
	}

	@Override
	public long getContentLengthLong() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		if(!isParameterParsed)
		{
			parseParameter();
		}
		List<String> rs = parameterMap.get(arg0);
		if(rs!=null)
		{
			return rs.get(0);
		}
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		if(!isParameterParsed)
		{
			parseParameter();
		}
		String[] arr = null;
		ParameterMap<String, String[]> rs = new ParameterMap<>();
		rs.setLockMode(true);
		for(String key:parameterMap.keySet())
		{
			List<String> list = parameterMap.get(key);
			arr = new String[list.size()];
			list.toArray(arr);
			rs.put(key, arr);
		}
		rs.setLockMode(false);
		return rs;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		if(!isParameterParsed)
		{
			parseParameter();
		}
		return new ParameterNameEnum(parameterMap);
	}

	@Override
	public String[] getParameterValues(String arg0) {
		if(!isParameterParsed)
		{
			parseParameter();
		}
		if(!parameterMap.containsKey(arg0))
		{
			return null;
		}
		return (String[])parameterMap.get(arg0).toArray();
		
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException 
	{
		try
		{
			Charset.forName(arg0);	//check if legal
		}
		catch(UnsupportedCharsetException e)
		{
			throw new UnsupportedEncodingException(arg0);
		}
		this.characterEncoding = arg0;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() 
	{
		if(cookies.size()!=0)
		{
			Cookie[] arr = new Cookie[cookies.size()];
			for(int i=0;i<arr.length;i++)
			{
				arr[i] = cookies.get(i);
			}
			return arr;
		}
		else
		{
			return null;
		}
	}

	/**
	 * implements of HttpServletRequest
	 * @param arg0 the name of the header
	 * @return if it is not exists of this header name, return null.
	 * 			if the header cant be converted to a date,throw IllegalArgumentException.
	 * 			return the milliseconds since 1970/1/1.
	 */
	@Override
	public long getDateHeader(String arg0) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.ENGLISH);
		String target = getHeader(arg0);
		if(target==null)
		{
			return -1L;
		}
		try
		{
			Date date = sdf.parse(target);
			if(date != null)//been parse
			{
				return date.getTime();
			}
			else
			{
				throw new IllegalArgumentException("can't parse the header as date");
			}
		} 
		catch (ParseException e) 
		{
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getHeader(String arg0)
	{
		Object obj = headers.get(arg0);
		if(obj!=null)
		{
			if(obj instanceof String)
			{
				return (String)obj;
			}
			else
			{
				return ((List<String>)obj).get(0);
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public Enumeration<String> getHeaderNames() 
	{
		return new NameEnumerator(headers);
	}

	@Override
	public Enumeration<String> getHeaders(String arg0)
	{
		return new ValueEnumerator(headers,arg0);
	}

	@Override
	public int getIntHeader(String arg0) 
	{
		String tmp = getHeader(arg0);
		if(tmp==null)
		{
			return -1;
		}
		return Integer.parseInt(tmp);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString()
	{
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() 
	{
		return requestUri;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestSessionId;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return requestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		//Deprecated!!!!
		return requestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	
	///////////setter//////////////
	public void setInputStream(SocketInputStream in)
	{
		this.input = in;
	}
	public void setQueryString(String queryString)
	{
		this.queryString = queryString;
	}
	public void setRequestSessionId(String sessionId)
	{
		this.requestSessionId = sessionId;
	}
	public void setRequestedSessionIdFromURL(boolean b)
	{
		this.requestedSessionIdFromURL = b;
	}
	public void setMethod(String method)
	{
		this.method = method;
	}
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}
	public void setRequestURI(String uri)
	{
		this.requestUri = uri;
	}

	@SuppressWarnings("unchecked")
	public void setHeader(String name,String value)
	{
		if(headers.containsKey(name))//this seldom happens
		{
			Object tmp = headers.get(name);
			if(tmp instanceof String)					
			{
				List<String> list = new ArrayList<>(2);
				list.add((String)tmp);
				list.add(value);
				headers.put(name, list);
			}
			else
			{
				List<String> list = (List<String>)tmp;
				list.add(value);
			}
		}
		else
		{
			headers.put(name, value);
		}
	}
	
	public void setCookie(String name,String value)
	{
		Cookie cookie = new Cookie(name, value);
		cookies.add(cookie);
	}
	
	public void setContentLength(long length)
	{
		this.contentLength = length;
	}
	
	public void setContentType(String type)
	{
		this.contentType = type;
	}
	
	public void setServerName(String name)
	{
		this.serverName = name;
	}
	public void setServerPort(int port)
	{
		this.serverPort = port;
	}
	
	public SocketInputStream getSocketStream()
	{
		return input;
	}
	public void setParameter(String key,String value)
	{
		List<String> list = parameterMap.get(key);
		if(list!=null)
		{
			list.add(value);
		}
		else
		{
			list = new ArrayList<String>();//default capacity
			list.add(value);
			parameterMap.put(key, list);
		}
	}
	///////////private method////////////
	private void parseParameter()
	{
		//TODO:parse parameter from body -- read protocol....
		parseParamFromQuery();
	}
	
	private void parseParamFromQuery()
	{
		if(characterEncoding==null)
		{
			parseEncoding();
		}
		if(characterEncoding==null)
		{
			characterEncoding = StandardCharsets.UTF_8.aliases().iterator().next();//get One of the alias
		}
		byte[] temp = queryString.getBytes(StandardCharsets.ISO_8859_1);//Http Standard Charsets
		//may need to parse parameters using header request charset instead of ISO
		int begin = 0;
		int and = -1;
		int equal = -1;
		while(begin<temp.length)
		{
			equal = ByteHelper.indexOf(temp, ByteHelper.EQUAL, begin);
			if(equal<0)
			{
				break;
			}
			and = ByteHelper.indexOf(temp, ByteHelper.AND, equal+1);
			if(and<0)
			{
				break;
			}
		}
	}
	
	private void parseEncoding()
	{
		String cs = "charset=";
		int i = contentType.indexOf(cs);
		if(i<0)
		{
			return;
		}
		characterEncoding = contentType.substring(i+cs.length());
	}
	
	//////////////////package-own classes///////////////////
	
	class NameEnumerator implements Enumeration<String>
	{
		Iterator<String> iter;//use iterator to realize
		NameEnumerator(Map<String,Object> map)
		{
			iter = map.keySet().iterator();
		}
		@Override
		public boolean hasMoreElements()
		{
			return iter.hasNext();
		}

		@Override
		public String nextElement()
		{
			return iter.next();
		}
	}
	
	class ValueEnumerator implements Enumeration<String>
	{
		Iterator<String> iter;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ValueEnumerator(Map<String,Object> map,String name)
		{
			Object tmp = map.get(name);
			if(tmp!=null)
			{
				if(tmp instanceof List)
				{
					iter = ((List)tmp).iterator();
				}
				else
				{
					List<String> list = new ArrayList<>(1);
					list.add(name);
					iter = list.iterator();
				}
			}
			else//return a empty iterator
			{
				iter = new Iterator<String>(){

					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public String next() {
						return null;
					}	
				};
			}
		}
		@Override
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		@Override
		public String nextElement() {
			return iter.next();
		}
	}
	
	class ParameterNameEnum implements Enumeration<String>
	{
		Iterator<String> iter;
		ParameterNameEnum(Map<String,List<String>> parMap)
		{
			iter = parMap.keySet().iterator();
		}
		@Override
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		@Override
		public String nextElement() {
			return iter.next();
		}
		
	}
	
	class AttributeNameEnum implements Enumeration<String>
	{
		Iterator<String> iter;
		
		AttributeNameEnum(Map<String,Object> map)
		{
			iter = map.keySet().iterator();
		}
		@Override
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		@Override
		public String nextElement() {
			return iter.next();
		}
		
	}
}
