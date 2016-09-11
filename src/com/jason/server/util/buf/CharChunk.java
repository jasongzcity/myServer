package com.jason.server.util.buf;

import static java.lang.System.arraycopy;

/**
 * String like recyclable char chunk.
 * @author lwz
 * @since 2016-9-8 
 * @see org.apache.tomcat.util.buf.CharChunk
 *
 */
public class CharChunk implements Chunk
{    
    //----fields and getter,setter----
    private char[] buff;    //storage
    private int start = 0;          //start index of the buff
    private int end = -1;            //end index of the buff
    
    //flag for factory recycle
    private boolean isUsing;
    public void setUsing(boolean b) { this.isUsing = b; }
    public boolean isUsing() { return isUsing; }
    
    private int limit;//default no limit
    public void setLimit(int limit) { this.limit = limit; }
    public int getLimit() { return limit; }
    
    private CharInputChannel in;
    public void setInput(CharInputChannel in) { this.in = in; }
    public CharInputChannel getIntput() { return in; }
    
    private CharOutputChannel out;
    public void setOutput(CharOutputChannel out) { this.out = out; }
    public CharOutputChannel getOutput() { return out; }
    
    /**
     * These 2 interface are taken from org.apache.tomcat.util.buf.charChunk
     * They are for the holder of charChunk to provide InputChannel & OutputChannel
     * for the charChunk.It will be called when the buffer is full or empty.
     * @author lwz
     */
    public static interface CharInputChannel
    {
        /**
         * Read chars into buffer from the source.
         * @return the number of chars have been read.
         */
        public int readReadChars();
    }
    
    public static interface CharOutputChannel 
    {
        /**
         * Write chars to the target.
         * @param src the source char array
         * @param off the offset of the source 
         * @param len the number of chars to be written.
         */
        public void realWritechars(char[] src, int off, int len);
    }
    
    public int getSize() { return end-start+1; }
    
    public int getCapacity() { return buff.length; }
    
    public char[] getBuffer() { return buff; }
    
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
    
    //reset all parameter and push back to cache
    public void recycle()
    {
        end = -1;
        start = 0;
        limit = -1;
        isUsing = false;
        CharChunkFactory.FACTORY.recycle(this);
    }
    
    public void flushBuffer()
    {
        if(out==null)
        {
            throw new RuntimeException("Buffer Overflow!");
        }
        out.realWritechars(buff, start, getSize());
        start = 0;
        end = -1;
    }
    
    public String toString()
    {
        return new String(buff);
    }
    
    //---------------------- char manipulation ---------------
    
    //-------append------
    /**
     * append given chars at the end of the buffer
     * @param chars char array
     * @param off offset of the array
     * @param len the given char length
     * @return the charChunk itself
     */
    public CharChunk append(char[] chars, int off, int len)
    {
        makeSpace(len);
        if(getSize()+len>getCapacity())//flush or will be Out Of Bound!
        {
            flushBuffer();
        }
        for(int i=end+1,j=off;j<off+len;i++,j++)
        {
            buff[i] = chars[j];
        }
        end += len;
        return this;
    }
    
    /**
     * append given chars at the end of the buffer
     * @param chars the whole char array
     * @return the charChunk itself.
     */
    public CharChunk append(char[] chars)
    {
        return append(chars,0,chars.length);
    }
    
    /**
     * append a char and the end of the buff.
     * @param b char to be written
     * @return the charChunk itself.
     */
    public CharChunk append(char b)
    {
        makeSpace(1);
        if(getSize()+1>getCapacity())
        {
            flushBuffer();
        }
        buff[++end] = b;
        return this;
    }
    
    //Make new space for the incoming chars
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
            char[] newBuff = new char[newSize];
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
     * subtract a char from the begin of the buff.
     * @return the begin char.return -1 if no char left
     */
    public char substract()
    {
        if(start>end)
            return (char)-1;
        return buff[start++];
    }
    
    /**
     * subtract buffer into given array
     * @param dest destination array
     * @param off offset of the destination array
     * @param len number of char to write in array
     * @return the number of chars have been written.-1 if not enough chars for subtraction.
     */
    public int substract(char[] dest, int off, int len)
    {
        if(getSize()<len)
            return -1;
        arraycopy(buff,start,dest,off,len);
        start += len;
        return len;
    }
    
    //---- search method ----
    
    /**
     * Find the first char in this charChunk.
     * Simply call index(b,start).
     * @param b the require char
     * @return the index of the require char. -1 if not found
     */
    public int indexOf(char b)
    {
        return indexOf(b,start);
    }
    
    /**
     * Find the char from the beginning index
     * @param b the require char
     * @param begin the beginning index 
     * @return the index of the require char. -1 if not found
     */
    public int indexOf(char b,int begin)
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
     * Simply call index(chars,start)
     * @param chars chars to find.
     * @return the index of chars. -1 if not found.
     */
    public int indexOf(char[] chars)
    {
        return indexOf(chars,start);
    }
    
    /**
     *  Find matched char array in this charChunk.
     *  simple matching, no KMP.
     * @param chars char array to find.
     * @param begin the begin search index.
     * @return the index of chars. -1 if not found.
     */
    public int indexOf(char[] chars,int begin)
    {
        int theLastCheckIndex = end-chars.length+1;
        for(int i=begin;i<=theLastCheckIndex;i++)
        {
            for(int j=0,k=i;j<chars.length;j++,k++)
            {
                if(buff[k]!=chars[j])
                {
                    break;
                }
                else if(j==chars.length-1)//the last char of the array matches!
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    
}
