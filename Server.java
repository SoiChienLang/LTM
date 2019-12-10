
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static ServerSocket listener = null;
    private static int clientNumber = 0;
    private static String IpClient = null;

    public static void main(String args[]) throws IOException {
        try {
            listener = new ServerSocket(12345);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            while (true) {
                clientNumber++;
                boolean isNewServer = false;
                System.out.println("Server is waiting to accept user...");
                Socket socketOfServer = listener.accept();
                System.out.println(socketOfServer.getInetAddress().getHostAddress());
                if (clientNumber == 1) {
                    isNewServer = true;
                    IpClient = socketOfServer.getInetAddress().getHostAddress();
                }
                ServiceThread st = new ServiceThread(socketOfServer, isNewServer);
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
        private boolean isNewServer = false;
        private BufferedWriter bWrite;
        private Scanner sc;

        public ServiceThread(Socket socketOfServer, boolean statusServer) {
            this.socketReceive = socketOfServer;
            this.isNewServer = statusServer;
        }

        @Override
        public void run() {
            try {
                DataOutputStream dos = new DataOutputStream(socketReceive.getOutputStream());
                bWrite = new BufferedWriter(new OutputStreamWriter(this.socketReceive.getOutputStream()));;
                if (isNewServer == true) {
                    sc = new Scanner(System.in);
                    System.out.println("name file:");
                    String file = sc.nextLine();

                    if (!(new File(System.getProperty("user.dir") + Folder + file)).exists()) {
                        System.out.println("File is not exist");
                    } else {
                        System.out.println(System.getProperty("user.dir") + Folder + file);
                        File fileSend = new File(System.getProperty("user.dir") + Folder + file);
                        int fileSize = (int) fileSend.length();
                        System.out.println(fileSize);
                        bWrite.write(file);
                        System.out.println(file);
                        bWrite.newLine();
                        bWrite.flush();

                        dos.writeInt(fileSize);
                        dos.flush();
                        System.out.println("Send header done");
                        sendFile(this.socketReceive, fileSend);
                    }
                } else {
                    bWrite.write(IpClient);
                    bWrite.newLine();
                    bWrite.flush();
                    System.out.println("Send ip client to other");
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

                System.out.println("Transfer Done");
                fis.close();

                return true;
            } catch (Exception e) {
                Logger.getLogger(ServiceThread.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        }
    }
}
