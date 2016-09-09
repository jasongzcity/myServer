package com.jason.server.util.buf;

/**
 * Recyclable byte buffer for byte storage.
 * It has String-like methods.
 * Its reuse mechanism is just like CharChunk.
 * @author lwz
 * @since 2016-9-9 
 * @see org.jason.server.util.buf.CharChunk
 * @see org.jason.server.util.buf.ByteChunkFactory
 *
 */
public class ByteChunk implements Chunk
{
    //----fields and getter,setter----
    private byte[] buff;    //storage
    private int start;          //start index of the buff
    private int end;             //end index of the buff

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
    ByteChunk(int capacity,int limit)
    {
        buff = new byte[capacity];
        setLimit(limit);
        setUsing(true);
    }
    
    ByteChunk(int capacity)
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
        limit = -1;
        isUsing = false;
        ByteChunkFactory.FACTORY.recycle(this);
    }
}
