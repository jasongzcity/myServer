package com.jason.server.util.http;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Decode the input using the specific character encoding.
 * @author lwz
 * @see com.jason.server.util.http.URLEncoder
 * @since 2016-9-7 
 */
public class URLDecoder
{
    private static final int DIGITDIS = '1' - 1;
    private static final int CHARDIS = 'A' - 10;
    
    //No instantiate
    private URLDecoder(){}
    
    /**
     * decode the URL
     * @param target URL string
     * @param enc character encoding name
     * @return the origin string
     * @throws UnsupportedEncodingException when the input encoding name is illegal
     */
    public static String decode(String target,String enc) throws UnsupportedEncodingException
    {
        boolean hasChange = false;
        StringBuilder builder = new StringBuilder(target.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(int i=0;i<target.length();)
        {
            char c = target.charAt(i);
            if(c != '%')
            {
                builder.append(c);
                i++;
            }
            else
            {
                hasChange = true;
                do
                {
                    if(i+2>=target.length()) {
                        throw new IllegalArgumentException("Illegal URL");
                    }
                    int high = charToInt(target.charAt(i+1)) << 4;
                    int low = charToInt(target.charAt(i+2));
                    out.write(high+low);
                    i += 3;
                } while(i<target.length() && target.charAt(i)=='%');
                //deal with bytes
                String orig = out.toString(enc);
                builder.append(orig);
            } 
        }
        return hasChange ? builder.toString() : target;
    }
    
    //transform character to its integer value
    private static int charToInt(char c)
    {
        if(Character.isDigit(c))
        {
            return c - DIGITDIS;
        }
        else//letter
        {
            return c - CHARDIS;
        }
    }
}
