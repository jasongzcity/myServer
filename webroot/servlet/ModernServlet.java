
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

public class ModernServlet extends HttpServlet 
{
    private static final long serialVersionUID = -7488396741783319940L;
    
    @Override
    protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException, IOException
    {
        httpResponse.setCharacterEncoding("utf-8");//remember to call this method before calling getWriter!
        httpResponse.setContentType("text/html; charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = httpResponse.getWriter();
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"en\">");
            writer.println("<head>");
            writer.println("<meta charset=\"utf-8\">");
            writer.println("<title>Servlet Test</title>");
            writer.println("	</head><body>");
            writer.println("<h1><center>RequestLine</center></h1>");
            writer.print("<div>");
            writer.print(httpRequest.getMethod()+" ");
            writer.print(httpRequest.getRequestURI()+" ");
            writer.print(httpRequest.getProtocol()+" ");
            writer.print("</div>");
            writer.println();
            writer.println("<h2><center>Headers</center></h2>");
            Enumeration<String> headerEnum = httpRequest.getHeaderNames();
            while(headerEnum.hasMoreElements()) {
                String headerName = headerEnum.nextElement();
                String value = httpRequest.getHeader(headerName);
                writer.print("<div>");
                writer.print(headerName+": "+value);
                writer.println("</div>");
            }
            Enumeration<String> paramEnum = httpRequest.getParameterNames();
            while(paramEnum.hasMoreElements()) {
                String paramName = paramEnum.nextElement();
                String paramValue = httpRequest.getParameter(paramName);
                writer.print("<div>");
                writer.print(paramName+": "+paramValue);
                writer.println("</div>");
            }
            httpResponse.setStatus(200);
            httpResponse.flushBuffer();

        } catch(IOException e) {
            e.printStackTrace();
            httpResponse.flushBuffer();
            return;
        }
    }
}
