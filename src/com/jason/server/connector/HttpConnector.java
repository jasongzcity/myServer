package com.jason.server.connector;

import java.net.*;
import java.io.IOException;

/**
  * HttpConnector in charge of connecting the client.
  * Create threads to accept client requests and  hand them to 
  * processors.
  * @author lwz
  * @since JDK1.8
  */
public class HttpConnector
{
	//TODO: reuse HttpProcessors
	private boolean stopped;	//flag
	private String schema = "Http"; 	//handle http request
	
	private int port;
	private ServerSocket serverSocket;
	
	//Default two acceptors
	private int acceptorCount = 2;
	private Acceptor[] acceptors;
	
	public HttpConnector(int port)
	{
		this.port = port;
	}
	
	public String getSchema()
	{
		return schema;
	}
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	public void init()
	{
		try
		{
			serverSocket = new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));
		}
		catch(Exception e)
		{
			//TODO: logger 
			System.exit(-1);
		}
	}
	
	protected Acceptor createAcceptor()
	{
		return new Acceptor();
	}
	
	public void startup()
	{
		acceptors = new Acceptor[acceptorCount];
		for(int i=0;i<acceptors.length;i++)
		{
			acceptors[i] = createAcceptor();
			String threadName = "Acceptor-"+i;
			Thread t = new Thread(acceptors[i],threadName);
			t.start();
		}
		//TODO:Logger
	}
	
	public void shutdown()
	{
		stopped = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Logger
		}
	}
	
	//use multiple threads to listen to the port
	//protected: left extension for new IO realization
	protected class Acceptor implements Runnable
	{
		Socket socket;
		@Override
		public void run() 
		{
			while(!stopped)
			{
				try
				{
					socket = serverSocket.accept();
				}
				catch(IOException e)
				{
					continue;
				}
			}
			HttpProcessor processor = new HttpProcessor();
			try
			{
				processor.process(socket);
				socket.close();
			}
			catch(Exception e)
			{
				try {
					socket.close();
				} catch (IOException e1) {
					//Ignore..
				}
			}
		}
	}
}
		
		 