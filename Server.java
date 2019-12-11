
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static ServerSocket listener = null;
    private static long timeStart=0;
    private static long timeEnd=0;
    private static long timeSend=0;
		
    public static void main(String args[]) throws IOException {
        try {
            listener = new ServerSocket(12345);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        
        try {
        	int clientNumber=0;
        	System.out.println("SERVER:");
            while (true) {
                clientNumber++;
                System.out.println("Server is waiting to accept user...");
                Socket socketOfServer = listener.accept();
                System.out.println(socketOfServer.getInetAddress().getHostAddress());
                if (clientNumber == 1) {
                    socketOfServer.getInetAddress().getHostAddress();
                }
                ServiceThread st = new ServiceThread(socketOfServer, clientNumber);
                st.start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ServiceThread extends Thread {
        private final static String Folder = "\\Share Folder\\";
        private final static int sizeBuffered = 2048;

        private Socket socketReceive;
        private int isNewServer =0;
        private BufferedWriter bWrite;
        private Scanner sc;

        public ServiceThread(Socket socketOfServer, int statusServer) {
            this.socketReceive = socketOfServer;
            if(statusServer==1) this.isNewServer = statusServer;
        }

        @Override
        public void run() {
            try {
            	String IpClient=socketReceive.getInetAddress().getHostAddress();
                DataOutputStream dos = new DataOutputStream(socketReceive.getOutputStream());
                DataInputStream dis = new DataInputStream(socketReceive.getInputStream());
                bWrite = new BufferedWriter(new OutputStreamWriter(this.socketReceive.getOutputStream()));
//-->send is new server to client (int)
                dos.writeInt(isNewServer);
                dos.flush();
                
                if (isNewServer == 1) {                	
                    sc = new Scanner(System.in);
                    System.out.println("to "+IpClient+" name file:");
                    String file = sc.nextLine();

                    if (!(new File(System.getProperty("user.dir") + Folder + file)).exists()) {
                        System.out.println("to "+IpClient+": File is not exist");
                    } else {
                        System.out.println(System.getProperty("user.dir") + Folder + file);
                        File fileSend = new File(System.getProperty("user.dir") + Folder + file);
                        int fileSize = (int) fileSend.length();
                        System.out.println("to "+IpClient+" size file: "+fileSize);
//-->start time
                        timeStart=System.currentTimeMillis();
                        
                        bWrite.write(file);
                        bWrite.newLine();
                        bWrite.flush();
                        //send size file
                        dos.writeInt(fileSize);
                        dos.flush();
                        System.out.println("to "+IpClient+": Send header done");
                        sendFile(this.socketReceive, fileSend);
                    }
                } else {
                	//send IP newServer
                    bWrite.write(IpClient);
                    bWrite.newLine();
                    bWrite.flush();
                    System.out.println("to "+IpClient+": Send ip client to other");
//-->caculator time send file
                    dis.readInt();
                    timeEnd=System.currentTimeMillis();
                    long time=timeEnd-timeStart;
                    System.out.println("time send file to client: "+IpClient+": " + time );
                    if(time>timeSend) timeSend=time;
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        private static boolean sendFile(Socket sendToSocket, File file) throws IOException {
            try {
                DataOutputStream dos = new DataOutputStream(sendToSocket.getOutputStream());
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[sizeBuffered];

                System.out.println("Start transfer file...");

                int fileSize = (int) file.length(), current = 0;
                int byteRead;
                do {
                    byteRead = fis.read(data);
                    dos.write(data, 0, byteRead);
                    dos.flush();
                    if (byteRead >= 0) {
                        current += byteRead;
                    }
                } while (current != fileSize);
                System.out.println("ok");

                System.out.println("to "+sendToSocket.getInetAddress().getHostAddress()+"Transfer Done");
                fis.close();

                return true;
            } catch (Exception e) {
                Logger.getLogger(ServiceThread.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        }
    }
}
