package com.jason.server.util.buf;

import static java.lang.System.arraycopy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Recyclable byte buffer for byte storage.
 * It has String-like methods.
 * Its reuse mechanism is just like CharChunk.
 * @author lwz
 * @since 2016-9-9 
 * @see org.jason.server.util.buf.CharChunk
 * @see org.jason.server.util.buf.ByteChunkFactory
 *
 * Some codes are taken from ByteHelper.
 * TODO:modify methods using ByteHelper and byte[] to use ByteChunk instead.
 */
public class ByteChunk implements Chunk
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    //----fields and getter,setter----
    private byte[] buff;        //buffer
    private int start = 0;      //start index of the buffer. initially 0.
    private int end = -1;      //(end index) of the buffer.initially -1 for empty buffer.

    private Charset charset;
    public void setCharset(Charset charset) { this.charset = charset; }
    public Charset getCharset() { return charset; }
    
    //flag for recycling
    private boolean isUsing;
    public void setUsing(boolean b) { this.isUsing = b; }
    public boolean isUsing() { return isUsing; }
    
    private int limit;//default no limit
    public void setLimit(int limit) { this.limit = limit; }
    public int getLimit() { return limit; }
    
    private ByteInputChannel in;
    public void setInput(ByteInputChannel in) { this.in = in; }
    public ByteInputChannel getIntput() { return in; }
    
    private ByteOutputChannel out;
    public void setOutput(ByteOutputChannel out) { this.out = out; }
    public ByteOutputChannel getOutput() { return out; }
    
    /**
     * These 2 interface are taken from org.apache.tomcat.util.buf.ByteChunk
     * They are for the holder of ByteChunk to provide InputChannel & OutputChannel
     * for the ByteChunk.It will be called when the buffer is full or empty.
     * @author lwz
     */
    public static interface ByteInputChannel
    {
        /**
         * Read bytes into buffer from the source.
         * @return the number of bytes have been read.
         */
        public int readReadBytes();
    }
    
    public static interface ByteOutputChannel 
    {
        /**
         * Write bytes to the target.
         * @param src the source byte array
         * @param off the offset of the source 
         * @param len the number of bytes to be written.
         */
        public void realWriteBytes(byte[] src, int off, int len);
    }
    
    public int getSize() { return end-start+1; }
    
    public byte[] getBuffer() { return buff; }
    
    /**
     * get the underlying buff's the capacity
     * @return the underlying buff's the capacity
     */
    public int getCapacity() { return buff.length; }
    
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
    
    /**
     * recycle method.remember to call it when the chunk is currently 
     * not used.
     */
    public void recycle()
    {
        end = -1;
        start = 0;
        limit = -1;
        charset = null;
        isUsing = false;
        ByteChunkFactory.FACTORY.recycle(this);
    }
    
    public String toString()
    {
        Charset cs = getCharset();
        if(cs==null)
        {
            cs = DEFAULT_CHARSET;
        }
        return new String(buff, start, getSize(),cs);
    }
    
    public String toString(Charset cs)
    {
        return new String(buff,start,getSize(),cs);
    }
    
    public void flushBuffer()
    {
        if(out==null)
        {
            throw new RuntimeException("Buffer Overflow!");
        }
        out.realWriteBytes(buff, start, getSize());
        start = 0;
        end = -1;
    }
    
    //---------------------- byte manipulation ---------------
    
    //-------append------
    /**
     * append given bytes at the end of the buffer
     * @param bytes byte array
     * @param off offset of the array
     * @param len the given byte length
     * @return the ByteChunk itself
     */
    public ByteChunk append(byte[] bytes, int off, int len)
    {
        makeSpace(len);
        if(getSize()+len>getCapacity())//flush or will be Out Of Bound!
        {
            flushBuffer();
        }
        for(int i=end+1,j=off;j<off+len;i++,j++)
        {
            buff[i] = bytes[j];
        }
        end += len;
        return this;
    }
    
    /**
     * append given bytes at the end of the buffer
     * @param bytes the whole byte array
     * @return the ByteChunk itself.
     */
    public ByteChunk append(byte[] bytes)
    {
        return append(bytes,0,bytes.length);
    }
    
    /**
     * append a byte and the end of the buff.
     * @param b byte to be written
     * @return the ByteChunk itself.
     */
    public ByteChunk append(byte b)
    {
        makeSpace(1);
        if(getSize()+1>getCapacity())
        {
            flushBuffer();
        }
        buff[++end] = b;
        return this;
    }
    
    //Make new space for the incoming bytes
    protected void makeSpace(int newSpace)
    {
        if(getCapacity()==limit)//optimizing,directly return
        {
            return;
        }
        int newSize = 0;//size for allocate
        int desireSize = getSize()+ newSpace;//size caller require
        if(desireSize > getCapacity())//need new buff
        {
            newSize = (limit==-1)||(desireSize*2 < limit)? desireSize*2 : limit;//if desireSize>limit,still make to limit,let upper level method to handle.
            byte[] newBuff = new byte[newSize];
            arraycopy(buff,start,newBuff,0,getSize());
            buff = newBuff;
            end = getSize()-1;
            start = 0;
        }
        else
        {
            int endRemain = getCapacity()-end-1;//the space in the end of array.
            if(endRemain<newSpace)//make the array begin with index 0.
            {
                arraycopy(buff,start,buff,0,getSize());
                end = getSize()-1;
                start = 0;
            }
            //else enough space, directly return;
        }
    }
    
    //----- subtraction -----
    
    /**
     * subtract a byte from the begin of the buff.
     * @return the begin byte.return -1 if no byte left
     */
    public byte substract()
    {
        if(start>end)
            return -1;
        return buff[start++];
    }
    
    /**
     * subtract buffer into given array
     * @param dest destination array
     * @param off offset of the destination array
     * @param len number of byte to write in array
     * @return the number of bytes have been written.-1 if not enough bytes for subtraction.
     */
    public int substract(byte[] dest, int off, int len)
    {
        if(getSize()<len)
            return -1;
        arraycopy(buff,start,dest,off,len);
        start += len;
        return len;
    }
    
    //---- search method ----
    
    /**
     * Find the first byte in this byteChunk.
     * Simply call index(b,start).
     * @param b the require byte
     * @return the index of the require byte. -1 if not found
     */
    public int indexOf(byte b)
    {
        return indexOf(b,start);
    }
    
    /**
     * Find the byte from the beginning index
     * @param b the require byte
     * @param begin the beginning index 
     * @return the index of the require byte. -1 if not found
     */
    public int indexOf(byte b,int begin)
    {
        if(begin<start) { begin = start; }
        if(begin>end) { return -1; }
        int i = begin;
        while(i<=end)
        {
            if(buff[i]==b)
            {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    /**
     * Simply call index(bytes,start)
     * @param bytes bytes to find.
     * @return the index of bytes. -1 if not found.
     */
    public int indexOf(byte[] bytes)
    {
        return indexOf(bytes,start);
    }
    
    /**
     *  Find matched byte array in this ByteChunk.
     *  simple matching, no KMP.
     * @param bytes byte array to find.
     * @param begin the begin search index.
     * @return the index of bytes. -1 if not found.
     */
    public int indexOf(byte[] bytes,int begin)
    {
        int theLastCheckIndex = end-bytes.length+1;
        for(int i=begin;i<=theLastCheckIndex;i++)
        {
            for(int j=0,k=i;j<bytes.length;j++,k++)
            {
                if(buff[k]!=bytes[j])
                {
                    break;
                }
                else if(j==bytes.length-1)//the last byte of the array matches!
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
//    public static void main(String[] args)
//    {
//        ByteChunk bc = new ByteChunk(10);
//        bc.append(new String("  abcd  ").getBytes(StandardCharsets.UTF_8));
//        //bc.append(ByteHelper.CRLF);
//        //bc.append(new String("abcd").getBytes(StandardCharsets.UTF_8),2,2);
//        //System.out.println(bc.substract()==ByteHelper.SPACE);
//        byte[] temp = new byte[15];
//        bc.substract(temp, 0, 4);
//        System.out.println(new String(temp , StandardCharsets.UTF_8));
//        System.out.println(bc.toString(StandardCharsets.UTF_8));
//        bc.append(new String("这里有部分中文").getBytes(StandardCharsets.UTF_8));
//        System.out.println(bc.toString(StandardCharsets.UTF_8));
//        System.out.println(bc.indexOf(new String("部分").getBytes(StandardCharsets.UTF_8)));
//        System.out.println(bc.indexOf((byte)'d'));
//    }
}
