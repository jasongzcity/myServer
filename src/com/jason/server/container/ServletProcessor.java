package com.jason.server.container;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.io.File;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServletProcessor
{
	private static final Logger log = LogManager.getLogger(ServletProcessor.class);
	
	public void process(HttpServletRequest request,HttpServletResponse response)
	{
		String uri = request.getRequestURI();
		String servletName = uri.substring(uri.lastIndexOf("/")+1);//get the servletname
		URLClassLoader classLoader = null;
		try
		{
			URL[] urls = new URL[1];
			URLStreamHandler handler = null;
			File classPath = new File(HttpServer.WEB_ROOT);
			//the forming of repository is taken from org.apache.catalina.startup.ClassLoaderFactory
			//method createClassLoader
			String repository = (new URL("file",null,classPath.getCanonicalPath()+File.separator)).toString();
			//the forming of URL is taken from org.apache.catalina.loader.StandardClassLoader
			//method addRepository
			//System.out.println(repository);
			URL url = new URL(null,repository,handler);
			//System.out.println(url);
			urls[0] = url;
			classLoader = new URLClassLoader(urls);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		Class<?> myClass = null;
		try
		{
			myClass = classLoader.loadClass(servletName);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		Servlet servlet = null;
		try
		{
			servlet = (Servlet)myClass.newInstance();
			servlet.service((ServletRequest)request,(ServletResponse)response);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}
			
			
			
		
		