package com.jason.server.connector;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Handle output.
 * translate output String(char) to byte by specific charset
 * so that underlying outputbuffer wont cause infomation loss.
 * @author lwz
 * @since 2016-8-6 
 */
public class MyServletWriter extends PrintWriter 
{
    public MyServletWriter(Writer out) {
        super(out);//no autoflush
        //PrintWriter use Writer#write
    }
}
