package com.jason.server.util.buf;

/**
 * Factory class for ByteChunk.
 * Use mechanism like CharChunkFactory.
 * 
 * @author lwz
 * @since 2016-9-9 
 * @see org.jason.server.util.buf.ByteChunk
 * @see org.jason.server.util.buf.ChunkFactory
 *
 */
public class ByteChunkFactory extends ChunkFactory<ByteChunk>
{
    public static final ByteChunkFactory FACTORY = new ByteChunkFactory();
    @Override
    public ByteChunk getInstance(int capacity, int limit)
    {
        if(capacity>MAX_INDEX)
        {
            return null;
        }
        Object[] array = (Object[])BINS[capacity-1];
        ByteChunk result = (ByteChunk) getIfCached(array);
        if(result!=null)
        {
            return result;
        }
        return new ByteChunk(capacity,limit);
    }
    
    //no public instantiate
    private ByteChunkFactory()
    {
        super();
    }
}
