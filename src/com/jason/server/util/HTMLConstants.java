package com.jason.server.util;

/**
 * static byte arrays for connector to 
 * construct a simple HTML document
 * @author lwz
 * @since 2016-8-11 
 */
public interface HTMLConstants 
{
	/*
	 * Get bytes from String, no need to look up the ASCII table  ;-)
	 * Notice:ASCII characters are compliant with UTF-8 
	 */
	//Here store in String(JAVA using UTF-16) and later encode according to specific charset. 
	public static final String ELE_HTML = "<html>";
	public static final String ELE_BODY = "<body>";
	public static final String ELE_TITLE = "<title>";
	public static final String ELE_HEAD = "<head>";
	public static final String ELE_HTML_END = "</html>";
	public static final String ELE_BODY_END = "</body>";
	public static final String ELE_TITLE_END = "</title>";
	public static final String ELE_HEAD_END = "</head>";
	public static final String HR = "<hr/>";
	public static final String ELE_DIV = "<div>";
	public static final String ELE_DIV_END = "</div>";
	public static final String ELE_CENTER = "<center>";
	public static final String ELE_CENTER_END = "</center>";
}
