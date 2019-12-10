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

public class Client2_3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Scanner inp = new Scanner(System.in);

        int port = 12345;

        Socket s = new Socket("127.0.0.1", port);
        System.out.println("connected " + s.toString());

        DataInputStream is = new DataInputStream(s.getInputStream());
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        // nhan dia chi ip pc1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
//        String x = is.readUTF();
        String x = reader.readLine();
        System.out.println("server: " + x);

        if (x.equals("")) {
            System.out.println("chua nhan dk ip pc1");
        } else {
            // dong ket noi vs server
            s.close();
            // mo socket moi ket noi voi PC1
            int Port = 23456;
            Socket socket = new Socket(x, Port);
            System.out.println("conneced " + socket);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
//            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            // nhan ten file filesize
            InputStream is_from_other = socket.getInputStream();
            String filename = dis.readUTF();
            int fileSize = dis.readInt();
            // luu file

            byte[] data = new byte[180000];
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
                System.out.println(current + " " + byteRead);
            } while (current != fileSize);
            bos.flush();
        }
    }
}
