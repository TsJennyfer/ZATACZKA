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
    
    public boolean isBoardEmpty() {
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
        isBoardEmpty();
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
                        if(isValidMove( player, player.x, player.y+1 )){
                            player.y = player.y+1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'N':{
                        if(isValidMove( player, player.x, player.y-1 )){
                            player.y = player.y-1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'E':{
                        if(isValidMove( player, player.x+1, player.y )){
                            player.x = player.x+1;
                            player.lastOperationStatus = "ok";
                        }
                        else player.lastOperationStatus = "failed";
                        break;
                    }
                    case 'W':{
                        if(isValidMove( player, player.x-1, player.y )){
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
    
    public boolean isValidMove(Player player, int x, int y){
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