package application;

import java.net.*;
import java.io.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HTTPServer implements Runnable{
    private Socket cs;
    private int port;
    private String IPAddress;

    public HTTPServer(Socket cs, String IPAddress, int port) {
        this.cs = cs;
        this.IPAddress=IPAddress;
        this.port = port;
    }

    public HTTPServer(String IPAddress, int port) {
        this.IPAddress = IPAddress;
        this.port = port;
        run();
    }

    public void run(){
        System.out.println("in run");
//        try {
//            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//
//            //serverSocketChannel.socket().bind(new InetSocketAddress(inetAddress.getHostAddress(),port));
//            serverSocketChannel.socket().bind(new InetSocketAddress(IPAddress,port));
//            serverSocketChannel.configureBlocking(false);
//
//             while(true){
//                 System.out.println(IPAddress+":"+port);
//              //   System.out.println("waiting for connection");
//            SocketChannel socketChannel =
//                    serverSocketChannel.accept();
//                // System.out.println("connected");
//
//
//            //do something with socketChannel...
//               }
//        } catch (Exception ee) {
//            ee.printStackTrace();
//        }
        try {
            PrintWriter out =
                    new PrintWriter(cs.getOutputStream(), true);
            InputStream is = cs.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while(true) {
                String request = br.readLine();
                System.out.println(request);
                out.println(":)");

            }

           // os.close();
           // cs.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}