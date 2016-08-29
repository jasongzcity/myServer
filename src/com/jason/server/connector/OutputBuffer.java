package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.util.ByteHelper;
import com.jason.server.util.exception.ExceptionUtils;

/**
 * Standard output for response
 * create lower level of buffered output 
 * for ServletOutputStream & PrintWriter
 * Handle lower level I/O operations.
 * 
 * 2016-8-8 : rewrite {@link Writer#write(String)} and associate methods 
 * to escape redundant copying.
 * 
 * @author lwz
 * @since 2016-8-6
 */
public class OutputBuffer extends Writer 
{
	private static final Logger log = LogManager.getLogger(OutputBuffer.class);
	private static final int DEFAULT_CAPACITY = 8 * 1024;//buffer default capacity 8k
	
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
	
	//write methods,write bytes to byteBuffer,for ServletOutputStreams and PrintWriter
	

	@Override
	public void write(char[] cbuf, int off, int len) throws UnsupportedEncodingException 
	{
		/*
		 *  Writer use this method 
		 *  its expensive ... arraycopy * 2
		 *  could we encode byte in a simpler way? direct putChar OK??
		 *  charset.encode also cost 2 times of array copying.
		 */
		checkBuffer();
		byteBuffer.put(new String(cbuf,off,len).getBytes(response.getCharacterEncoding()));
		isBufferWritten = true;
	}
	
	//Note: writing bytes to a protected ByteBuffer
	//No need to worry about synchronizing
	@Override
	public void write(String s,int off,int len) throws UnsupportedEncodingException
	{
		checkBuffer();
		write(s.substring(off, off+len));
	}
	
	@Override
	public void write(String s) throws UnsupportedEncodingException
	{
		checkBuffer();
		byteBuffer.put(s.getBytes(response.getCharacterEncoding()));
	}
	
	/*
	 * OutputStream use this method 
	 */
	public void writeByte(byte b)
	{
		checkBuffer();
		byteBuffer.put(b);
		isBufferWritten = true;
	}

	//Note:Writer & OutputStream call this method.
	@Override
	public void flush() throws IOException 
	{
		response.commit();
		out.close();
	}

	@Override
	public void close()
	{
		try {
			out.close();
		} catch (IOException e) {
			ExceptionUtils.swallowException(e);
		}
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
		if(byteBuffer==null)
		{
			return;
		}
		byteBuffer.clear();//reset position
	}
	
	//since writing from the index 0
	//the position represents the bytes have been written
	public int bytesWrittern()
	{
		return byteBuffer.position();
	}
	
	//----------------------output methods,direct write to socket----------//
	
	//methods for reponse to write headers
	public void realWriteBytes(byte[] src,int off,int len)
	{
		try {
			out.write(src, off, len);
		} catch (IOException e) {
			log.warn("transfering "+e.getClass().getName()+" to RuntimeException");
			ExceptionUtils.transRuntimeException(e);
		}
	}
	
	public void realWriteByte(byte src)
	{
		try {
			out.write(src);
		} catch (IOException e) {
			log.warn("transfering "+e.getClass().getName()+" to RuntimeException");
			ExceptionUtils.transRuntimeException(e);
		}
	}
	
	public void realWriteCRLF()
	{
		realWriteBytes(ByteHelper.CRLF);
	}
	
	public void  realWriteBytes(byte[] src)
	{
		realWriteBytes(src,0,src.length);
	}
	
	public void realWriteSpace()
	{
		realWriteByte(ByteHelper.SPACE);
	}
	
	//write byteBuffer to socket
	public void flushBody() throws IOException
	{
		if(response.isRedirect())//no need to write byte buffer
		{
			return;
		}
		out.write(byteBuffer.array(), 0, byteBuffer.position());//write byte array
		
		if(log.isDebugEnabled()){
			log.debug(new String(byteBuffer.array()));
		}
	}
	
	//-----------------private methods-------------------//
	

	private void checkBuffer()
	{
		if(!isBufferWritten && byteBuffer==null)
		{
			byteBuffer = ByteBuffer.allocate(DEFAULT_CAPACITY);
		}
	}
}
