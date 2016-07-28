package com.jason.server.util.http;

import java.io.IOException;
import java.io.OutputStream;

import com.jason.server.connector.MyServletRequest;
import com.jason.server.util.exception.InvalidRequestException;

/**
 * contain methods to parse http request 
 * @author lwz
 * @since 2016-7-28 
 */
public class HttpRequestUtil
{
	////////////static fields//////////////
	private final static String contentType = "Content-Type";
	
	private final static String contentLength = "Content-Length";
	
	/**
	 * the private method for processor to parse 
	 * the request's request line and set the properties of HttpRequest object
	 * @param input SocketInputStream wrapping socket's inputstream
	 * @param output 
	 * @throws InvalidRequestException to close the socket
	 */
	public static void parseRequestLine(SocketInputStream input,OutputStream output,MyServletRequest request) throws InvalidRequestException
	{
		HttpRequestLine requestLine = new HttpRequestLine();
		try {//to deal with the InputStream's IOException
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
	 * parse the request's header and put them in request
	 * (including cookies)
	 * @param input
	 * @param request 
	 */
	public static void parseHeaders(SocketInputStream input,MyServletRequest request)
	{
		/*
		 *  Tomcat's code which deals with request's headers & cookies is far too complicated..
		 *  Do it in a simpler way to deal with the most common headers.
		 *  Others throw an exception.
		 */
	}
}
