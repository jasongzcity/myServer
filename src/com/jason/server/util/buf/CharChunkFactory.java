package com.jason.server.util.buf;

/**
 * Inherit CharChunk for most methods.
 * And use getInstance for specific type instantiate.
 * @author lwz
 * @since 2016-9-9
 * @see org.jason.server.util.buf.CharChunk
 * @see org.jason.server.util.buf.ChunkFactory
 */
public class CharChunkFactory extends ChunkFactory<CharChunk>
{
    public static final CharChunkFactory FACTORY = new CharChunkFactory();
    @Override
    public CharChunk getInstance(int capacity, int limit)
    {
        if(capacity>MAX_INDEX)
        {
            return null;
        }
        Object[] array = (Object[])BINS[capacity-1];
        CharChunk result = (CharChunk) getIfCached(array);
        if(result!=null)
        {
            return result;
        }
        return new CharChunk(capacity,limit);
    }
    
    //no public instantiate
    private CharChunkFactory()
    {
        super();
    }
}
