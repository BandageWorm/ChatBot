package mcs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ReplyServer {
    public static int port = 23333;
    public static String host = "localhost";
    private Engine engine;

    public static void main(String[] args) {
        ReplyServer rs = new ReplyServer();
        rs.init();
    }

    private ReplyServer(){
        this.engine = new Engine();
    }

    public void init() {
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Response server started!");
            while (true){
                Socket socket = ss.accept();
                new HandlerThread(socket);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class HandlerThread implements Runnable {
        Socket socket;
        public HandlerThread(Socket client) {
            socket = client;
            new Thread(this).run();
        }

        public void run() {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String ask = dis.readUTF();
                String res = engine.reply(ask);

                String log = ask + " -> " + res + "\n";
                System.out.print(log);
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FilePath.get("Text\\LogReply.txt"),true), Util.CHARSET);
                osw.write(log);

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(res);

                osw.close();
                dis.close();
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
