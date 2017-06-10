import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.ConnectException;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.File;
import java.io.IOException;


public class Client_project {
    
    public static int maxX;
    public static int maxY;
    public static String filebuff = "";
    
    public static void write(String id){
 
        String namefile = "communication"+id+".txt";

        try{
            PrintWriter writer = new PrintWriter(namefile, "UTF-8");
            writer.println("File writing started. ");
            writer.println(filebuff);
            writer.println("File writing ended. ");
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
    
    public static void printBoard(String buff)
    {
        for (int i = 0 ; i < maxX ; i++){
            for (int j = 0 ; j < maxY ; j++){
                System.out.print( buff.charAt( i * maxX + j ));
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) throws Exception {
        Scanner keyboard = new Scanner(System.in);
        
        
        String myName = "";
        String dir = "";
        String buff = "";
        String nextGameInd = "";
        
        buff = "Hello! Welcome to Zataczka game! We need only 8 people to start, so let's go! ";
        System.out.println(buff);
        filebuff += buff +" \n";
        
        try{
            Socket clientSocket = new Socket("localhost", 8781);
            
            buff = "Connection started";
            System.out.println(buff);
            filebuff +=  buff + " \n";
            
            ///new round
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            buff = ">" + inFromServer.readLine();
            
            System.out.println(buff);
            filebuff += buff + "\n";
            
            maxX = Integer.parseInt(inFromServer.readLine());
            maxY = Integer.parseInt(inFromServer.readLine());
            
            while(myName.isEmpty()){
                buff = "LOGIN" + "(put your name) :";
                System.out.println(buff);
                filebuff += buff + "\n";
                myName = keyboard.nextLine();
                //the same name
            }
            
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            
            outToServer.writeBytes(myName + "\n");
            filebuff += myName + "\n";
            
            String id = inFromServer.readLine();
            
            buff = ">ok. wait next indications.";
            System.out.println(buff);
            filebuff += buff + "\n";
            
            while(true){
                
                    String buffFromServer = "";
                
                    buffFromServer = inFromServer.readLine();
                    //System.out.println(">" + buff);

                    String[] parts = buffFromServer.split(";");
                    if(parts[0].equals("start"))
                    {
                        buff = ">WE START NEW GAME. "+parts[1];
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = ">your id is " + id + ".";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = "your board is " + maxX + " * " + maxY;
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        
                        buff = "put your move(use only W, E, S, N)";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        dir = keyboard.nextLine();
                        outToServer.writeBytes(dir + "\n");
                        
                        filebuff += dir + "\n";
                        
                    }
                    else if(parts[0].equals("board"))
                    {
                        buff = ">" + parts[1];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        printBoard(parts[2]);
                        //board to file
                        
                        buff = "move counter - " + parts[3];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        buff = "round - " + parts[4];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        buff = "put your move(use only W, E, S, N)";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        dir = keyboard.nextLine();
                        outToServer.writeBytes(dir + "\n");
                        
                        filebuff += dir + "\n";
                    }
                    else if(parts[0].equals("failed"))
                    {
                        buff = ">"+parts[1];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        printBoard(parts[2]);
                        //board to file

                        buff = "move counter - " + parts[3];
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = "round - " + parts[4];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        buff = "last position - " + parts[4];
                        System.out.println(buff);
                        filebuff += buff + "\n";

                        buff = "FAILED. I'm sorry. you failed. wait your game";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                      //  dir = keyboard.nextLine();
                      //  outToServer.writeBytes(dir + "\n");
                        
                      //  filebuff += dir + "\n";


                    }
                    else if(parts[0].equals("win"))
                    {
                        buff =">" + parts[1];
                        buff = "WIN. wait your game";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        printBoard(parts[2]);
                        //board to file
                        
                        buff = "move counter - " + parts[3];
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = "round - " + parts[4];
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = "you get " + parts[4] + " point in this game";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                        buff = "WIN. wait your game";
                        System.out.println(buff);
                        filebuff += buff + "\n";
                        
                      //  dir = keyboard.nextLine();
                     //   outToServer.writeBytes(dir + "\n");
                     //
                      //  filebuff += dir + "\n";

                    }

                    else if(parts[0].equals("winnerList"))
                    {
                        buff =">" + parts[1];
                        filebuff += buff + "\n";
                    }
                
                
                write(id);
                
            }
            
        } catch (ConnectException e) {
            System.out.println("Connection refused");
        }
    }
}
