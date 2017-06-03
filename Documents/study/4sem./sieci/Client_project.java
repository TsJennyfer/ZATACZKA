import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.ConnectException;

public class Client_project {
    
    public static void printBoard(String buff)
    {
        //why exception? why can't do on 0?
        for (int i = 0 ; i < maxX ; i++){
            for (int j = 0 ; j < maxY ; j++){
                System.out.print( buff.charAt( i * maxX + j ));
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    
    public static int maxX;
    public static int maxY;
    
    public static void main(String[] args) throws Exception {
        Scanner keyboard = new Scanner(System.in);
        
        String myName = "";
        String dir = "";
        
        System.out.println("Hello! Welcome to Zataczka game! We need only 8 people to start, so let's go! ");
        
        
        try{
            Socket clientSocket = new Socket("localhost", 8780);
            
            System.out.println("Connection started");
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println(">" + inFromServer.readLine());
            
            maxX = Integer.parseInt(inFromServer.readLine());
            maxY = Integer.parseInt(inFromServer.readLine());
            
            while(myName.isEmpty()){
                System.out.println("LOGIN" + "(put your name) :");
                myName = keyboard.nextLine();
                //the same name
            }
            
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            
            outToServer.writeBytes(myName + "\n");
            
            String buff = inFromServer.readLine();
            
            System.out.println(">" + buff);
            System.out.println(">" + inFromServer.readLine());
            System.out.println("your board is " + maxX + " * " + maxY);
            //System.out.println(">" + inFromServer.readLine());
            
           // System.out.println("the starting board is:");

            //buff = inFromServer.readLine();
          //  printBoard(buff);
            //System.out.println("BEGIN");

            while(true){
                System.out.println("put your move(use only W, E, S, N)");

                dir = keyboard.nextLine();
                outToServer.writeBytes(dir + "\n");
                
               // buff = null;
                //buff = inFromServer.readLine();
               // System.out.println(">" + buff);
                
                //String buff = "";
                String buff1 = inFromServer.readLine();
               // System.out.println(">" + buff1);
                printBoard(buff1);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused");
        }

        
    }
}
