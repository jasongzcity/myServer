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
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.util.ParameterMap;
import com.jason.server.util.http.SocketInputStream;
import com.jason.server.util.http.URLDecoder;

/**
 * lower level implementation of HttpServletRequest
 * 
 * @author lwz
 * @since 2016-8-4 
 */
public final class MyServletRequest implements HttpServletRequest 
{
    private static final Logger log  = LogManager.getLogger(MyServletRequest.class);
    
    private Map<String,Object> headers = new HashMap<String,Object>();
    private Map<String,List<String>> parameterMap = new HashMap<>();
    private String method;
    private String protocol = "http";
    private String requestUri;
    private String queryString;
    private String requestSessionId;
    private ArrayList<Cookie> cookies = new ArrayList<>();//transform to array when user call getter
    private SocketInputStream input;//underlying socket's inputStream
    private boolean requestedSessionIdFromURL;
    private Map<String,Object> attributes = new HashMap<String,Object>();
    private String contentType;
    private Long contentLength = -1L;
    private String characterEncoding;//null if not parse,parse from content-type
    private int serverPort = 0;
    private String serverName;
    private boolean isParameterParsed;
	
    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }
    
    @Override
    public Object getAttribute(String arg0) {
        return attributes.get(arg0);
    }
    
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
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
        return null;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }
    
    @Override
    public String getLocalAddr() {
        return null;
    }
    
    @Override
    public String getLocalName() {
        return null;
    }
    
    @Override
    public int getLocalPort() {
        return 0;
    }
    
    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
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
        rs.setLockMode(false);//unlock
        for(String key:parameterMap.keySet())
        {
            List<String> list = parameterMap.get(key);
            arr = new String[list.size()];
            list.toArray(arr);
            rs.put(key, arr);
        }
        rs.setLockMode(true);
        return rs;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if(!isParameterParsed)
        {
            parseParameter();
        }
        return Collections.enumeration(parameterMap.keySet());
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
        return null;
    }
    
    @Override
    public String getRealPath(String arg0) {
        return null;
    }
    
    @Override
    public String getRemoteAddr() {
        return null;
    }
    
    @Override
    public String getRemoteHost() {
        return null;
    }
    
    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }
    
    @Override
    public String getScheme() {
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
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public boolean isSecure() {
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
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
            throws IllegalStateException {
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse arg0) throws IOException,
            ServletException {
        return false;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getContextPath() {
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
     * if the header cant be converted to a date,throw IllegalArgumentException.
     * return the milliseconds since 1970/1/1.
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
        //return new NameEnumerator(headers);
        return Collections.enumeration(headers.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getHeaders(String arg0)
    {
        Object obj = headers.get(arg0);
        if(obj==null)
        {
            return Collections.emptyEnumeration();
        }
        List<String> c  = new ArrayList<>();
        if(obj instanceof String)
        {
            c.add((String) obj);
        }
        else
        {
            c = (List<String>) obj;
        }
        return Collections.enumeration(c);
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
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
	}

    @Override
    public String getRequestedSessionId() {
        return requestSessionId;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
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
        return false;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return false;
    }

    @Override
    public void login(String arg0, String arg1) throws ServletException {
    
    }

    @Override
    public void logout() throws ServletException {
    
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
            throws IOException, ServletException {
        return null;
    }
	
	///////////setter//////////////
    public void setInputStream(SocketInputStream in) { this.input = in; }
    
    public void setQueryString(String queryString) { this.queryString = queryString; }
    
    public void setRequestSessionId(String sessionId) { this.requestSessionId = sessionId; }
    
    public void setRequestedSessionIdFromURL(boolean b) { this.requestedSessionIdFromURL = b; }
    
    public void setMethod(String method) { this.method = method; }
    
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public void setRequestURI(String uri) { this.requestUri = uri; }

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
	
    public void setContentLength(long length) { this.contentLength = length; }
    
    public void setContentType(String type) { this.contentType = type; }
    
    public void setServerName(String name) { this.serverName = name; }
    
    public void setServerPort(int port) { this.serverPort = port; }
    
    public SocketInputStream getSocketStream() { return input; }
	
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
    private boolean parseParameter()
    {
        //TODO:parse parameter from body 
        return parseParamFromQuery();
    }
	
    //parse the parameter from the request line
    //rewrote in:2016-9-12
    //return true if parse success
    //return false if the encoding is not supported
    private boolean parseParamFromQuery()
    {
        if(characterEncoding==null)
        {
            parseEncoding();
        }
        if(characterEncoding==null)
        {
            characterEncoding = StandardCharsets.UTF_8.name();//get One of the alias
        }
        if(queryString==null) {
            return false;
        }
        int length = queryString.length();
        int beginIndex = 0;
        int equalIndex = -1;
        String paramValue = null;
        String paramName = null;
        final char EQUALS = '=';
        final char AND = '&';
        while(beginIndex<length)
        {
            equalIndex = queryString.indexOf(EQUALS,beginIndex);
            if(equalIndex==-1)//queryString end
            {
                return true;
            }
            paramName = queryString.substring(beginIndex+1, equalIndex);//temp save param name.
            if(paramName.indexOf('%')!=-1)
            {
                try {
                    paramName = URLDecoder.decode(paramName, characterEncoding);
                } catch (UnsupportedEncodingException e) {
                    return false;//stop parsing
                }
            }
            beginIndex = queryString.indexOf(AND,equalIndex+1);
            if(beginIndex==-1)//queryString end
            {
                beginIndex = length;
            }
            paramValue = queryString.substring(equalIndex+1, beginIndex);
            if(paramValue.indexOf('%')!=-1)
            {
                try {
                    paramValue = URLDecoder.decode(paramValue, characterEncoding);
                } catch (UnsupportedEncodingException e) {
                    return false;
                }
            }
            setParameter(paramName,paramValue);
        }
        return true;
    }
	
    /*
     *  This method must be called after the ContentType
     *  has been set.
     */
    private void parseEncoding()
    {
        if(contentType==null)
        {
            return;
        }
        String cs = "charset=";
        int i = contentType.indexOf(cs);
        if(i<0)
        {
            return;
        }
        characterEncoding = contentType.substring(i+cs.length());
    }
}
