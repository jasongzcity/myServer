package com.jason.server.connector;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
  * HttpConnector in charge of connecting the client.
  * Create threads to accept client requests and  hand them to 
  * processors.
  * @author lwz
  * @since JDK1.8
  */
public class HttpConnector
{
	private static Logger log = LogManager.getLogger(HttpConnector.class);
	
	//TODO: reuse HttpProcessors
	private boolean stopped;	//flag
	private String schema = "Http"; 	//handle http request
	
	private int port;
	private ServerSocket serverSocket;
	
	//Default two acceptors
	private int acceptorCount = 2;
	private Acceptor[] acceptors;
	
	//thread number that queue at port
	private int backLog = 3;
	public int getBackLog() { return backLog; }
	public void setBackLog(int backLog) { this.backLog = backLog; }
	
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
			log.error("can't bind server socket to given address");
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
		log.info("setting up acceptors");
	}
	
	public void shutdown()
	{
		stopped = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			log.warn("error while closing socket");
		}
		log.info("connector has been destroyed");
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
				try{
					socket = serverSocket.accept();
					socket.setSoTimeout(10*1000);
				}catch(IOException e){
					log.warn("error while accepting socket");
					continue;
				}
				HttpProcessor processor = new HttpProcessor();
				processor.process(socket);
				try {
					socket.close();
				} catch (IOException e) {
					log.warn("error while closing socket");
				}
			}
			log.info("exiting acceptor");
		}
	}
}
		
		 