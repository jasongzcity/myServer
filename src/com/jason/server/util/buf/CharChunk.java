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
public class CharChunk
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    
    //----fields and getter,setter----
    private char[] buff;    //storage
    private int head;          //lower index of the buff
    private int tail;             //higher index of the buff
    
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
    
    private int size;
    public int getSize() { return size; }
    
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
        head = tail = 0;
        charset = null;
        size = 0;
        limit = -1;
        isUsing = false;
        CharChunkFactory.recycle(this);
    }
    
    
}
