package com.jason.server.util.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jason.server.util.exception.InvalidRequestException;

/**
 * this class handle the socket's InputStream 
 * and parse the request for the processor
 * @author lwz
 * @since JDK1.8
 * should use a requestProcessor later....
 */
public class SocketInputStream extends BufferedInputStream
{	
	public SocketInputStream(InputStream in,int length)
	{
		super(in,length);//wrap the socket's InputStream with BufferedInputStream
											 //it implements the InputStream interface and use an 
											 //underlying buffer for faster I/O
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
		read();	//trigger fill() to fill underlying char[] buf with socket's inputstream
		String  target = new String(buf).split("\n")[0];
		String[] arr = target.split(" ");
		if(arr.length!=3)
		{
			throw new InvalidRequestException("request line error");
		}
//		System.out.println(new String(buf));
//		System.out.println(arr[0]);
//		System.out.println(arr[1]);
//		System.out.println(arr[2]);
		requestLine.method = arr[0];
		requestLine.uri = arr[1];
		requestLine.protocol = arr[2];
		requestLine.setWholeLine(target);
	}
}
