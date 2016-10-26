MyServer

This is an experimental project of an HTTP Server.It supports HTTP/1.0 and implements some of the Servlet Spec. interfaces for upper level programming.

While writing this, I mainly focus on the network connection and the HTTP message's parsing and constructing.

Modules in the server:
1.connector. It is responsible for the connection with the client, and the lower level details of parsing and writing the HTTP message byte by byte.

2.container. The container of resources. It is responsible for resources mapping & storing user's status etc.

3.util. The utility classes for the server. Some data structures for object caching to avoid GC. Some utility methods for byte manipulation and HTTP message parsing.

Check BUIDING_GUIDE.TXT for a quick-start.

 