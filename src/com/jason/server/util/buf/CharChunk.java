package com.jason.server.util.buf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * String like recyclable char chunk.
 * @author lwz
 * @since 2016-9-8 
 * @see org.apache.tomcat.util.buf.CharChunk
 *
 */
public class CharChunk implements Chunk
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    
    //----fields and getter,setter----
    private char[] buff;    //storage
    private int start;          //start index of the buff
    private int end;            //end index of the buff
    
    private Charset charset;
    public void setCharset(Charset charset) { this.charset = charset; }
    public Charset getCharset() { return charset; }
    
    //flag for factory recycle
    private boolean isUsing;
    public void setUsing(boolean b) { this.isUsing = b; }
    public boolean isUsing() { return isUsing; }
    
    private int limit;//default no limit
    public void setLimit(int limit) { this.limit = limit; }
    public int getLimit() { return limit; }
    
    public int getSize() { return end-start; }
    
    //----Constructors,should only be instantiated by factory----
    //must specify the capacity
    CharChunk(int capacity,int limit)
    {
        buff = new char[capacity];
        setLimit(limit);
        setUsing(true);
    }
    
    CharChunk(int capacity)
    {
        this(capacity,-1);//no limit
    }
    
    //---- util methods----
    public int getCapacity()
    {
        return buff.length;
    }
    
    //reset all parameter and push back to cache
    public void recycle()
    {
        end = start = 0;
        charset = null;
        limit = -1;
        isUsing = false;
        CharChunkFactory.FACTORY.recycle(this);
    }
    
    public char[] getBuffer()
    {
        return buff;
    }
    
    
}