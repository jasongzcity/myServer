package com.jason.server.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.MyServletRequest;
import com.jason.server.util.ByteHelper;
import com.jason.server.util.exception.InvalidRequestException;

/**
 * this class directly reads the socket's InputStream 
 * and do simple parsing for request's util method 
 * @author lwz
 * @since 2016-8-4 
 */
public class SocketInputStream extends InputStream
{
    private static final Logger log = LogManager.getLogger(SocketInputStream.class);
    
    protected byte[] byteBuffer;
    private int length;
    private int requestLineEnd = -1;
    private int headerEnd = -1;
    private boolean hasBody;
    private InputStream in;
    protected Charset defaultCharset = StandardCharsets.US_ASCII;
    public void setDefaultCharset(Charset charset)
    {
        this.defaultCharset = charset;  
    }
    public Charset getDefaultCharset(){ return defaultCharset; }
	
    /**
     * the constructor reads the inputstream and wrap the bytes in bytebuffer
     * @param in socket's InputStream
     * @param length the byteBuffer initial length(to proper estimate reduce array copy)
     */
    public SocketInputStream(InputStream in,int length)
    {
        this.in = in;
        this.length = length;
        init();
        /*
         * TODO:later change it to the nio to support the socket.
         */
    }
	
    //init method may need to rewrite: 
    //It works like: bulk reading the whole http message and later parse it. 
    //It may cause severe performance damage while the input stream is
    //not arrived yet since it will block the thread.
    private void init()
    {
        int i = 0;
        int posi = 0;
        byte[] buffer = new byte[length];
        while(true)
        {
            try {	
                i = in.read(buffer,posi,length);//read from the posi and 'length' bytes every time
            } catch (IOException e) {
                log.warn("IOE while reading request");
                continue;
            }
            if(i<length)//to the end..
            {
                break;
            }
            else
            {
                byte[] tmp = new byte[buffer.length+length];
                System.arraycopy(buffer,0,tmp,0,buffer.length);//copy buffer
                posi = buffer.length;
                buffer = tmp;
            }
        }
        byteBuffer = buffer;
    }
	
    @Override
    public int read() throws IOException
    {
        return in.read();
    }
	
    /**
     * this method read from input and set the 
     * properties for object requestLine
     * @param requestLine Object that encapsulate http request line 
     * @throws InvalidRequestException when the request line is not in right syntax
     */
    public void readRequestLine(MyServletRequest request) throws InvalidRequestException
    {
        if(requestLineEnd==-1)
        {
            findRequestLineEnd();
        }
        int methodEnd = ByteHelper.indexOf(byteBuffer, ByteHelper.SPACE, 0);
        request.setMethod(new String(byteBuffer,0,methodEnd,defaultCharset));
        int uriEnd = ByteHelper.indexOf(byteBuffer, ByteHelper.SPACE, methodEnd+1);//find uri String
        request.setRequestURI(new String(byteBuffer,methodEnd,uriEnd-methodEnd,defaultCharset));
        request.setProtocol(new String(byteBuffer,uriEnd+1,requestLineEnd-uriEnd-1,defaultCharset));
    }
	
    /**
     * read headers from socket's inputStream
     * @return list of Strings for utility methods
     * @throws InvalidRequestException 
     */
    public List<String> readHeaders() throws InvalidRequestException 
    {
        int i = 0;
        if(requestLineEnd == -1)
        {
            throw new InvalidRequestException();//the request line end should be set by now
        }
        List<String> list = new ArrayList<>();
        i = requestLineEnd + 2;
        while(true)
        {
            int head = ByteHelper.checkNextCRLF(byteBuffer, i);
            if(head==-1)
            {
                headerEnd = i;
                hasBody = false;
                break;
            }
            else if(byteBuffer[head+2]==ByteHelper.CRLF[0])//header end and empty line
            {
                headerEnd = i;
                hasBody = true;
                list.add(new String(byteBuffer,i,head-i,defaultCharset));
                break;
            }
            else//still has headers
            {
                list.add(new String(byteBuffer,i,head-i,defaultCharset));
            }
            i = head+2;
        }
        return list;
    }
	
    private void findRequestLineEnd() throws InvalidRequestException
    {
        int i = ByteHelper.checkNextCRLF(byteBuffer, 0);
        if(i==-1)
        {
            throw new InvalidRequestException();
        }
        requestLineEnd = i;
    }
}
