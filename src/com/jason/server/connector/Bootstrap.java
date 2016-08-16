package com.jason.server.connector;

import com.jason.server.container.HttpServer;

/**
  * The entrance. Starts the server
  * @author lwz
  * @since 2016-7-27
  */
public final class Bootstrap
{
	public static void main(String[] args)
	{
		HttpServer server = null;
		if(args.length>0)
		{
			server = new HttpServer(Integer.parseInt(args[0]));
		}
		else
		{
			server = new HttpServer();
		}
		
		server.init();
		server.start();
		server.await();
		server.shutdown();
	}
}