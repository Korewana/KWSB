# KWSB
Korewana-Web-Server-Builder (KWSB) is a powerful tool to build and configure web servers in a simple and easy way in Java

# Summary
1. [Introduction](#creating-the-kwsb-object)
1. [Code Example](#simple-web-server-example)

## Creating the KWSB Object
The KWSB object is created using its constructor

**Example**:
```java
KWSB kwsb = new KWSB();
```

### Adding routes
With the addition of routes, building the web server really begins. 

**Example**:
```java
kwsb.addRequestHandler("/", new GetRequestHandler() {
   @Override
   public void onRequest(Request req, Response res) {
      res.send("Hello World!");
   }
})
```

## Simple Web-Server Example
```java
public static void main(String[] args) {
   int port = 80; //the port the server listens to

   KWSB kwsb = new KWSB(); //create the object
   
   //Add a handler to get route "/": localhost:80/
   kwsb.addRequestHandler("/", new GetRequestHandler() {
      @Override
      public void onRequest(Request req, Response res) {
         res.send("<h1>Hello World!</h1>"); //return "Hello World!" in a headline
      }
   });

   kwsb.listen(port).whenComplete((server) -> { //start the server
      System.out.println("Server listening to "+server.getIPAddress()+":"+port); //print ip and port
   });
}
```
