package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.util.ByteHelper;
import com.jason.server.util.HTMLHelper;
import com.jason.server.util.exception.InvalidResponseException;
import com.jason.server.util.http.CookieProcessor;
import com.jason.server.util.http.DateFormatter;

/**
 * lower level implementation of HttpServletResponse
 * @author lwz
 * @since 2016-8-5
 * TODO: store headers in Chunks 
 */
public final class MyServletResponse implements HttpServletResponse 
{
    private static final Logger log  = LogManager.getLogger(MyServletResponse.class);
    
    //static standard charset
    public static final Charset CS_USCII = StandardCharsets.US_ASCII; 
    
    private String characterEncoding;
    private boolean isCharsetSet;//charset explicitly setted
    private boolean isCommited;
    private String contentType;
    private WriteStatus writeStatus = WriteStatus.NOTUSED;
    //protected OutputStream out;//No direct operation
    private ServletOutputStream sout;
    private PrintWriter writer;
    private Locale locale;
    private Long contentLength = -1L;
    private int status = -1; //status code
    //According to RFC 7230 client should ignore reason phrase
    
    private OutputBuffer ob;// gives direct control of buffer
    // also,wrap in ServletOutputStream or PrintWriter for 
    // abstract read/write operation on http body
    public OutputBuffer getOutputBuffer(){ return ob; }
    
    private List<Cookie> cookies = new ArrayList<>();
    private Map<String,Object> headers  = new HashMap<>();
    private String protocol = "HTTP/1.1"; //default protocol
    private boolean isError;
    private boolean isRedirect;
    private String redirectURL;//should be absolute URL 
    private MyServletRequest request;//lower level request
    
    public MyServletResponse(OutputStream out)
    {
        this.ob = new OutputBuffer(out,this);//Setup OutputBuffer
    }
    
    /**
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        commit();
    }

    /**
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    @Override
    public int getBufferSize() {
        return ob.capacity();
    }
    
    /**
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        if(characterEncoding==null)//Not set
        {
            return StandardCharsets.ISO_8859_1.name();
        }
        return characterEncoding;
    }

    /**
     * @see javax.servlet.ServletResponse#getContentType()
     */
    @Override
    public String getContentType() {
        return contentType;
    }
    
    /**
     * @see javax.servlet.ServletResponse#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * this ServletOutputStream writes raw bytes as Servlet protocol required.
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(writeStatus==WriteStatus.USINGWRITER)
        {
            throw new IllegalStateException();
        }
        if(writeStatus==WriteStatus.USINGOUTPUT)
        {
            return sout;
        }
        //writeStatus == NONE
        sout = new MyServletOutputStream(ob);
        writeStatus = WriteStatus.USINGOUTPUT;
        prepareCharset();//making sure response has a charsetEncoding 
        									   //for the body.Parse from ContentType or default UTF-8.
        return sout;
    }

    /**
     * write
     * @see javax.servlet.ServletResponse#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException
    {
        if(writeStatus==WriteStatus.USINGWRITER)
        {
            return writer;
        }
        if(writeStatus==WriteStatus.USINGOUTPUT)
        {
            throw new IllegalStateException();
        }
        writer = new MyServletWriter(ob);
        writeStatus = WriteStatus.USINGWRITER;
        prepareCharset();//making sure response has a charsetEncoding 
        									   //for the body.Parse from ContentType or default UTF-8.
        return writer;
    }
    
    public void setCommited(boolean b)
    {
        this.isCommited = b;
    }
	
    /**
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    @Override
    public boolean isCommitted() {
        return isCommited;
    }
    
    /**
     * @see javax.servlet.ServletResponse#reset()
     */
    @Override
    public void reset() {
        if(isCommited)
        {
            throw new IllegalStateException();
        }
        status = -1;
        headers.clear();
        ob.resetBuffer();
    }

    /**
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        if(isCommited)
        {
            throw new IllegalStateException();
        }
        ob.resetBuffer();
    }

    /**
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int arg0) 
    {
        if(ob.isBufferWritten()||isCommited)
        {
            throw new IllegalStateException();
        }
        ob.setBufferSize(arg0);
    }

    /**
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String arg0) 
    {
        if(writeStatus!=WriteStatus.NOTUSED||isCommited)//writting has begun,unable to change character encoding
        {
            return;
        }
        characterEncoding = arg0;
        setCharsetSet(true);
    }

    /**
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    @Override
    public void setContentLength(int arg0) 
    {	
        contentLength = (long) arg0;
    }
    
    /**
     * @see javax.servlet.ServletResponse#setContentLengthLong(long)
     */
    @Override
    public void setContentLengthLong(long arg0) 
    {
        contentLength = arg0;
    }

    /**
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String arg0) {
        this.contentType = arg0;//add "charset" at the point of sendHeader 
    }
    
    /**
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale arg0) {
        this.locale = arg0;
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie arg0) {
        if(isCommited)
        {
            return;
        }
        cookies.add(arg0);//bytes will be generated in #sendHeaders
    }
	
    /**
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String arg0, long arg1) 
    {
        String date = DateFormatter.formatDate(arg1,null);//using standard date formatter
        addHeader(arg0,date);
    }

    /**
     * Note: according to servlet API, {@link #addHeader(String, String)}should not overwrite
     * the previous datas, while {@link #setHeader(String, String)} should overwrite the previous datas.
     * 
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     * @see #setHeader(String, String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addHeader(String arg0, String arg1) {
        if(isCommited)
        {
            return;
        }
        if(headers.containsKey(arg0))
        {
            Object tmp = headers.get(arg0);
            if(tmp instanceof String)//transfer String to List
            {
                List<String> list = new ArrayList<>();
                list.add((String) tmp);
                list.add(arg1);
                headers.put(arg0, list);
            }
            else if(tmp instanceof List)	//Already a list
            {
                ((List<String>)tmp).add(arg1);
            }
            else
            {
                throw new IllegalStateException("Cant add headers with different types");
            }
        }
        else
        {
            headers.put(arg0, arg1);
        }
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String arg0, int arg1) {
        String intValue = String.valueOf(arg1);
        addHeader(arg0,intValue);
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String arg0) {
        return headers.containsKey(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    @Override
    public String encodeRedirectURL(String arg0) {
    	//TODO: implement session mechanism for this server
        return arg0;
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     */
    @Deprecated
    @Override
    public String encodeRedirectUrl(String arg0) {
        return encodeRedirectURL(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    @Override
    public String encodeURL(String arg0) {
        //TODO: implement session mechanism for this server
        return arg0;
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    @Deprecated
    @Override
    public String encodeUrl(String arg0) {
        return encodeURL(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getHeader(String arg0) {
        Object tmp = headers.get(arg0);
        String rs = null;
        if(tmp instanceof List)
        {
            rs = ((List<String>)tmp).get(0);
        }
        else
        {
            rs = (String)tmp;
        }
        return rs;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
     */
    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getHeaders(String arg0) {
        Object tmp = headers.get(arg0);
        if(tmp instanceof List)
        {
            return (Collection<String>)tmp;
        }
        List<String> list = new ArrayList<>();
        list.add((String)tmp);
        return list;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#getStatus()
     * @return the status code. -1 if not set. 
     */
    @Override
    public int getStatus() {
        return status;
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    @Override
    public void sendError(int arg0) throws IOException {
        sendError(arg0,null);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int arg0, String arg1) throws IOException,IllegalStateException {
        if(isCommited)
        {
            throw new IllegalStateException();
        }
        resetBuffer();//clearing buffer but keep headers and cookies
        setError(true);//using error page.
        setStatus(arg0);
        commit();
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        this.redirectURL = location;
        setRedirect(true);
        commit();
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String arg0, long arg1) {
        String date = DateFormatter.formatDate(arg1, null);//using standard date formatter
        setHeader(arg0,date);
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String arg0, String arg1) {
        headers.put(arg0, arg1);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String arg0, int arg1) {
        headers.put(arg0, String.valueOf(arg1));
    }
    
    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    @Override
    public void setStatus(int arg0) {
        this.status = arg0;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     */
    @Override
    public void setStatus(int arg0, String arg1) {
        //Deprecated
    }
    
    public boolean isCharsetSet() {
        return isCharsetSet;
    }
    
    public void setCharsetSet(boolean isCharsetSet) {
        this.isCharsetSet = isCharsetSet;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isError() {
        return isError;
    }
    
    public void setError(boolean isError) {
        this.isError = isError;
    }
    
    public boolean isRedirect() {
        return isRedirect;
    }
    
    public void setRedirect(boolean isRedirect) {
        this.isRedirect = isRedirect;
    }
    
    public void setRequest(MyServletRequest request) {
        this.request = request;
    }
	
	public MyServletRequest getRequest() {
	    return this.request;
	}

	//-----------send methods------------//

    /**
     * convenience method for static resource processor
     * @throws InvalidResponseException 
     */
    public void flushHeaderOnly() throws InvalidResponseException
    {
        sendFirstLine();
        sendHeaders();
    }
	
    /**
     * entrance to end this reponse. 
     */
    public void commit()
    {
        try {
            writeSpecialBody();//Currently error
            sendFirstLine();
            sendHeaders();
            ob.flushBody();
        } catch (InvalidResponseException e) {
            log.error("Invalid reponse",e);
        } catch (IOException IOE){
            log.error("IO Error while sending response",IOE);
    	}
        ob.close();
        setCommited(true);
    }
	
    private void sendFirstLine() throws InvalidResponseException
    {
        if(status>599||status<100)
        {
            throw new InvalidResponseException();
        }
        ob.realWriteBytes(protocol.getBytes(CS_USCII));
        ob.realWriteSpace();
        ob.realWriteBytes(String.valueOf(status).getBytes(CS_USCII));//writing status code
        ob.realWriteSpace();
        
        //ignore useless reason phrase here on purpose
        
        ob.realWriteCRLF();
    }
	
    /*
     * this method is responsible for parsing Strings 
     * and write bytes to OutputBuffer.
     * send headers & cookies
     */
    @SuppressWarnings("unchecked")
    private void sendHeaders() throws InvalidResponseException
    {
        checkSpecialHeader();//check headers before send
        for(Entry<String,Object> node:headers.entrySet())
        {
            ob.realWriteBytes(node.getKey().getBytes(CS_USCII));
            ob.realWriteByte(ByteHelper.COLON);
            ob.realWriteByte(ByteHelper.SPACE);
            Object value = node.getValue();
            if(value instanceof String)
            {
                ob.realWriteBytes(((String) value).getBytes(CS_USCII));
            }
            else if(value instanceof List)
            {
                List<String> list = (List<String>)value;
                for(int i=0;i<list.size();i++)
                {
                    ob.realWriteBytes(list.get(i).getBytes(CS_USCII));
                    if(i<list.size()-1)
                    {
                        ob.realWriteByte(ByteHelper.COMMA);
                        ob.realWriteByte(ByteHelper.SPACE);
                    }
                }
            }
            ob.realWriteCRLF();
        }
        sendCookies();
        ob.realWriteCRLF();//line that separates headers and body 
    }
	
    //check if any important header missing
    //for now: Content-Type,Content-Length,
    //Server and Date.Also,consider the headers when sending 
    //ERROR or REDIRECT
    private void checkSpecialHeader() throws InvalidResponseException
    {
        if(isError)//Sending error 
        {
            setHeader("Content-Type","text/html; charset=utf-8");//default error page format utf-8
        }
        else if(isRedirect)//No content
        {
            setHeader("Location",redirectURL);
            setHeader("Content-Length","0");
        }
        else	//Normal response,check Content* headers
        {
            if(contentLength==-1L)     //Not set
            {
                if(!ob.isBufferWritten()) //No bytes written
                {
                    setHeader("Content-Length","0");
                }
                else
                {
                    setHeader("Content-Length",String.valueOf(ob.bytesWrittern()));
                }
            }
            else
            {
                setHeader("Content-Length",String.valueOf(contentLength));
            }
            if(contentType==null&&(!isRedirect || ob.isBufferWritten()))//must set contentType
            {
                throw new InvalidResponseException("must set content type");
            }
            if(contentType!=null)//if user setting charset in content type different from character encoding may leads to error
            {
                setHeader("Content-Type",contentType);
            }
        }
        //check general missing headers
        if(!headers.containsKey("Date"))
        {
            setHeader("Date",DateFormatter.STANDARD_FORMAT.format(new Date()));
        }
        if(!headers.containsKey("Server"))
        {
            setHeader("Server","MyServer/0.1");
        }
        if(!headers.containsKey("Connection"))//do not support keep-alive connection
        {
            setHeader("Connection","close");
        }
    }
	
    //Sending Netscape cookie instead of RFC2965 cookie
    private void sendCookies() {
        int num = cookies.size();
        if(num==0)
        {
            return;
        }
        for(int i=0;i<num;i++)
        {
            ob.realWriteBytes("Set-Cookie: ".getBytes(CS_USCII));
            ob.realWriteBytes(
                    CookieProcessor.generateHeader(cookies.get(i)).getBytes(CS_USCII));
            ob.realWriteCRLF();
        }
    }

	//----------private methods--------//

    private void parseCsFromContent()
    {
        if(contentType==null)
        {
            return;
        }
        String cs = "charset=";
        int index = contentType.indexOf(cs);
        if(index<0)
        {
            return;
        }
        characterEncoding = contentType.substring(index+cs.length());
    }
	
    private void prepareCharset()
    {
        if(!isCharsetSet()) //using explicitly set charset
        {
            parseCsFromContent();
            if(characterEncoding==null)
            {
                characterEncoding = StandardCharsets.UTF_8.name();//using utf-8 as default
            }
        }
        isCharsetSet = true;
    }

    //fast route writing error page to client
    private void writeSpecialBody()
    {
        if(isError)
        {
            if(!ob.isBufferWritten())
            {
                setBufferSize(512);
            }
            else
            {
                resetBuffer();
            }
            writeErrorPage();
        }
    }
	
    /*
     * This method exposes lower level detail.
     * However I can't find a better way :-(
     * write error page in USCII(compliant with UTF-8)
     */
    private void writeErrorPage()
    {
        //Direct write buffer??
        ByteBuffer buffer = ob.byteBuffer;
        buffer.put(HTMLHelper.ELE_HTML_BYTE)
            .put(ByteHelper.CRLF[0])//'\r'
            .put(HTMLHelper.ELE_HEAD_BYTE)
            .put(HTMLHelper.ELE_TITLE_BYTE)
            .put(String.valueOf(status).getBytes(CS_USCII))
            .put(HTMLHelper.ELE_TITLE_END_BYTE)
            .put(HTMLHelper.ELE_HEAD_END_BYTE)
            .put(ByteHelper.CRLF[0])
            .put(HTMLHelper.ELE_BODY_BYTE)
            .put(ByteHelper.CRLF[0])
            .put("<h1>".getBytes(CS_USCII))
            .put(HTMLHelper.ELE_CENTER_BYTE)
            .put(String.valueOf(status).getBytes(CS_USCII))
            .put(HTMLHelper.ELE_CENTER_END_BYTE)
            .put("</h1>".getBytes(CS_USCII))
            .put(HTMLHelper.HR_BYTE)
            .put(ByteHelper.CRLF[0])
            .put(HTMLHelper.ELE_CENTER_BYTE)
            .put("MyServer/0.1".getBytes(CS_USCII))
            .put(HTMLHelper.ELE_CENTER_END_BYTE)
            .put(HTMLHelper.ELE_BODY_END_BYTE)
            .put(HTMLHelper.ELE_HTML_END_BYTE);
        setHeader("Content-Length",String.valueOf(ob.bytesWrittern()));
    }

    //--------------------------Inner class--------------------//
    /*
     * Indicate the status of 
     */
    static enum WriteStatus
    {
        USINGWRITER,
        USINGOUTPUT,
        NOTUSED
    }
}
