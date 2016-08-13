package com.jason.server.util;

import java.nio.charset.StandardCharsets;

/**
 * Helper class to  
 * construct a simple HTML document
 * @author lwz
 * @since 2016-8-11 
 */
public final class HTMLHelper
{
	/*
	 * Get bytes from String, no need to look up the ASCII table  ;-)
	 * Notice:ASCII characters are compliant with UTF-8 
	 */
	public static final String ELE_HTML = "<html>";
	public static final byte[] ELE_HTML_BYTE = ELE_HTML.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_BODY = "<body>";
	public static final byte[] ELE_BODY_BYTE = ELE_BODY.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_TITLE = "<title>";
	public static final byte[] ELE_TITLE_BYTE = ELE_TITLE.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_HEAD = "<head>";
	public static final byte[] ELE_HEAD_BYTE = ELE_HEAD.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_HTML_END = "</html>";
	public static final byte[] ELE_HTML_END_BYTE = ELE_HTML_END.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_BODY_END = "</body>";
	public static final byte[] ELE_BODY_END_BYTE = ELE_BODY_END.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_TITLE_END = "</title>";
	public static final byte[] ELE_TITLE_END_BYTE = ELE_TITLE_END.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_HEAD_END = "</head>";
	public static final byte[] ELE_HEAD_END_BYTE = ELE_HEAD_END.getBytes(StandardCharsets.US_ASCII);
	public static final String HR = "<hr>";
	public static final byte[] HR_BYTE = HR.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_DIV = "<div>";
	public static final byte[] ELE_DIV_BYTE = ELE_DIV.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_DIV_END = "</div>";
	public static final byte[] ELE_DIV_END_BYTE = ELE_DIV_END.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_CENTER = "<center>";
	public static final byte[] ELE_CENTER_BYTE = ELE_CENTER.getBytes(StandardCharsets.US_ASCII);
	public static final String ELE_CENTER_END = "</center>";
	public static final byte[] ELE_CENTER_END_BYTE = ELE_CENTER_END.getBytes(StandardCharsets.US_ASCII);
	
}
