package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * lower level implementation of HttpServletResponse
 * @author lwz
 * @since 2016-8-5 
 */
public class MyServletResponse implements HttpServletResponse 
{
	protected String characterEncoding;
	protected boolean isCharsetSet;//charset explicitly setted
	protected boolean isCommited;
	protected String contentType;
	protected WriteStatus writeStatus = WriteStatus.NOTUSED;
	protected OutputStream out;//wrapped by ServletOutputStream or PrintWriter
	protected ServletOutputStream sout;
	protected PrintWriter writer;
	protected Locale locale;
	protected Long contentLength = -1L;
	protected ByteBuffer headerBuffer;//store header
	
	public void setOutputStream(OutputStream out)
	{
		this.out = out;
	}
	
	/**
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
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
		sout = new MyServletOutputStream(out,this);
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
		String cs = getCharacterEncoding();
		writer = new PrintWriter(new OutputStreamWriter(out,cs));
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
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	@Override
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding(String arg0) 
	{
		this.characterEncoding = arg0;
		isCharsetSet = true;
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	@Override
	public void setContentLength(int arg0) 
	{	
		this.contentLength = (long) arg0;
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentLengthLong(long)
	 */
	@Override
	public void setContentLengthLong(long arg0) 
	{
		this.contentLength = arg0;
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String arg0) {
		this.contentType = arg0;
		if(!isCharsetSet)
		{
			parseCsFromContent();
		}
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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub

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
