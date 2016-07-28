package com.jason.server.connector;

/**
  * The entrance. Starts the  server
  * @author lwz
  * @since 2016-7-27
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