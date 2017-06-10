import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class Server_project {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        Socket socket = null;
        int sessionId = 0;
        
        try {
            serverSocket = new ServerSocket(8781);
            System.out.println("Server is Running");
            try {
                Game game = new Game();
                while(true) {
                    sessionId++;
                    System.out.println("Waiting for connections...");
                    Game.Player playerX = game.new Player(serverSocket.accept(), sessionId);
                    
                    playerX.start();
                    //System.out.println("Connection accepted.");
                }
            } finally {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("already started");
        }
    }
}

class Game {
    public HashMap<String, Player> players = new HashMap<String, Player>();
    Game game = null;
    
    public int maxCount = 3;
    public int maxX = 20;
    public int maxY = 20;
    public int counterMove = 0;
    public int round = 0;
    public boolean hasWinner = false;
    public int inActiveCount = 0 ;

    int [][] board = new int[maxX][maxY];
    Player currentPlayer;
    
    public Game(){
        this.game = this;
    }
    
    Timer timer=new Timer();
    TimerTask task=new MyTimer();
    
    public boolean boardEmpty() {
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                board[i][j] = 0;
            }
        }
        return true;
    }
    
    public void startGame(){
        System.out.println("NEW GAME");
        round++;
        boardEmpty();
        for(Player player : players.values() ){
            player.active = 1;
            player.x = 0 + (int) (Math.random() * maxX);
            player.y = 0 + (int) (Math.random() * maxY);
        }
    }
    
    class MyTimer extends TimerTask{
        public void run() {
            System.out.println("go timer");
            counterMove++;
            handleUserInput();
            recalcBoard();
            sendBoard();

            if(hasWinner || inActiveCount == players.size() ) {
                hasWinner = false;
                if(round>5)
                    sendWinnerList();
                else startGame();
            }
        }
    }
    
    public boolean addPlayer(Player player)
    {
        players.put(player.loginName, player);
        player.output.println(player.sessionId);
        
        if(players.size() == maxCount){
            startGame();
            sendPlayerList();
            timer.schedule( task, 500, 1000);

            return true;
        }
        if(players.size() > maxCount) return false;
        else return true;
    }
    
    public void sendPlayerList() {
        String playersStr = "";
        String delimiter = "";
       
        for ( Player player : players.values() ) {
            playersStr += delimiter + "player " + player.sessionId + ": " + player.loginName + " ( coordinates " +player.x + " and " + player.y + " ) ";
            delimiter = ", ";
        }
        
        String playerSendBuff = "start;GAME STARTED with: " + playersStr ;

        for( Player player : players.values() ){
            player.output.println(playerSendBuff);
        }
    }
    
    public void handleUserInput(){
        for( Player player : players.values() ){
            if(!player.inputLast.isEmpty()){
                //System.out.println("MESSAGE FROM CLIENT" + player.sessionId + player.inputLast);
                player.dir = player.inputLast;
            }
        }
    }
    
    public void sendBoard() {
        String buff  = "";
        for(int i = 0; i < maxX; i++){
            for(int j = 0; j < maxY; j++){
                buff += board[i][j];
            }
        }
        
        for( Player player : players.values() ){
            if(!player.inputLast.isEmpty()){
                String playerSendBuff = "";
                
                if( player.active == 1 ){
                    playerSendBuff = "board;" + player.lastOperationStatus + ";" + buff + ";" + counterMove + ";" +round;

                    player.output.println(playerSendBuff);
                    player.lastOperationStatus = "";
                    player.inputLast = "";
                }
                else if(player.active == 0) {
                    playerSendBuff = "failed;" + player.lastOperationStatus + ";" + buff + ";" + counterMove + ";" +round + ";" + player.x + ", " + player.y;
                    
                    player.output.println(playerSendBuff);
                    player.lastOperationStatus = "";//COUNT LAST POSITION
                    player.inputLast = "";
                    player.active = -1;
                }
                else if(player.active == 2){
                    System.out.println("we are in 2 activity status");
                    player.points++;
                    playerSendBuff = "win;" + player.lastOperationStatus + ";" + buff + ";" + counterMove + ";" +round +";"+player.points;
                    player.output.println(playerSendBuff);
                    player.lastOperationStatus = "";
                    player.inputLast = "";
                    player.active = -1;
                }
            }
        }
   }

    public void recalcBoard(){
        inActiveCount = 0;
        
        for ( Player player : players.values() ) {
            System.out.println("ACT " + player.sessionId + " "+ player.active +" X:"+player.x+" y:"+player.y);

            if(player.active < 1){
                inActiveCount++;
            }
            
            if(player.dir.length() > 0 && player.active==1){
               char direction = player.dir.charAt(0);
                
                switch(direction){
                    case 'S':{
                        if(validMove( player, player.x, player.y+1 )){
                            player.y = player.y+1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'N':{
                        if(validMove( player, player.x, player.y-1 )){
                            player.y = player.y-1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'E':{
                        if(validMove( player, player.x+1, player.y )){
                            player.x = player.x+1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'W':{
                        if(validMove( player, player.x-1, player.y )){
                            player.x = player.x-1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    default: {
                        player.lastOperationStatus = "not correct symbol";
                    }
                }
                board[player.x][player.y] = player.sessionId;
            }
        }
        hasWinner();

    }
    
    public void hasWinner(){
        System.out.println("inAct "+inActiveCount + " "+ players.size());
        
        if(inActiveCount == players.size()-1 ){
            System.out.println("WIN!!!");

            hasWinner = true;
            for( Player player : players.values() ){
                if(player.active == 1){
                    System.out.println("CHANGED TO 2");
                    player.active = 2;
                }
            }
        }
    }
    
    public boolean validMove(Player player, int x, int y){
        if( x>=maxX || x<0 || y>=maxY || y<0){
            player.active = 0;
            System.out.println("VALID:"+player.active);
            return false;
        }
        else if( board[x][y] != 0){
            player.active = 0;
            System.out.println("VALID:"+player.active);
            return false;
        }
        else return true;
    }
    
    public void sendWinnerList(){ //MAKE WINNER LIST
        String buff;
        for( Player player : players.values() ){
            //max(player.points);
        }
    }
    
    class Player extends Thread {
        public int sessionId;
        Player playerX;
        Socket socket;
        BufferedReader input;
        public PrintWriter output;
        String name;
        int win;
        int lost;
        public String loginName = "";
        public int x;
        public int y;
        public String dir = "";
        public String inputLast = "";
        public String lastOperationStatus = "";
        public int active = -1;
        public int points = 0;
       
        public Player(){
        }

        public Player(Socket socket, int sessionId) {
            this.socket = socket;
            this.name= "";
            this.sessionId = sessionId;
        }
     
        public void run(){
            try {
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("You are connected");
                output.println(maxX);
                output.println(maxY);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.loginName = reader.readLine();
               
                System.out.println("CONNECTION " + sessionId + " from " + loginName);
               
                if( game.addPlayer(this) ) { }
                else {
                    output.println("Sorry. The game already started");
                    this.socket.close();
                }

                while(true){
                    inputLast = reader.readLine();
                }
                
                
            } catch (IOException e) {
                System.out.println("Player died: " + this.loginName);
            }
        }
    }
}
    
        /*   
         ДАЛЬШЕ НЕ МОЕ. ПРОСТО КОММЕНТАРИИ. ЕЩЕ НЕ РАЗБИРАЛА
         
         public synchronized boolean legalMove(int location, Player player, PrintWriter output) {
         if (player == currentPlayer && board[location] == null) {
         board[location] = currentPlayer;
         //currentPlayer = currentPlayer.opponent;
         //currentPlayer.otherPlayerMoved(location);
         
         output.println("OK");
         if (hasWinner() || boardFilledUp()) {
         output.println(hasWinner() ? "WIN"
         : boardFilledUp() ? "WIN"
         : "");
         currentPlayer.win++;
         return true;
         }
         
         else// (!hasWinner() && !boardFilledUp())
         {
         Random r = new Random();
         int ile=r.nextInt(25);
         while(board[ile] != null) ile=r.nextInt(25);
         board[ile] = currentPlayer.opponent;
         currentPlayer.otherPlayerMoved(ile);
         }
         
         
         return true;
         }
         return false;
         }
         */
      /*  public void otherPlayerMoved(int location) {
            output.println("OPPONENT " + location);
            if (hasWinner() || boardFilledUp()) {
                output.println(hasWinner() ? "LOST" : boardFilledUp() ? "WIN" : "");
                this.lost++;
            }
        }
        */
        /*!!*/ /*public boolean hasWinner() {
                return
                (board[0] != null && board[0] == board[1] && board[0] == board[2] && board[0] == board[3] && board[0] == board[4])
                ||(board[5] != null && board[5] == board[6] && board[5] == board[7] && board[5] == board[8] && board[5] == board[9])
                ||(board[10] != null && board[10] == board[11] && board[10] == board[12] && board[10] == board[13] && board[10] == board[14])
                ||(board[15] != null && board[15] == board[16] && board[15] == board[17] && board[15] == board[18] && board[15] == board[19])
                ||(board[20] != null && board[20] == board[21] && board[20] == board[22] && board[20] == board[23] && board[20] == board[24])
                ||(board[0] != null && board[0] == board[5] && board[0] == board[10] && board[0] == board[15] && board[0] == board[20])
                ||(board[1] != null && board[1] == board[6] && board[1] == board[11] && board[1] == board[16] && board[1] == board[21])
                ||(board[2] != null && board[2] == board[7] && board[2] == board[12] && board[2] == board[17] && board[2] == board[22])
                ||(board[3] != null && board[3] == board[8] && board[3] == board[13] && board[3] == board[18] && board[3] == board[23])
                ||(board[4] != null && board[4] == board[9] && board[4] == board[14] && board[4] == board[19] && board[4] == board[24])
                ||(board[0] != null && board[0] == board[6] && board[0] == board[12] && board[0] == board[18] && board[0] == board[24])
                ||(board[4] != null && board[4] == board[8] && board[4] == board[12] && board[4] == board[16] && board[4] == board[20]);
                }*/
        
      /*  public void run() {
            try {
                // The thread is only started after everyone connects.
                //output.println("MESSAGE All players connected");
                
                // Tell the first player that it is her turn.
                //if (mark == 'X') {
                //output.println("MESSAGE Your move");
                //}
                
                // Repeatedly get commands from the client and process them.
                while (this.lost+this.win<200) {
                    String command = input.readLine();
                    int location =-1;
                    if (this.name.length()>0 && command.startsWith("MOVE")) {
                        try {
                            location = Integer.parseInt(command.substring(5));
                        } catch(Exception e)
                        {  }
                        if(location <0 || location>=25)  output.println("ERROR");
                        else
                            if (legalMove(location, this, output)) {
                                
                            } else {
                                output.println("ERROR");
                            }
                    } else if (command.startsWith("LOGIN")) {
                        output.println("OK");
                        name = command.substring(5, command.length());
                    } else if (command.startsWith("QUIT")) {
                        return;
                    } else {
                        output.println("ERROR");
                    }
                    for(int i=0;i<25;i++)
                    {
                        if(board[i] == null)
                            System.out.print(" ");
                        else
                            if(board[i] == this)
                                System.out.print(".");
                            else System.out.print("+");
                    }
                    System.out.println("<-");
                    if(this.lost+this.win<200 && (hasWinner() || boardFilledUp())) {
                        for(int i=0;i<25;i++) board[i]=null;
                        output.println("NEW GAME");
                        
                    }
                }
                if(this.win>=175) {
                    try{
                        output.println("SUCCESS");
                        String filename= "wyniki.txt";
                        FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                        fw.write(this.name + '\n');//appends the string to the file
                        fw.close();
                        
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(this.name + " " + this.win + " " + this.lost);
                }
                else
                {
                    output.println("FAILED");
                    System.out.println(this.name + " " + this.win + " " + this.lost);
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                
                
                try {socket.close();} catch (IOException e) {}
            }
        }*/

