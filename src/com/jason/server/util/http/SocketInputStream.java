package com.jason.server.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.jason.server.util.ByteHelper;
import com.jason.server.util.exception.InvalidRequestException;

/**
 * this class handle the socket's InputStream 
 * and parse the request for the processor
 * @author lwz
 * @since JDK1.8
 * should use a requestProcessor later....
 */
public class SocketInputStream extends InputStream
{
	protected byte[] byteBuffer;
	private int requestLineEnd;
	private int headerEnd;
	private boolean hasContent;
	private InputStream in;
	
	/**
	 * the constructor reads the inputstream and wrap the bytes in bytebuffer
	 * @param in socket's InputStream
	 * @param length the byteBuffer initial length(to proper estimate reduce array copy)
	 */
	public SocketInputStream(InputStream in,int length)
	{
		this.in = in;
		int i = 0;
		int posi = 0;
		byte[] buffer = new byte[length];
		while(true)
		{
			try {	
				i = in.read(buffer,posi,length);//read from the posi and 'length' bytes every time
			} catch (IOException e) {
				e.printStackTrace();
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
		System.out.println(new String(byteBuffer));
		/*
		 * TODO:later change it to the nio to support the socket.
		 */
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
	 * @throws IOException when reading InputStream
	 * @throws InvalidRequestException when the request line is not in right syntax
	 */
	public void readRequestLine(HttpRequestLine requestLine) throws InvalidRequestException, IOException
	{
		int i = ByteHelper.checkNextCRLF(byteBuffer, 0);
		if(i==-1)
		{
			throw new InvalidRequestException();
		}
		requestLineEnd = i;
		String target = new String(byteBuffer,0,i,StandardCharsets.ISO_8859_1);
		String[] arr = target.split(" ");
		if(arr.length!=3)
		{
			throw new InvalidRequestException("request line error");
		}
		requestLine.method = arr[0];
		requestLine.uri = arr[1];
		requestLine.protocol = arr[2];
		requestLine.setWholeLine(target);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> readHeaders() 
	{
		int i = requestLineEnd+2;//skip CRLF
		List<String> list = new ArrayList<>();
		while(true)
		{
			int head = ByteHelper.checkNextCRLF(byteBuffer, i);
			if(head==-1)
			{
				headerEnd = i;
				hasContent = false;
				break;
			}
			else if(byteBuffer[head+2]==ByteHelper.CRLF[0])//header end and empty line
			{
				headerEnd = i;
				hasContent = true;
				list.add(new String(byteBuffer,i,head-i,StandardCharsets.ISO_8859_1));
				break;
			}
			else//still has headers
			{
				list.add(new String(byteBuffer,i,head-i,StandardCharsets.ISO_8859_1));
			}
			i = head+2;
		}
		return list;
	}
}
