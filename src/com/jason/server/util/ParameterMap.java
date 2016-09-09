package com.jason.server.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used for storing request parameters. 
 * subclass of LinkedHashMap but add an lock so when 
 * servlet programmer cant modify K-V in this Map.
 * insprired by org.apache.catalina.util.ParameterMap
 * @author lwz
 * @since JDK1.8
 * @see org.apache.catalina.util.ParameterMap
 */
public class ParameterMap<K,V> extends LinkedHashMap<K, V>
{

    private static final long serialVersionUID = -4381111522378696501L;
    
    private boolean locked = false;
    
    public ParameterMap()
    {
        super();
    }
	
    public ParameterMap(int initialCapacity)
    {
        super(initialCapacity);
    }
	
    public ParameterMap(int initialCapacity,float loadFactor)
    {
        super(initialCapacity,loadFactor);
    }
	
    public void setLockMode(boolean b)
    {
        this.locked = b;
    }
	
    public boolean isLock()
    {
        return locked;
    }
	
    @Override
    public V put(K key,V value)
    {
        if(locked)
        {
            throw new IllegalStateException();
        }
        return super.put(key, value);
    }
	
    @Override
    public void putAll(Map<? extends K,? extends V> map)
    {
        if(locked)
        {
            throw new IllegalStateException();
        }
        super.putAll(map);
    }
	
    @Override
    public void clear()
    {
        if(locked)
        {
            throw new IllegalStateException();
        }
        super.clear();
    }
}
