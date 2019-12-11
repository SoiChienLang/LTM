package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
 
public class server {
	private static ServerSocket listener=null;
   public static void main(String args[]) throws IOException {
       int clientNumber = 0;
       String workingDir = System.getProperty("user.dir");
       System.out.println(workingDir);
       try {
           listener = new ServerSocket(7777);
       } catch (IOException e) {
           System.out.println(e);
           System.exit(1);
       }
 
       try {
           while (true) {
        	   System.out.println("Server is waiting to accept user...");
               Socket socketOfServer = listener.accept();
               ServiceThread st =new ServiceThread(socketOfServer, clientNumber++);
               st.start();
           }
       } finally {
           listener.close();
       }
 
   }
 
   private static class ServiceThread extends Thread {
       private Socket socketOfServer;
 
       public ServiceThread(Socket socketOfServer, int clientNumber) {
           this.socketOfServer = socketOfServer;
       }
 
       @Override
       public void run() {
           try {
               BufferedReader is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
               BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
               String line = is.readLine(); 
               System.out.println("client : " + line);
               while (true) {
            	   line="input: 'list-file' or 'file' or 'upload' or '@logout':";
 	        	   os.write(line);
 	        	   os.newLine();
 	        	   os.flush();
 	        	   System.out.println("me : " + line);
 	        	   
 	        	   line=is.readLine();
 	        	   System.out.println("client : " + line);
 	        	   
 	        	   if(line.equals("list-file")) {
 	        		   String workingDir = System.getProperty("user.dir");
 	        		   workingDir+="\\SharedFolder";
 	        		   File dir=new File(workingDir);
 	        		   String[] paths = dir.list();
 	        		   line="";
 	        		   for (String path : paths) {
 	        			   line+=path+" ; ";
 	        	       }
 	        		   System.out.println("me :"+line);
 	        		   os.write(line);
 	 	        	   os.newLine();
 	 	        	   os.flush();
 	        		   
 	 	        	 line =is.readLine();
		        	   System.out.println("client : " + line);
 	        	   }
 	        	   else if(line.equals("@logout")) {
                       os.write(">> OK");
                       os.newLine();
                       os.flush();
                       break;
                   }
 	        	   else if(line.equals("file")) {
 	        		   line="nhap ten file";
 	        		   os.write(line);
                       os.newLine();
                       os.flush();
                       System.out.println("me :"+line);
                       
                       line=is.readLine();
                       System.out.println("client : " + line);
                       
                       String workingDir = System.getProperty("user.dir");
            		   workingDir+="\\SharedFolder\\";
            		   workingDir+=line;
            		   File myFile = new File(workingDir);
            		   os.write(String.valueOf(myFile.length()));
                       os.newLine();
                       os.flush();
                       
 	        		  sendFile(workingDir, socketOfServer);
 	        	   }
 	        	   else if(line.equals("upload")) {
 	        		  line="nhap ten file";
	        		   os.write(line);
                      os.newLine();
                      os.flush();
                      System.out.println("me :"+line);
                      
                      line=is.readLine();
                      System.out.println("client : " + line);
                      
                      String workingDir = System.getProperty("user.dir");
                 		workingDir+="\\download\\";
                 		workingDir+=line;
                 		System.out.println("me :"+ workingDir);
                 		
                 		int length =Integer.valueOf(is.readLine());
 		        	   System.out.println("server : " + line);
 		        	   
 	                   saveFile(socketOfServer, workingDir,length);
 	        	   }
               }
           } catch (IOException e) {
               System.out.println(e);
               e.printStackTrace();
           }
       }
       private static void sendFile(String file,Socket s) throws IOException {
     		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
     		FileInputStream fis = new FileInputStream(file);
     		while (true) {
     			byte[] buffer = new byte[2048];
     			if(fis.read(buffer) > 0) {
     			dos.write(buffer);
     			System.out.println(buffer.length);
     			}
     			else break;
     		}
     		fis.close();
     		System.out.println("ok");
     	}
       private static void saveFile(Socket sock,String dir,int length) throws IOException {
   		byte[] data = new byte[2048];
   		System.out.println(length);
        int byteRead=0;
        int current = 0;
        File file = new File(dir);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is =sock.getInputStream();
        do{
            byteRead = is.read(data);
            bos.write(data,0, byteRead);
            if(byteRead >=0) current+=byteRead;
            System.out.println(current + " " + byteRead);
        }while(current != length);
        bos.flush();
   	}
   	}
   }
   
