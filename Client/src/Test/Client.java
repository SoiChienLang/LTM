/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Soi's
 */
public class Client {

    public final static int BUFFER_SIZE = 1024;
    public final static String SERVER = "192.168.1.190";
    public final static int PORT = 12345;
    public final static int SIGNAL_DONE = 1;

    public static void main(String[] args) {
        try {
            Socket sock_to_server = new Socket(SERVER, PORT);
            System.out.println("Connected to Server: " + SERVER);
            System.out.println("Waiting signal from SERVER...");
            InputStream is = sock_to_server.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            int signal_receive;
            signal_receive = dis.readInt();
            if (signal_receive == 1) {
                System.out.println("SIGNAL: RECEIVE FROM SERVER!");
                System.out.println("~******************************~");
                PipedInputStream pipeIn;
                PipedOutputStream pipeOut;

                pipeIn = new PipedInputStream();
                pipeOut = new PipedOutputStream(pipeIn);
                Thread_Receive_Data thread_Rec = new Thread_Receive_Data(pipeOut, sock_to_server);
                thread_Rec.start();
                Thread_Send_Data thread_Send = new Thread_Send_Data(pipeIn);
                thread_Send.start();
            } else {
                receive_from_other(sock_to_server);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void receive_from_other(Socket sock_to_server) {
        try {
            System.out.println("SIGNAL: RECEIVE FROM OTHER CLIENT!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock_to_server.getInputStream()));

            String x = reader.readLine();
            System.out.println("Server of Client: " + x);

            int Port = 23456;
            Socket socket = new Socket(x, Port);
            System.out.println("Connected other client: " + socket);
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            InputStream is_from_other = socket.getInputStream();
            String filename = dis.readUTF();
            int fileSize = dis.readInt();

            byte[] data = new byte[Client.BUFFER_SIZE];
            int byteRead = 0;
            int current = 0;
            File file = new File("Download Folder\\" + filename);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            do {
                byteRead = is_from_other.read(data);
                bos.write(data, 0, byteRead);
                if (byteRead >= 0) {
                    current += byteRead;
                }
            } while (current != fileSize);
            bos.flush();
            send_signal_done(sock_to_server);
            System.out.println("RECEIVE SOFTWARE UPDATA DONE!");
            sock_to_server.close();
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void send_signal_done(Socket socket) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(SIGNAL_DONE);
        dos.flush();

        Date date = new Date();
        System.out.println("--Receive done at: " + date.toGMTString());
    }
}

class Thread_Receive_Data extends Thread {

    Socket sock_to_server = null;
    PipedOutputStream pipeOut = null;
    InputStream is = null;
    final int SIGNAL_DONE = 1;

    Thread_Receive_Data(PipedOutputStream pipeOut, Socket socket) {
        this.pipeOut = pipeOut;
        sock_to_server = socket;
    }

    @Override
    public void run() {
        try {
            is = sock_to_server.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            String filename;
            int fileSize;

            filename = dis.readLine();
            System.out.println("software_update_name: " + filename);
            fileSize = dis.readInt();
            System.out.println("size_file: " + fileSize);

            FileOutputStream fos = new FileOutputStream(filename);
            DataOutputStream dos = new DataOutputStream(pipeOut);
            dos.writeUTF(filename);
            dos.writeInt(fileSize);
            dos.flush();
            byte[] data = new byte[Client.BUFFER_SIZE];
            int byteRead, current = 0;
            do {
                byteRead = dis.read(data);
                fos.write(data, 0, byteRead);
                pipeOut.write(data, 0, byteRead);
                if (byteRead >= 0) {
                    current += byteRead;
                }

            } while (current != fileSize);
            System.out.println("RECEIVE SOFTWARE UPDATE DONE!");
            fos.flush();
            pipeOut.flush();
            send_signal_done(sock_to_server);
            dis.close();
            is.close();
            dos.close();
            sock_to_server.close();
            pipeOut.close();
        } catch (IOException ex) {
            Logger.getLogger(Thread_Receive_Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send_signal_done(Socket socket) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(SIGNAL_DONE);
        dos.flush();

        Date date = new Date();
        System.out.println("--Receive done at: " + date.toGMTString());
    }
}

class Thread_Send_Data extends Thread {

    PipedInputStream pipeIn = null;
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
            fileName = dis.readUTF();
            fileSize = dis.readInt();

            Socket sock_to_pc2 = server.accept();
            System.out.println("Other Client: " + sock_to_pc2.getLocalAddress().getHostAddress());
            Socket sock_to_pc3 = server.accept();
            System.out.println("Other Client: " + sock_to_pc3.getLocalAddress().getHostAddress());
            send_header_to_socket(sock_to_pc2, fileName, fileSize);
            send_header_to_socket(sock_to_pc3, fileName, fileSize);

            byte[] data = new byte[Client.BUFFER_SIZE];
            int byteRead, current = 0;
            do {
                byteRead = pipeIn.read(data);
                send_data_to_socket(sock_to_pc2, data, byteRead);
                send_data_to_socket(sock_to_pc3, data, byteRead);

                if (byteRead >= 0) {
                    current += byteRead;
                }
            } while (current != fileSize);

        } catch (IOException ex) {
            Logger.getLogger(Thread_Send_Data.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void send_data_to_socket(Socket socket, byte[] data, int byteRead) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(data, 0, byteRead);
    }

    public void send_header_to_socket(Socket socket, String filename, int filesize) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(filename);
        dos.writeInt(filesize);
        dos.flush();
    }

}
