package com.jason.server.util.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.jason.server.connector.MyServletRequest;
import com.jason.server.util.ByteHelper;
import com.jason.server.util.exception.InvalidRequestException;

/**
 * contain methods to parse http request 
 * @author lwz
 * @since 2016-7-28 
 */
public class HttpRequestUtil
{
	////////////static fields//////////////
	public final static String CONTENT_TYPE = "Content-Type";
	
	public final static String CONTENT_LENGTH = "Content-Length";
	
	public final static String COOKIE = "Cookie";
	
	public final static String HOST = "Host";
	
	//No instantiate
	private HttpRequestUtil(){}
	
	
	/**
	 * the private method for processor to parse 
	 * the request's request line and set the properties of HttpRequest object
	 * @param request the lower level encapsulation of http request
	 * @throws InvalidRequestException to close the socket
	 */
	public static void parseRequestLine(MyServletRequest request) throws InvalidRequestException
	{
		HttpRequestLine requestLine = new HttpRequestLine();
		SocketInputStream input = request.getSocketStream();
		try {
			//to deal with the InputStream's IOException
			input.readRequestLine(requestLine);
		} catch (IOException e) {
			try {
				input.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		/*
		 * tomcat put these fields in ByteBuffer(ByteChunk)
		 * and create a lot of String-like methods to deal with it.
		 * so that it reduce the chances of garbage-clean hence 
		 * improve the server's performance.
		 * Here I use String instead.
		 */
		String method = requestLine.method;
		String uri = requestLine.uri;
		String protocol = requestLine.protocol;
		
		//parse query parameters
		int question = uri.indexOf('?');
		if(question>=0) //   0: com?param...
		{
			request.setQueryString(uri.substring(question+1));
			uri = uri.substring(0,question);
		}
		else
		{
			request.setQueryString(null);
		}
		
		//parse jsessionid in the url, end with ;
		String match = ";jsession=";
		int semicolon = uri.indexOf(match);
		if(semicolon>=0)
		{
			int semicolon2 = uri.indexOf(';', semicolon+1);
			if(semicolon2>-1)//equals0 would be wrong
			{
				request.setRequestSessionId(uri.substring(semicolon+match.length(),semicolon2));
				uri = uri.substring(0, semicolon)+uri.substring(semicolon2);
			}
			else//the rest of the String is session id
			{
				request.setRequestSessionId(uri.substring(semicolon+match.length()));
				uri = uri.substring(0, semicolon);
			}
		}
		else//no session id
		{
			request.setRequestSessionId(null);
			request.setRequestedSessionIdFromURL(false);
		}
		request.setMethod(method);
		request.setProtocol(protocol);
		request.setRequestURI(uri);
	}
	
    /**
	 *  parse the request's header and put them in request
	 *  (including cookies)
	 *  Also,it needs to parse those headers which ServletRequest
	 * @param request the lower level encapsulation of http request
     * @throws InvalidRequestException 
	 */
	public static void parseHeaders(MyServletRequest request) throws InvalidRequestException
	{
		/*
		 *  Tomcat's codes which deals with request's headers & cookies are far too complicated..
		 *  Do it in a simpler way to deal with the most common headers.
		 *  Other throws an exception.
		 */
		SocketInputStream input = request.getSocketStream();
		List<String> rawHeaders = input.readHeaders();
		for(String str : rawHeaders)
		{
			int i = str.indexOf(':');
			String name = str.substring(0, i);
			String value = str.substring(i+2);
			if(name.equals(CONTENT_TYPE))//will need to get character encoding from content-type further
			{
				request.setContentType(value);
			}
			else if(name.equals(CONTENT_LENGTH))
			{
				request.setContentLength(Long.valueOf(value));
			}
			else if(name.equals(COOKIE))
			{
				parseCookies(value,request);
			}
			else if(name.equals(HOST))
			{
				String[] target = value.split(":");
				if(target.length!=2)
				{
					throw new InvalidRequestException();
				}
				request.setServerName(target[0]);
				request.setServerPort(Integer.valueOf(target[1]));
			}
			else
			{
				for(String v : value.split(","))
				{
					request.setHeader(name, v);
				}
			}
		}
	}
	
	/**
	 * parse the cookies.
	 * @param value the String of value of  "Cookie" Header
	 * @param request the lower level encapsulation of http request
	 */
	public static void parseCookies(String value,MyServletRequest request)
	{
		byte[] temp = value.getBytes(request.getSocketStream().getDefaultCharset());
		int semicolon = -1;
		int equal = -1;
		int begin = 0;
		while(begin<temp.length)
		{//ignore case with temp[0] = '='
			equal = ByteHelper.indexOf(temp, ByteHelper.EQUAL, begin);
			if(equal<0)
			{
				break;//parsing end
			}
			semicolon = ByteHelper.indexOf(temp, ByteHelper.SEMICOLON, equal+1);
			if(semicolon<0)
			{
				break;//end
			}
			request.setCookie(new String(temp,begin,equal-begin,StandardCharsets.ISO_8859_1),
					new String(temp,equal+1,semicolon-equal-1,StandardCharsets.ISO_8859_1));
			begin = semicolon+1;
		}
	}
}
