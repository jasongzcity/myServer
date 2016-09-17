package com.jason.server.util.http;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger log = LogManager.getLogger(HttpRequestUtil.class);
    
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
        SocketInputStream input = request.getSocketStream();
        input.readRequestLine(request);
		
        /*
         * tomcat put these fields in ByteBuffer(ByteChunk)
         * and create a lot of String-like methods to deal with it.
         * so that it reduce the chances of garbage-clean hence 
         * improve the server's performance.
         * Here I use String instead.
         */
        String uri = request.getRequestURI();//It's raw,parse it
		
        if(uri.contains("http"))//full URL
        {
            uri = uri.substring(uri.lastIndexOf('/'));
        }
        else 										// string like: www.sample.com/URI.... or /URI.....
        {
            uri = uri.substring(uri.indexOf('/'));
        }
		
        //parse query parameters
        int question = uri.indexOf('?');
        if(question>=0) 
        {
            String queryString = uri.substring(question+1);
            request.setQueryString(queryString);
            //parseQueryString(queryString,request);
            uri = uri.substring(0,question);//ignore '?'
        }
        else
        {
            request.setQueryString(null);
        }
		
        //parse jsessionid in the URL, end with ;
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
        else//no URL-encoded session id
        {
            request.setRequestSessionId(null);
            request.setRequestedSessionIdFromURL(false);
        }
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
            String value = str.substring(i+2);//skip space
            if(name.equals(CONTENT_TYPE))//need to get character encoding from content-type
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
                if(value.contains(":"))
                {
                    String[] target = value.split(":");
                    request.setServerPort(Integer.valueOf(target[1]));
                    request.setServerName(target[0]);
                }
                else
                {
                    request.setServerName(value);
                }
                request.setHeader("Host", value);
            }
            else
            {
                request.setHeader(name, value);
            }
        }
        if(request.getHeader("Host")==null)
        {
            throw new InvalidRequestException("request doesn't contain Host header");
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
