package com.jason.server.util.buf;

/**
 * Abstract methods for ByteChunk & CharChunk.
 * For the factory
 * @author lwz
 *
 */
public interface Chunk
{
    public void setUsing(boolean b);
    public boolean isUsing();
    
    public void setLimit(int limit);
    public int getLimit();
    
    public int getSize();
    
    public int getCapacity();
    public void recycle();
}
