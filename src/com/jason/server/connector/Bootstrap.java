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
    public static void main(String[] args) throws InterruptedException
    {
        Bootstrap boot = new Bootstrap();
        boot.init(args);
    }

    private static URLClassLoader servletLoader;//ClassLoader for servlets 
    //Notice: URLClassLoader mainly rewrite findClass method 
    //but still use parent-delegation.
    //So it can still find javax.servlet.* because 
    //they are loaded by its parent(System-loader).
    public static URLClassLoader getServletLoader(){ return servletLoader; }
	
    public void init(String[] arguments)
    {
        //using default port
        HttpServer server = new HttpServer();
        String command = "start"; //default start
        if(arguments.length>0) {
            command = arguments[0];
        }
        if(command.equals("start"))
        {
            initClassLoader();
        	
            server.init();
            server.start();
            server.await();
            server.shutdown();
        }
        else if(command.equals("shutdown"))
        {
            server.shutdownServer();
        }
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