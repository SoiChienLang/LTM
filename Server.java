import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.DataOutputStream;

public class Server {
	private static ServerSocket listener=null;
	private static int clientNumber=0;
	private static String IpClient=null;
	public static void main(String args[])throws IOException{
		try {
			listener=new ServerSocket(7777);
			System.out.println(listener.getInetAddress().getHostAddress());
		}catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}
		
		try {
			while(true) {
				clientNumber++;
				boolean isNewServer=false;
				System.out.println("Server is waiting to accept user...");
				Socket socketOfServer = listener.accept();
				System.out.println(socketOfServer.getInetAddress().getHostAddress());
				if(clientNumber==1) {
					isNewServer=true;
					IpClient=socketOfServer.getInetAddress().getHostAddress();
				}
				ServiceThread st =new ServiceThread(socketOfServer, isNewServer);
	            st.start();
			}
		}finally {
			listener.close();
		}
	}
	private static class ServiceThread extends Thread{
		private final static String Folder="\\Share Folder\\";
		private final static int sizeBuffered=2048;
		
		private Socket socketReceive;
		private boolean isNewServer=false;
		private BufferedWriter bWrite;
		private Scanner sc;
		
		public ServiceThread(Socket socketOfServer,boolean statusServer) {
			this.socketReceive=socketOfServer;
			this.isNewServer=statusServer;
		}
		@Override
		public void run() {
			try {
		        bWrite = new BufferedWriter(new OutputStreamWriter(this.socketReceive.getOutputStream()));;
				if(isNewServer==true) {
					sc = new Scanner(System.in);
					System.out.println("name file:");
					String file =sc.nextLine();
					System.out.println(System.getProperty("user.dir")+Folder+file);
					if(!(new File(System.getProperty("user.dir")+Folder+file)).exists()) {
						System.out.println("File is not exist");
					}
					else {
						File fileSend=new File(System.getProperty("user.dir")+Folder+file);
						
						bWrite.write(file);
	                    bWrite.newLine();
	                    bWrite.flush();
	                    
	                    bWrite.write((int)fileSend.length());
	                    bWrite.newLine();
	                    bWrite.flush();
	                    
	                    sendFile(this.socketReceive, fileSend);
					}
				}
				else {
					bWrite.write(IpClient);
                    bWrite.newLine();
                    bWrite.flush();
				}
			}catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		private static boolean sendFile(Socket sendToSocket,File file) throws IOException {
            try{
            	DataOutputStream dos = new DataOutputStream(sendToSocket.getOutputStream());
          		FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[sizeBuffered];
          		while (fis.read(buffer) > 0) {
          			dos.write(buffer);
          			//System.out.println(buffer.length);
          		}
          		fis.close();
          		System.out.println("ok");
          		return true;
            }catch (Exception e) {
            	System.out.println(e);
            	return false;
			}
	     }
	}
}
