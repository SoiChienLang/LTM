/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package java_client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.String;
import java.net.ServerSocket;
import java.util.Scanner;
/**
 *
 * @author Soi's
 */
public class Client_1 {

    /**
     * @param args the command line arguments
     */
    
    
    public final static int SOCKET_PORT = 12345;
    public final static String SERVER = "127.0.0.1";
    
    public static void main(String[] args) throws InterruptedException {
        PipedInputStream pipeIn;
        PipedOutputStream pipeOut;
        try {
            pipeIn = new PipedInputStream();
            pipeOut = new PipedOutputStream(pipeIn);
            
//            System.out.println(socket.getLocalAddress().getHostAddress());
            
            Thread_Receive_Data sock_receiv = new Thread_Receive_Data(pipeOut);
            sock_receiv.start();
            Thread_Send_Data sock_send = new Thread_Send_Data(pipeIn);
            sock_send.start();
        } catch (IOException ex) {
            Logger.getLogger(Client_1.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
class Thread_Receive_Data extends Thread{
    Socket socket = null;
    PipedOutputStream pipeOut = null;
    InputStream is = null;
    Thread_Receive_Data(PipedOutputStream pipeOut){
        this.pipeOut = pipeOut;
    }
    @Override
    public void run(){
        try {
            socket = new Socket(Client_1.SERVER, Client_1.SOCKET_PORT);
            is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            String filename;
            int fileSize;
            
            filename = dis.readLine();System.out.println(filename);
            fileSize = dis.readInt();System.out.println(fileSize);
            
            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream dos = new DataOutputStream(pipeOut);
            dos.writeUTF(filename);
            dos.writeInt(fileSize);
            dos.flush();
            byte[] data = new byte[1024];
            int byteRead, current = 0;
            do{
                byteRead = dis.read(data);
                fos.write(data, 0, byteRead);
                System.out.println("Write to file " + byteRead);
                pipeOut.write(data, 0, byteRead);
                System.out.println("Write to other threads");
                if(byteRead >=0) current += byteRead;
                                                                                
            }while(current != fileSize);
            System.out.println("Write to file done");
            fos.flush();
            pipeOut.flush();
            dis.close();
            is.close();
            socket.close();
            dos.close();
            pipeOut.close();
        } catch (IOException ex) {
            Logger.getLogger(Thread_Receive_Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
class Thread_Send_Data extends Thread{
    PipedInputStream pipeIn= null;
    ServerSocket server = null;
    DataInputStream dis = null;
    public Thread_Send_Data(PipedInputStream pipeIn) {
        this.pipeIn = pipeIn;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(23456);
            int fileSize;
            String fileName;
            dis = new DataInputStream(pipeIn);
            fileName = dis.readUTF();System.out.println("Thread send: " + fileName);
            fileSize = dis.readInt();System.out.println("Thread send: " + fileSize);
            
            Socket sock_to_pc2 = server.accept();System.out.println("Client: " + sock_to_pc2.getLocalAddress().getHostAddress());
//            Socket sock_to_pc3 = server.accept();
            
            send_header_to_socket(sock_to_pc2, fileName, fileSize);
//            send_header_to_socket(sock_to_pc3, fileName, fileSize);
            
            
            byte[] data = new byte[1024];
            int byteRead, current = 0;
            do{
                byteRead = pipeIn.read(data);
                send_data_to_socket(sock_to_pc2, data, byteRead);
//                send_data_to_socket(sock_to_pc3, data, byteRead);
                
                if(byteRead >= 0) current += byteRead;
            }while(current != fileSize);
            System.out.println("Send done");
            
        } catch (IOException ex) {
            Logger.getLogger(Thread_Send_Data.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
    public void send_data_to_socket(Socket socket, byte [] data, int byteRead) throws IOException{
        OutputStream os = socket.getOutputStream();
        os.write(data, 0, byteRead);
    }
    public void send_header_to_socket(Socket socket, String filename, int filesize) throws IOException{
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(filename);
        dos.writeInt(filesize);
        dos.flush();
    }
}
