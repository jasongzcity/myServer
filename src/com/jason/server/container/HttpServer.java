package com.jason.server.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.HttpConnector;
import com.jason.server.util.exception.ExceptionUtils;

/**
  * This is the core of the whole container.
  * In tomcat, a server instance hold all the components 
  * and responsible of 
  * starting up and shutting downing components.
  * @author lwz
  * @since 2016-8-16 
  */ 
public class HttpServer
{
    private static final Logger log = LogManager.getLogger(HttpServer.class);

    //========static final fields==============
    //simulate a config file here :-)
    public static final String WEB_ROOT = System.getProperty("server.base")+File.separator+"webroot";
    public static final String STATIC_DIR = WEB_ROOT+File.separator+"static";
    public static final String SERVLET_DIR = WEB_ROOT+File.separator+"servlet";
    public static final String SHUTDOWN = "SHUTDOWN";//shutdown command
    public static final String HOST = "127.0.0.1";
  //port listening for connection,default 8080
    public static final int PORT = 8080;
    //port listening for shutdown command
    public static final int SHUTDOWNPORT = 8005;
    
    private boolean stopped;
    private ServerSocket serverSocket;
    
    private HttpConnector connector;
	
    public HttpServer(){}
	
    public void init()
    {
        connector = new HttpConnector(PORT);
        connector.init();
    }
	
    public void start()
    {
        connector.startup();
    }
	
    //main thread stay listening for shutdown command
    public void await()
    {
        try
        {
            //shutdown port bind on local host
            serverSocket = new ServerSocket(SHUTDOWNPORT,1,InetAddress.getByName(HOST));
        }
        catch(Exception e)
        {
            log.error("error binding awaiting server socket");
            shutdown();
        }
        while(!stopped)
        {
            Socket socket = null;
            InputStream in = null;
            try
            {
                socket = serverSocket.accept();
                socket.setSoTimeout(10*1000);
                in = socket.getInputStream();
            }
            catch(IOException e)
            {
                log.warn("error while accepting socket");
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
                if(b<32)//control characters
                {
                    break;
                }
                command += (char)b;
                count++;
            }while(count<SHUTDOWN.length());
            if(command.equals(SHUTDOWN))
            {
                stopped = true;//stop listening & head to shutdown method
            }
            try {
                socket.close();
            } catch (IOException e) {
                ExceptionUtils.swallowException(e);
            }
        }//while
    }

    /**
     *  shutdown this server instance
     */
    public void shutdown()
    {
        try {
            serverSocket.close();
        } catch (IOException e) {
            ExceptionUtils.swallowException(e);
        }
        connector.shutdown();
        log.info("server has been destroyed");
    }
    
    /**
     * Send shutdown command to the server.
     */
    public void shutdownServer()
    {
        try {
            Socket s = new Socket(HOST,SHUTDOWNPORT);
            OutputStream out = s.getOutputStream();
            out.write(SHUTDOWN.getBytes(StandardCharsets.US_ASCII));
            out.close();
            s.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
