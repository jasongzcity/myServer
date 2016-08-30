package com.jason.server.util.collection;

/**
 * A synchronized stack data structure.
 * It's a very commonly used data structure 
 * for re-used objects to avoid frequent GC.
 * @author lwz
 *
 * @param <T> the type of contained object
 * @since 2016-8-30 
 */
public class SynchronizedStack<T> 
{
	private static final int DEFAULT_SIZE = 16;
	
	private Object[] stack;
	private int top = 0;//stack top index
	
	public SynchronizedStack(int initSize)
	{
		stack =  new Object[initSize];
	}
	
	public SynchronizedStack()
	{
		this(DEFAULT_SIZE);
	}
	
	//action before add
	//no need to synchronize on this method
	protected void ensureCapacity()
	{
		if(top<stack.length)
		{
			return;
		}
		Object[] newStack = new Object[stack.length<<1];
		System.arraycopy(stack, 0,newStack, 0, stack.length);
		stack = newStack;
	}
	
	public synchronized void push(T ele)
	{
		ensureCapacity();
		stack[top++] = ele;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T	pop()
	{
		if(top>0) {
			return (T)stack[--top];
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T peek()
	{
		if(top>0) {
			return (T)stack[top-1];
		} else {
			return null;
		}
	}
	
	public synchronized int size()
	{
		return top;
	}
	
	public synchronized boolean isEmpty()
	{
		return top==0;
	}
	
	public synchronized void reset()
	{
		top = 0;
	}
	
	public synchronized int capacity()
	{
		return stack.length;
	}
}
