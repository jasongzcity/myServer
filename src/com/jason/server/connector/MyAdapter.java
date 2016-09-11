package com.jason.server.connector;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.HttpProcessor.ActionCode;
import com.jason.server.container.ServletProcessor;
import com.jason.server.container.StaticResourceProcessor;
import com.jason.server.util.collection.SynchronizedStack;
import com.jason.server.util.exception.InvalidRequestException;
import com.jason.server.util.http.HttpRequestUtil;

/**
 * Adapter service the request & response.
 * Finds the mapping resource for current session
 * and prepare for servlet
 * @author lwz
 * @since 2016-8-26 
 *
 */
public class MyAdapter 
{
    private static final Logger log = LogManager.getLogger(MyAdapter.class);
    
    private HttpProcessor httpProcessor;
    public void setHttpProcessor(HttpProcessor httpProcessor){ this.httpProcessor = httpProcessor; }
    public HttpProcessor getHttpProcessor(){ return httpProcessor; }
    
    public MyAdapter(HttpProcessor httpProcessor){ this.httpProcessor = httpProcessor; }
    
    protected SynchronizedStack<ServletProcessor> servletProcCache = new SynchronizedStack<>();
    private int servletProcCacheMax = 50;
    public int getServletProcCacheMax() { return servletProcCacheMax; }
    public void setServletProcCacheSize(int max) { this.servletProcCacheMax = max; }
    
    protected SynchronizedStack<StaticResourceProcessor> staticProcCache = new SynchronizedStack<>();
    private int staticProcCacheMax = 50;
    public int getStaticProcCacheMax() { return staticProcCacheMax; }
    public void setStaticProcCacheSize(int max) { this.staticProcCacheMax = max; }
	
    /**
     * Core method.
     * @param request lower level request
     * @param response lower level response
     */
    public void service(MyServletRequest request,MyServletResponse response)
    {
        try
        {
            HttpRequestUtil.parseRequestLine(request);
            HttpRequestUtil.parseHeaders(request);
        }
        catch(InvalidRequestException e)//Bad Request
        {
            log.warn("Received bad request",e);
            response.setError(true);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpProcessor.action(ActionCode.COMMIT);
            return;
        }
        
        //easy resource mapping ;-)
        if(request.getRequestURI().startsWith("/servlet/"))	//calling servlet
        {
            ServletProcessor servletProcessor = servletProcCache.pop();
            if(servletProcessor==null)
            {
                servletProcessor = new ServletProcessor(httpProcessor);
            }
            servletProcessor.process(request,response);
            if(servletProcCache.size()<servletProcCacheMax)
            {
                servletProcessor.recycle();
                servletProcCache.push(servletProcessor);
            }
        }
        else
        {
            StaticResourceProcessor staticProcessor = staticProcCache.pop();
            if(staticProcessor==null)
            {
                staticProcessor = new StaticResourceProcessor(httpProcessor);
            }			
            staticProcessor.process(request,response);
            if(staticProcCache.size()<staticProcCacheMax)
            {
                staticProcessor.recycle();
                staticProcCache.push(staticProcessor);
            }
        }
    }
}
