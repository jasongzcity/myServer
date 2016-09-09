package com.jason.server.util;

/**
 * Util class for byte manipulation.
 * @author lwz
 * @since 2016-7-29 
 */
public class ByteHelper
{
    public final static byte[] CRLF = {13,10};
    
    public final static byte SPACE = 32;
    
    public final static byte EQUAL = 61;
    
    public final static byte AND = 38;
    
    public final static byte COLON = 58;
    
    public final static byte SEMICOLON = 59;
    
    public final static byte COMMA = 44;
    
    private ByteHelper(){}
    
    /**
     * util method to find the CRLF in http protocol 
     * @param src the target byte buffer
     * @param posi begin index
     * @return the index of CR('\r') if matches or -1 if no matches
     */
    public static int checkNextCRLF(byte[] src,int posi)
    {
        int i = posi;
        while(true)
        {
            i = indexOf(src,CRLF[0],i);
            if(i==-1)//OutOfBound!
            {
                return -1;
            }
            else if(src[i+1]==CRLF[1])
            {
                return i;
            }
            //Not match,keep searching
        }
    }

    /**
     * call indexOf(src,b,0)
     * @return the first index to match the byte,or -1 if no byte matches.
     */
    public static int indexOf(byte[] src,int b)
    {
        return indexOf(src,b,0);
    }
	
    /**
     * String-like method to deal with bytes
     * @param src the source to search 
     * @param b the byte searching for 
     * @param begin the beginning index
     * @return the first index to match the byte,or -1 if no byte matches.
     */
    public static int indexOf(byte[] src,int b,int begin)
    {
        int i = begin;
        while(i<src.length)
        {
            if(src[i]==b)
            {
                return i;
            }
            i++;
        }
        return -1;
    }
	
    public static int findBytes(byte[] src,byte[] target)
    {
        return findBytes(src,0,target);
    }
	
    /**
     * To find a byte sequence in source byte array
     * @param src source byte array
     * @param begin the begin index of source array
     * @param target the target sequence you wants to find
     * @return the index of first match byte. Or -1 if not matches.
     * Note: no need to use KMP.....
     */
    public static int findBytes(byte[] src,int begin,byte[] target)
    {
        int i = begin;
        while(i<src.length)
        {
            for(int j=0,k=i;j<target.length;j++,k++)
            {
                if(target[j]!=src[k])
                {
                    break;
                }
                else if(j==target.length-1) //matches
                {
                    return i;
                }
            }
        }
        return -1;
    }
	
    /**
     * Note: use cautiously. heavy
     * @param src the source byte array 
     * @return the byte array which trims the SPACE(' ')
     * of the source  array.
     */
    public static byte[] trim(byte[] src)
    {
        int trimBegin = -1;
        int trimEnd = -1;
        int begin = 0;
        int end = src.length-1;
        while((trimBegin==-1||trimEnd==-1)&&(begin<end))//scan condition
        {
            if(src[begin]!=ByteHelper.SPACE && trimBegin==-1)//Not record 
            {
                trimBegin = begin;
            }
            if(src[end]!=ByteHelper.SPACE && trimEnd == -1)
            {
                trimEnd = end;
            }
            begin++;
            end--;
        }
        if(trimBegin==0&&trimEnd==src.length-1)//No need to trim
        {
            return src;
        }
        byte[] rs = new byte[trimEnd-trimBegin+1];
        System.arraycopy(src, trimBegin, rs, 0, rs.length);
        return rs;
    }
}
