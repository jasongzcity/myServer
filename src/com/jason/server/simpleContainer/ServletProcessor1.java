package com.jason.server.simpleContainer;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import java.io.File;
import java.io.IOException;
import javax.servlet.*;

public class ServletProcessor1
{
	public void process(Request request,Response response)
	{
		String uri = request.getUri();
		String servletName = uri.substring(uri.lastIndexOf("/")+1);//get the servletname
		URLClassLoader classLoader = null;
		try
		{
			URL[] urls = new URL[1];
			URLStreamHandler handler = null;
			File classPath = new File(HttpServer1.WEB_ROOT);
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
		
		Class myClass = null;
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
			System.out.println(t);
		}
	}
}
			
			
			
		
		