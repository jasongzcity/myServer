package com.jason.server.connector;

/**
  * the entrance of the server. Starts the  server
  */
public final class Bootstrap
{
	public static void main(String[] args)
	{
		HttpConnector connector = null;
		if(args.length>0)
		{
			connector = new HttpConnector(Integer.parseInt(args[0]));
		}
		else
		{
			connector = new HttpConnector();
		}
		connector.start();
	}
}