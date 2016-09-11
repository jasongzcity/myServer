package com.jason.server.util.buf;

/**
 * Abstract methods for ByteChunk & CharChunk.
 * For the factory.
 * @author lwz
 * @since 2016-9-11 
 */
public interface Chunk
{
    /**
     * setter & getter of the using status
     */
    public void setUsing(boolean b);
    public boolean isUsing();
    
    /**
     * setter & getter for the limit
     */
    public void setLimit(int limit);
    public int getLimit();
    
    /**
     * get the current size of the chunk
     */
    public int getSize();
    
    /**
     * get the buffer's capacity
     */
    public int getCapacity();
    
    /**
     * recycle the chunk
     */
    public void recycle();
}
