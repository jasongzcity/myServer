package com.jason.server.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for ClassLoader(basically URLClassLoader)
 * @author lwz
 * @since 2016-8-20 
 * @see org.apache.catalina.startup.ClassLoaderFactory
 */
public final class ClassLoaderFactory 
{
	private static final Logger log = LogManager.getLogger(ClassLoaderFactory.class);
	
	/**
	 * turning directories to URL and create an URL classloader
	 * @param unpacked directories that doesn't contain JAR file 
	 * @param packed directories that DOES contain JAR file 
	 * @param parent parent class loader
	 * @return URLClassLoader that contains packed & unpacked as Class path
	 * Using stream as test :-)))
	 */
	public static URLClassLoader createClassLoader(File[] unpacked,File[] packed,ClassLoader parent)
	{
		Set<URL> URLSet = new HashSet<>();
		if(unpacked!=null)
		{
			URLSet.addAll(
					Arrays.stream(unpacked)
						.filter( file  -> { return file.isDirectory()&&file.canRead(); })//only directory
						.map( file  -> { 
							URL url = null;	
							try {
									url =  new File(file.getCanonicalPath()+File.separator).toURI().toURL();
								} catch (Exception e) {
									//ignore
								}
								return url; })//mapping URL
						.collect(Collectors.toSet()));
		}
		if(packed!=null)
		{
			for(int i=0;i<packed.length;i++) //not using stream because need to use outer level dir name
			{
				final File dir = packed[i];
					URLSet.addAll(
						Arrays.stream(dir.list())
							.map(str -> str.toLowerCase())
							.filter(str -> str.endsWith(".jar"))
							.map(str -> {
								URL url = null;
								try{
									url = new File(dir.getCanonicalFile(),str).toURI().toURL();
								}catch(Exception e){
									//Ignore
								} 
								return url; })
							.collect(Collectors.toSet()));
			}			
		}
		
		URL[] urls = (URL[]) URLSet.toArray();
		if(parent!=null)
		{
			return new URLClassLoader(urls,parent);
		}
		else
		{
			return new URLClassLoader(urls);//System class loader as parent.
		}
	}
}
