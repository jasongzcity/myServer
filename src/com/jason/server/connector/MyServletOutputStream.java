package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * Buffered Output.Store protocol body bytes here and wait for flush command 
 * @author lwz
 * @since 2016-8-5
 * Note: actually ServletOutputStream is kind of strange,
 * it only support Latin-1 or may cause information loss.
 */
public class MyServletOutputStream extends ServletOutputStream 
{
	private static final int DEFAULT_BUFFER_SIZE = 8*1024;
	
	protected ByteBuffer byteBuffer;					//hold body
	protected MyServletResponse response;//associate response for callback
	protected OutputStream out;
	
	public MyServletOutputStream(OutputStream out,MyServletResponse response)//lazy init bytebuffer
	{
		this.response = response;
		this.out = out;
	}
	
	public MyServletOutputStream(OutputStream out,MyServletResponse response,int size)
	{
		this.response = response;
		byteBuffer = ByteBuffer.allocate(size);
		this.out = out;
	}
	
	
	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
		return;
	}

	@Override
	public void write(int b) throws IOException {
		if(byteBuffer==null)
		{
			byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		}
		byteBuffer.put((byte)b);//the underlying method has already safely change them to byte
	}
	
	@Override
	public void flush()
	{
		response.sendHeaders();//set commited
		doFlush();
	}
	
	/*
	 * Expose ByteBuffer for repsonse
	 * since its low level,wont change by servlet programmar
	 */
	public ByteBuffer getByteBuffer()
	{
		return byteBuffer;
	}
	
	//write byte[] to socket's output stream
	private void doFlush()
	{
		try {
			out.write(byteBuffer.array(),0,byteBuffer.position()-1);
		} catch (IOException e) {
			//...Ignore
		}
	}

}
