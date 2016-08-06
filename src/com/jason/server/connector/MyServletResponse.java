package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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
	private ByteBuffer responseLine;//store response line
	private OutputBuffer ob;	   // this gives direct control of buffer
															   // also,wrap in ServletOutputStream or PrintWriter for 
															   // abstract read/write operation on http body
	private final List<Cookie> cookies = new ArrayList<>();
	private Map<String,List<String>> headers;
	
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
		responseLine.clear();
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
		isCharsetSet = true;
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
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	@Override
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
	 */
	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
	 */
	@Override
	public Collection<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#getStatus()
	 */
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader(String arg0, long arg1) {
		
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	@Override
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}
	
	public void sendHeaders()
	{
		
	}
	
	//-----------------------------private method---------------//
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
	
	
	//------------------------------Inner class--------------------//
	/*
	 * Indicate the status of 
	 */
	protected enum WriteStatus
	{
		USINGWRITER,
		USINGOUTPUT,
		NOTUSED
	}
}
