package com.jason.server.util.http;

/**
 * A simpler realization of cookie
 * @author jason
 * @since 2016-7-28 
 * @see javax.servlet.http.Cookie
 */
public class Cookie 
{
	private String name;
	private String value;
	
	public Cookie()
	{}
	
	public Cookie(String name,String value)
	{
		this.setName(name);
		this.setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
