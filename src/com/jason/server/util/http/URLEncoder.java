package com.jason.server.util.http;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * To better understand the principle of URL encoding,
 * I wrote this. Inspired by 
 * org.apache.catalina.util.URLEncoder &
 * java.net.URLEncoder
 * @author lwz
 * @since 2016-9-7
 * @see org.apache.catalina.util.URLEncoder
 * @see java.net.URLEncoder
 * Notice: as  org.apache.catalina.util.URLEncoder  says, it should let the users specify 
 * the "safe" character for encoding.
 * Notice:this class is not thread safe because of using BitSet, external synchronization
 * must be taken while multiple threads call method "addSafeCharacter"  
 * @see RFC 3986 
 */
public class URLEncoder
{
    //Notice: in DEFAULT encoder ,' '(space) will not be encoded as '+'
    public static final URLEncoder DEFAULT = new URLEncoder();
    static {
        DEFAULT.addSafeCharacter('~');
        DEFAULT.addSafeCharacter('-');
        DEFAULT.addSafeCharacter('_');
        DEFAULT.addSafeCharacter('.');
        DEFAULT.addSafeCharacter('*');
        DEFAULT.addSafeCharacter('/');
    }
	
    //Arrays for hexadecimal numbers mapping to characters
    //upper case
    public final static char[] HEXADECIMALS  = 
						{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	//definite safe character
    public URLEncoder() {
        for(char c='0';c<'9';c++) {
            safeCharacters.set(c);
        }
        for(char c='a';c<'z';c++) {
            safeCharacters.set(c);
        }
        for(char c='A';c<'Z';c++) {
            safeCharacters.set(c);
       }
    }
    
    protected BitSet safeCharacters = new BitSet(256);
	
    /**
     * add safe character to skip encoding
     * @param c the safe character
     */
    public void addSafeCharacter(char c) {
        safeCharacters.set(c);
    }
	
    /**
     * encode method 
     * @param src the source string
     * @param enc character encoding name
     * @return the URL-safe string
     */
    public String encode(String src,String enc)
    {
        boolean hasChange = false;
        Charset charset = null;
        StringBuilder builder = new StringBuilder(src.length());
        CharArrayWriter writer = new CharArrayWriter();
        try {
            charset = Charset.forName(enc);
        } catch(Exception e) {
            charset = StandardCharsets.UTF_8;//default
        }

        for(int i=0;i<src.length();) 
        {
            char c = src.charAt(i);
            if(safeCharacters.get(c))
            {
                builder.append(c);
                i++;
            }
            else
            {
                hasChange = true;
                do
                {
                    writer.append(c);
                    i++;
                }while(i<src.length() && !safeCharacters.get(c = src.charAt(i)));
	    
                //deal with not safe character
                byte[] bytes = writer.toString().getBytes(charset);
                for(int j=0;j<bytes.length;j++)
                {
                    builder.append('%');
                    builder.append(HEXADECIMALS[(bytes[j] & 0xF0) >> 4]);//get high 4bit
                    builder.append(HEXADECIMALS[bytes[j] & 0x0F]);//low 4bit
                }
            }
        }
        return hasChange ? builder.toString() : src;
    }
	
	//for test
//	public static void main(String[] args) throws UnsupportedEncodingException
//	{
//	    String str = DEFAULT.encode("HEAD中文在这里tail", "GBK");
//	    System.out.println(str);
//	    System.out.println(URLDecoder.decode(str,"GBK"));
//	    System.out.println(URLDecoder.decode("%E6%B5%8B%E8%AF%95%E6%90%9C%E7%B4%A2","UTF-8"));
//	}
}
