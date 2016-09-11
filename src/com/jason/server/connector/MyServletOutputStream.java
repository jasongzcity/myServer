package com.jason.server.connector;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * Expose write byte method as ServletOutputStream
 * @author lwz
 * @since 2016-8-5
 * Note: it only support Latin-1 or may cause information loss.
 */
public class MyServletOutputStream extends ServletOutputStream 
{
	
    protected OutputBuffer ob;
    
    public MyServletOutputStream(OutputBuffer ob)
    {
        this.ob = ob;   
    }
	
    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener arg0) {
        return;
    }

    /**
     *  Must associate this method with underlying OutputBuffer
     *  @param b byte actually.upper level methods have already block the higher 8-bit
     *  @throws IOException 
     */
    @Override
    public void write(int b) throws IOException 
    {
        ob.writeByte((byte)b);
    }
	
    @Override
    public void flush()
    {
        try {
            ob.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
