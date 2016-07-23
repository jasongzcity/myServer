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
		
		//��outputStream��Socketд����,���ɷ�������
		out.println("GET /index.jsp HTTP/1.1");
		out.println("Host: localhost:8080");
		out.println("Connection: close");
		out.println();
		//û�з���ʵ��
		
		//��ȡ���
		boolean loop  = true;
		StringBuffer sb = new StringBuffer(8096);
		while(loop)
		{
			if(in.ready())//ѭ��ֱ���������л�Ӧ
			{
				int i = 0;
				while(i!=-1)
				{
					i = in.read();//���CHARACTER��ȡ
					sb.append((char)i);
				}
				loop = false;
			}
			Thread.currentThread().sleep(50);//ѭ����ͣ
		}
		System.out.println(sb.toString());
		mySocket.close();
	}
}