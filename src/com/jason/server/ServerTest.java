package com.jason.server;

import java .net.Socket;
import java.io.*;

public class ServerTest
{
	public static void main(String[] args) throws Exception 
	{
		Socket mySocket = new Socket("127.0.0.1",8080);
		OutputStream os = mySocket.getOutputStream();
		
		PrintWriter out = new PrintWriter(os,true);
		BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		
		//用outputStream向Socket写数据,即可发送请求
		out.println("GET /index.jsp HTTP/1.1");
		out.println("Host: localhost:8080");
		out.println("Connection: close");
		out.println();
		//没有发送实体
		
		//读取结果
		boolean loop  = true;
		StringBuffer sb = new StringBuffer(8096);
		while(loop)
		{
			if(in.ready())//循环直到服务器有回应
			{
				int i = 0;
				while(i!=-1)
				{
					i = in.read();//逐个CHARACTER读取
					sb.append((char)i);
				}
				loop = false;
			}
			Thread.currentThread().sleep(50);//循环暂停
		}
		System.out.println(sb.toString());
		mySocket.close();
	}
}