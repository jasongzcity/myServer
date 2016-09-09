package com.jason.server.util.buf;

/**
 * Factory class for Chunks.
 * Use a simple CharChunk[][] 2 dimension array to store Chunks.
 * 
 * In this class,Chunks are cached in a bin,the CharChunks in a bin have the same capacity,
 * and bins are in an array in the format like BINS[(CharChunk's capacity)/2].
 * Saving the Chunks according to their size, so that when applications require
 * Chunks with specific capacity, the factory don't need to iterate all the 
 * Chunks it cached.
 * 
 * @author lwz
 * @since 2016-9-9 
 */
public abstract class ChunkFactory<T extends Chunk>
{
    //the maximum index of the array buff
    //only cache CharChunks whose capacity are below 60
    protected static final int MAX_INDEX = 30;
    //cache size of every bin
    protected static final int CACHE_SIZE = 15;
    //stores arrays of CharChunks
    protected final Object[] BINS = new Object[MAX_INDEX];
    
    protected ChunkFactory()
    {
        //initialize the 2-d array
        for(int i=0;i<MAX_INDEX;i++)
        {
            Object[] array = new Object[CACHE_SIZE+1];
            array[0] = new Object();//lock object for this array
            BINS[i] = array;
        }
    }
    
    /**
     * Get CharChunk without limit size.
     * @param capacity the capacity of the CharChunk
     * @return the required CharChunk from cache or newly created.
     */
    public T getInstance(int capacity)
    {
        return getInstance(capacity,-1);
    }
    
    /**
     * Get CharChunk by specific capacity and limit size
     * @param capacity the capacity
     * @param limit the limit size
     * @return the required CharChunk from cache or newly created.
     */
    public abstract T getInstance(int capacity,int limit);
    
    /**
     * push the CharChunk back to cache if the cache still have space.
     * else ignore, let it be GCed.
     * @param charChunk CharChunk to be cached.
     */
    public void recycle(T chunk)
    {
        int index = (chunk.getCapacity()+1)>>2;
        if(index>MAX_INDEX)//too large,no cache
        {
            return;
        }
        putIfNull(chunk,(Object[])BINS[index]);
    }
    
    //check if usable CharChunk in the array,return null if none.
    //if any,set the CharChunk in "in use" status and return.
    @SuppressWarnings("unchecked")
    protected T getIfCached(Object[] array)
    {
        synchronized(array[0])
        {
            for(int i=1;i<CACHE_SIZE+1;i++)//array[0] is the lock object
            {
                if(array[i]!=null && !((T)array[i]).isUsing())//in extreme circumstance this logic may still fail
                {
                    T result = (T)array[i];
                    result.setUsing(true);
                    return result;
                }
            }
        }
        //no cache usable
        return null;
    }
    
    //put the CharChunk back to the cache. return true if put back success 
    //return false if the cache is overload.
    protected boolean putIfNull(T c,Object[] arr)
    {
        synchronized(arr[0])
        {
            for(int i=1;i<CACHE_SIZE+1;i++)
            {
                if(arr[i]==null)
                {
                    arr[i] = c;
                    return true;
                }
            }
            return false;
        }
    }
}
