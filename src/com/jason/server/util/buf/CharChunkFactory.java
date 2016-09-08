package com.jason.server.util.buf;

/**
 * Factory class for CharChunk.
 * Use a simple CharChunk[][] 2 dimension array to store CharChunks.
 * 
 * In this class,CharChunk is cached in a bin,the CharChunks in a bin have the same capacity,
 * and bins are in an array in the format like BINS[CharChunk's capacity].
 * Saving the CharChunks according to its size, so that when applications require
 * CharChunk with specific capacity, the factory don't need to iterate all the 
 * CharChunks it cached.
 * 
 * @author lwz
 * @since 2016-9-9 
 */
public final class CharChunkFactory
{
    //the maximum index of the array buff
    //only cache CharChunks whose capacity are below 60
    private static final int MAX_INDEX = 30;
    //cache size of every bin
    private static final int CACHE_SIZE = 15;
    //stores arrays of CharChunks
    private static final Object[] BINS = new Object[MAX_INDEX];
    static {
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
    public static CharChunk getInstance(int capacity)
    {
        return getInstance(capacity,-1);
    }
    
    /**
     * Get CharChunk by specific capacity and limit size
     * @param capacity the capacity
     * @param limit the limit size
     * @return the required CharChunk from cache or newly created.
     */
    public static CharChunk getInstance(int capacity,int limit)
    {
        int index = (capacity+1)>>1;
        CharChunk result = getIfCached((Object[])BINS[index]);
        if(result!=null)
        {
            result.setLimit(limit);
            return result;
        }
        return new CharChunk(capacity,limit);
    }
    
    /**
     * push the CharChunk back to cache if the cache still have space.
     * else ignore, let it be GCed.
     * @param charChunk CharChunk to be cached.
     */
    public static void recycle(CharChunk charChunk)
    {
        
    }
    
    //check if usable CharChunk in the array,return null if none.
    //if any,set the CharChunk in "in use" status and return.
    private static CharChunk getIfCached(Object[] array)
    {
        for(int i=1;i<CACHE_SIZE+1;i++)//array[0] is the lock object
        {
            if(array[i]!=null && !((CharChunk)array[i]).isUsing())//in extreme circumstance this logic may still fail
            {
                synchronized(array[0])
                {
                    CharChunk result = (CharChunk)array[i];
                    result.setUsing(true);
                    return result;
                }
            }
        }
        //no cache usable
        return null;
    }
}
