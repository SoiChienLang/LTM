/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client1;

/**
 *
 * @author dinhc
 */
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client1 {

    /**
     * @param args the command line arguments
     */
 

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Scanner inp = new Scanner(System.in);

        int port=12345;
        
        Socket s = new Socket("127.0.0.1", port);
        System.out.println("conneced" + s);

        DataInputStream is = new DataInputStream(s.getInputStream());
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        // nhan dia chi ip pc1;

        String x = is.readUTF();
        System.out.println("server: " + x);

        if (x.equals("")) {
            System.out.println("chua nhan dk ip pc1");
        } else {
            // dong ket noi vs server
            s.close();
            // mo socket moi ket noi voi PC1
            int Port = 12345;
            Socket socket = new Socket(x,Port);
            System.out.println("conneced "+ socket);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            // nhan ten file filesize
            String filename = dis.readUTF();
            int fileSize = dis.readInt();
            // luu file
            
             byte[] data = new byte[180000];
                    int byteRead=0;
                    int current = 0;
                File    file = new File("Download Folder\\" + filename);
                FileOutputStream    fos = new FileOutputStream(file);
                BufferedOutputStream    bos = new BufferedOutputStream(fos);
                    do{
                        byteRead = is.read(data);
                        bos.write(data,0, byteRead);
                        if(byteRead >=0) current+=byteRead;
                        System.out.println(current + " " + byteRead);
                    }while(current != fileSize);
                    bos.flush();
        }
    }
}
