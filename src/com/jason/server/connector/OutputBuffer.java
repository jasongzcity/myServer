package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

/**
 * Standard output for response
 * create lower level of buffered output 
 * for ServletOutputStream & PrintWriter
 * Handle lower level I/O operations.
 * @author lwz
 * @since 2016-8-6
 */
public class OutputBuffer extends Writer 
{
	private static final int DEFAULT_CAPACITY = 8 * 1024;//buffer default capacity
	protected ByteBuffer byteBuffer;
	protected boolean isBufferWritten;
	protected OutputStream out;
	protected MyServletResponse response;
	
	public OutputBuffer(OutputStream out,MyServletResponse response)//lazily init byteBuffer
	{
		this.out = out;
		this.response = response;
	}
	
	public OutputBuffer(OutputStream out,int size,MyServletResponse response)
	{
		this.out = out;
		this.response = response;
		byteBuffer = ByteBuffer.allocate(size);
	}
	
	/*
	 *  Writer use this method 
	 *  its expensive ... arraycopy * 2
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException 
	{
		if(!isBufferWritten)
		{
			byteBuffer = ByteBuffer.allocate(DEFAULT_CAPACITY);
		}
		byteBuffer.put(new String(cbuf,off,len).getBytes(response.getCharacterEncoding()));
		isBufferWritten = true;
	}
	
	/*
	 * OutputStream use this method 
	 */
	public void writeByte(byte b)
	{
		if(!isBufferWritten)
		{
			byteBuffer = ByteBuffer.allocate(DEFAULT_CAPACITY);
		}
		byteBuffer.put(b);
		isBufferWritten = true;
	}

	@Override
	public void flush() throws IOException 
	{
		response.sendHeaders();
		doFlush();
	}

	@Override
	public void close() throws IOException 
	{
		out.close();
	}
	
	public void setBufferSize(int size)
	{
		//Safe to allocate 
		byteBuffer = ByteBuffer.allocate(size);
	}
	
	/**
	 * @return the capacity of byteBuffer or 0 if byteBuffer has not been initialize
	 */
	public int capacity()
	{
		if(!isBufferWritten)
		{
			return 0;
		}
		return byteBuffer.capacity();
	}
	
	public boolean isBufferWritten()
	{
		return isBufferWritten;
	}
	
	public void resetBuffer()
	{
		byteBuffer.clear();//reset position
	}
	
	//commit the response.prepare for response and setCommited
	private void doFlush()
	{
		
	}
}
