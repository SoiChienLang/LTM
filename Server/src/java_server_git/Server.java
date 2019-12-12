
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
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.chart.PieChart;

public class Server {

    private static ServerSocket listener = null;
    private static String IpClient = null;
    public static int SIGNAL = 1;
    public static int number_client = 0;
    public static void main(String args[]) throws IOException {
        try {
            listener = new ServerSocket(12345);
            System.out.println("Server is waiting to accept user...");
            while (number_client < 3) {
                Socket socket = listener.accept();
                send_signal(socket, SIGNAL);
                if(SIGNAL == 1)
                    IpClient = socket.getLocalAddress().getHostAddress();
                ServiceThread service_thread = new ServiceThread(socket, SIGNAL);
                service_thread.start();
                SIGNAL = 0;
                number_client++;
            }
        } finally {
            listener.close();
        }
    }

    public static void send_signal(Socket socket, int SIGNAL) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(SIGNAL);
        dos.flush();
    }

    private static class ServiceThread extends Thread {

        private final static String Folder = "Share Folder\\";
        private final static int sizeBuffered = 180000;

        private final Socket socketReceive;
        private final int signalServer;
        private BufferedWriter bWrite;
        private Scanner sc;

        public ServiceThread(Socket socketOfServer, int signalServer) {
            this.socketReceive = socketOfServer;
            this.signalServer = signalServer;
        }

        @Override
        public void run() {
            try {
                System.out.println("\t\t\t\t\t\t\t\tConnect with CLIENT: " + socketReceive.getInetAddress().getHostAddress());
                DataOutputStream dos = new DataOutputStream(socketReceive.getOutputStream());
                DataInputStream dis = new DataInputStream(socketReceive.getInputStream());
                bWrite = new BufferedWriter(new OutputStreamWriter(socketReceive.getOutputStream()));
                if (signalServer == 1) {
                    sc = new Scanner(System.in);
                    System.out.println("Press name of software update transfer to clients: ");
                    String file_name = sc.nextLine();
                    
                    Counter_Timer.start_timer();
                    
                    
                    if (!(new File(Folder + file_name)).exists()) {
                        System.out.println("File is not exist");
                        return;
                    }
                    File file = new File(Folder + file_name);

                    bWrite.write(file_name);
                    bWrite.newLine();
                    bWrite.flush();
                    System.out.println("software_update_name: "+file_name);
                    
                    
                    int fileSize = (int) file.length();
                    dos.writeInt(fileSize);
                    dos.flush();
                    System.out.println("file_size: "+fileSize);
                    
                    sendFile(socketReceive, file);

                } else {
                    bWrite.write(IpClient);
                    bWrite.newLine();
                    bWrite.flush();
                }
                
                int signal_from_client = dis.readInt();
                if(signal_from_client == 1){
                    Counter_Timer.count_client_response();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean sendFile(Socket sendToSocket, File file) throws IOException {
            try {
                OutputStream os = sendToSocket.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[sizeBuffered];

                System.out.println("Start transfer file...");

                int fileSize = (int) file.length(), current = 0;
                int byteRead;
                do {
                    byteRead = fis.read(data);
                    os.write(data, 0, byteRead);
                    os.flush();
                    if (byteRead >= 0) {
                        current += byteRead;
                    }
                } while (current != fileSize);

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
class Counter_Timer{
    public static long start, end;
    public static float total_time;
    public static int number_client_response = 0;
    public static void start_timer(){
        start = System.nanoTime();
        Date date = new Date();
        System.out.println("Start at: " + date.toGMTString());
    }
    public static void count_client_response(){
        number_client_response++;
        if(number_client_response == 3){
            end = System.nanoTime();
            total_time = end - start;
            total_time = total_time/1000000000;
            System.out.println("SEND SOFTWARE UPDATE TO CLIENTS WITH TIME: " + total_time + " s");
        }
    }
}