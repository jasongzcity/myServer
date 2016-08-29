package com.jason.server.connector;

import java.io.File;
import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.container.HttpServer;
import com.jason.server.util.ClassLoaderFactory;

/**
  * The entrance. Starts the server
  * @author lwz
  * @since 2016-7-27
  */
public final class Bootstrap
{
	private static final Logger log = LogManager.getLogger(Bootstrap.class);

	//main method should left for arguments parsing
	public static void main(String[] args)
	{
		Bootstrap boot = new Bootstrap();
		if(args.length>0)
		{
			boot.init(args[0]);
		}
		else
		{
			boot.init(null);
		}
	}
	
	private URLClassLoader servletLoader;//ClassLoader for servlets 
	//Notice: URLClassLoader mainly rewrite findClass method 
	//but still use parent-delegation.
	//So it can still find javax.servlet.* because 
	//they are loaded by its parent(System-loader).
	public URLClassLoader getServletLoader(){ return servletLoader; }
	
	public void init(String port)
	{
		initClassLoader();
		HttpServer server = null;
		if(port != null)
		{
			server = new HttpServer(Integer.parseInt(port));
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
	
	/**
	 *  Create ClassLoader for servlets
	 */
	public void initClassLoader()
	{
		log.info("initializing class loaders");
		File[] files = { new File(HttpServer.SERVLET_DIR) };//put servlets in servlet_dir
		servletLoader = ClassLoaderFactory.createClassLoader(files,null,null);
	}
}