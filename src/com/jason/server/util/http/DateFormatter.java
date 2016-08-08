package com.jason.server.util.http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * an util class for date formatting.
 * its a experimental server,no cache ;-)
 * @author lwz
 * @see org.apache.tomcat.util.http.FastHttpDateFormat
 */
public final class DateFormatter 
{
	public static final String RFC1123_DATE =
            "EEE, dd MMM yyyy HH:mm:ss zzz";

	//Note: SimpleDateFormat is not thread-safe
    public static final SimpleDateFormat STANDARD_FORMAT =
            new SimpleDateFormat(RFC1123_DATE, Locale.US);

    private static final TimeZone gmtZone = TimeZone.getTimeZone("GMT");

    static {
    	STANDARD_FORMAT.setTimeZone(gmtZone);
    }
    
    private DateFormatter(){}
    
    /**
     * format date using given long value
     * @param value milliseconds of currentTime
     * @param sdf SimpleDateFormat object to format date.if null,use Standard format instead
     * @return String format of Date in RFC1123 
     */
    public static String formatDate(long value,SimpleDateFormat sdf)
    {
    	Date date = new Date(value);
    	String rs = null;
    	if(sdf!=null)
    	{
    		rs = sdf.format(date);
    	}
    	else
    	{
    		synchronized(STANDARD_FORMAT)
    		{
    			rs = STANDARD_FORMAT.format(date);
    		}
    	}
    	return rs;
    }
    
    /**
     * parse date to long(millisecs)
     * @param date the String format of date 
     * @param sdf the specific SimpleDateFormat.If null,use STANDARD_FORMAT instead. 
     * @return millisecs or null if parse error
     */
    public static long parseDate(String date,SimpleDateFormat sdf)
    {
    	Date d;
    	try
    	{
    		if(sdf!=null)
    		{	
    			d = sdf.parse(date);
    		}
    		else
    		{
        		synchronized(STANDARD_FORMAT)
        		{
        			d = STANDARD_FORMAT.parse(date);
        		}
    		}
    	}
    	catch(ParseException e)
    	{
    		return -1L;
    	}
    	return d.getTime();
    }
}
