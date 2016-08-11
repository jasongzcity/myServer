package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.jason.server.util.ByteHelper;
import com.jason.server.util.exception.InvalidResponseException;
import com.jason.server.util.http.DateFormatter;

/**
 * lower level implementation of HttpServletResponse
 * @author lwz
 * @since 2016-8-5 
 */
public final class MyServletResponse implements HttpServletResponse 
{
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
	private String message;//reponse message in 1st line
	private OutputBuffer ob;// this gives direct control of buffer
														 // also,wrap in ServletOutputStream or PrintWriter for 
													    // abstract read/write operation on http body
	private List<Cookie> cookies = new ArrayList<>();
	private Map<String,Object> headers  = new HashMap<String,Object>();
	private String protocol = "HTTP/1.1"; //default protocol
	private boolean isError;
	public static final Charset CS_USCII = StandardCharsets.US_ASCII; 
	
	public MyServletResponse(OutputStream out)
	{
		this.ob = new OutputBuffer(out,this);//Setup OutputBuffer
	}
	
	/**
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	@Override
	public void flushBuffer() throws IOException {
		ob.flush();
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
		setMessage(null);
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
		String date = DateFormatter.formatDate(arg1,null);
		addHeader(arg0,date);
	}

	/**
	 * Note: according to servlet API, {@link #addHeader(String, String)}should not overwrite
	 * the previous API, while {@link #setHeader(String, String)} should overwrite the previous API.
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
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	@Override
	public String encodeRedirectUrl(String arg0) {
		// deprecated
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	@Override
	public String encodeURL(String arg0) {
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	@Override
	public String encodeUrl(String arg0) {
		//deprecated
		return null;
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
		setMessage(null);
		commit();
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Read "redirect"

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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	//-----------send methods------------//
	
	private void sendFirstLine() throws InvalidResponseException
	{
		ob.realWriteBytes(protocol.getBytes(CS_USCII));
		ob.realWriteSpace();
		if(status>599||status<100)
		{
			throw new InvalidResponseException();
		}
		ob.realWriteBytes(String.valueOf(status).getBytes(CS_USCII));//writing status code
		ob.realWriteSpace();
		ob.realWriteBytes(message.getBytes(CS_USCII));
		ob.realWriteCRLF();
	}
	
	/*
	 * this method is responsible for parsing Strings and write bytes to OutputBuffer.
	 * send headers & cookies
	 * TODO: 8-12 finish send header & send error 
	 */
	@SuppressWarnings("unchecked")
	private void sendHeaders() throws InvalidResponseException
	{
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
			else
			{
				throw new InvalidResponseException();
			}
			ob.realWriteCRLF();
		}
		checkSpecialHeader();
		ob.realWriteCRLF();//line that separate header and body 
	}
	
	/**
	 * entrance to end this reponse. 
	 */
	public void commit()
	{
		try {
			sendFirstLine();
			sendHeaders();
		} catch (InvalidResponseException e) {
			try {
				ob.close();
			} catch (IOException e1) {

			}
		}
		setCommited(true);
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
	
	private void checkSpecialHeader()
	{
		
	}


	//------------------------------Inner class--------------------//
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
