package com.jason.server.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.jason.server.connector.HttpConnector;

/**
  * This is the core of the whole container.
  * In tomcat, a server instance hold all the components 
  * and responsible of 
  * starting up and shutting downing.
  * @author lwz
  * @since 2016-8-16 
  */ 
public class HttpServer
{
	public static final String WEB_ROOT = System.getProperty("server.base")+File.separator+"webroot";
	public static final String SHUTDOWN = "SHUTDOWN";//shutdown command
	//port listening for shutdown command
	private final int shudownPort = 8005;
	private boolean stopped;
	
	
	//port listening for connection,default 8080
	private int port = 8080;
	private HttpConnector connector;
	
	public HttpServer(int port)
	{
		this.port = port;
	}
	public HttpServer(){}
	
	public void init()
	{
		connector = new HttpConnector(port);
		connector.init();
	}
	
	public void start()
	{
		connector.startup();
	}
	
	//main thread stay listening for shutdown command
	public void await()
	{
		ServerSocket serverSocket = null;
		try
		{
			//shutdown port bind on local host
			serverSocket = new ServerSocket(shudownPort,1,InetAddress.getLocalHost());
		}
		catch(Exception e)
		{
			//TODO: logger
			shutdown();
		}
		while(!stopped)
		{
			Socket socket = null;
			InputStream in = null;
			try
			{
				socket = serverSocket.accept();
				in = socket.getInputStream();
			}
			catch(IOException e)
			{
				//TODO: logging warning
				continue;
			}
			int count = 0;//counting byte
			int b = 0;
			String command = "";
			do
			{
				try {
					b = in.read();
				} catch (IOException e) {
					b = -1;
				}
				if(b<32)
				{
					break;
				}
				command += (char)b;
				count++;
			}while(count<SHUTDOWN.length()-1);
			if(command.equals(SHUTDOWN))
			{
				stopped = true;//stop listening & ahead to shutdown method
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO logger
				}
			}
		}//while
	}

	//shutdown this server instance
	public void shutdown()
	{
		connector.shutdown();
	}
	
}
