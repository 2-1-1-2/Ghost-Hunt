import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/*
TODO:
OK GAMES nbGames***
OK     OGAME game.numGame game.nbPlayers***

X REGOK game.numGame***
X REGNO***

X UNROK game.numGame***
OK SIZE! game.numGame game.height game.width***
OK LIST! game.numGame game.nbPlayers***
OK     PLAYR player.username***
X DUNNO***

OK WELCO game.numGame game.height game.width game.nbGhostsRemain game.ip game.portMultiD***
X POSIT player.username player.row player.col***

X MOVE! player.row player.col***
X MOVEF player.row player.col player.score***

X GODBYE***

X GLIS! game.nbPlayers***
X     GPLYR player.username player.row player.col player.score***

X MESSA playerReceiver.username message***
X MALL!***
X MESSP playerSender.username message***
X SEND!***
X NSEND***

X GHOST ghost.row ghost.col+++
X SCORE player.username player.score player.row player.col+++
X ENDGA playerWinner.username playerWinner.score+++
*/

public class Server{
    //tout en static puisque de toute facon 1 serveur pour tous ?
    private static ArrayList<Game> games=new ArrayList<Game>();
    private static int nbGames=0;
    private static LinkedList<ServiceClient> connectedUsers=new LinkedList<ServiceClient>();
    private static int nbClients=0;

    synchronized static void incClients(){
        nbClients++;
    }

    synchronized static int getNbClients(){
        return nbClients;
    }

    /* FONCTIONS DE MODIF SUR LES PARTIES */
    synchronized static void incGames(){
        nbGames++;
    }

    synchronized static int getNbGames(){
        return nbGames;
    }

    //ajout du joueur suite a REGIS
    synchronized static Game addInGame(Player p, int numGame){
        Game g=games.get(numGame);
        g.addPlayer(p);
        return g;
    }

    //creation d'une nouvelle partie apres NEWPL
    synchronized static Game addGame(Player p){
        Game g=new Game(p);
        games.add(g);
        return g;
    }
    /* FIN FONCTIONS DE MODIF SUR LES PARTIES */

    
    /* FONCTIONS TESTS DE VALIDITE */
    static boolean idOk(String id){
        for(ServiceClient client:connectedUsers){//check qu'il n'y a pas de joueur qui porte deja ce pseudo
            if(client.getID()!=null && client.getID().equals(id))
                return false;
        }
        return id.matches("[a-zA-Z0-9]{8}");
    }
    /* FIN FONCTIONS TESTS DE VALIDITE */

    
    /* FONCTIONS D'INFORMATION SUR LES PARTIES */
    synchronized static boolean gameExists(int numGame){
        for(Game g: games){
            if(g.getNum()==numGame) return true;
        }
        return false;
    }

    //tester si tous les joueurs ont envoye START
    synchronized static boolean canStart(int numGame){
        for(Player p: games.get(numGame).getListPlayers()){
            if(!p.sentStart()) return false;
        }
        return true;
    }

    //premiere fonction a executer, quand le joueur se connecte
    synchronized static String listGames(){
        String toSend="GAMES "+(byte)nbGames+"***";
        for(Game g: games)
            toSend+="OGAME "+(byte)(g.getNum())+" "+(byte)(g.getNbPlayers())+"***";
        return toSend;
    }

    //reponse a SIZE?
    static String sizeMaze(int numGame){
        Game g=games.get(numGame);
        int h=g.getHeight();
        byte[] hBytes=new byte[]{(byte)(h & 0xFF), (byte)((h>>8) & 0xFF)};
        int w=g.getWidth();
        byte[] wBytes=new byte[]{(byte)(w & 0xFF), (byte)((w>>8) & 0xFF)};
        return "SIZE! "+(byte)numGame+" "+hBytes[0]+hBytes[1]+" "+wBytes[0]+wBytes[1]+"***";
    }

    //reponse a LIST?
    synchronized static String listPlayers(int numGame){
        Game g=games.get(numGame);
        String toSend="LIST! "+(byte)numGame+" "+(byte)(g.getNbPlayers())+"***";
        for(Player p: g.getListPlayers())
            toSend+=p;//appel a p.toString()
        return toSend;
    }
    /* FIN FONCTIONS D'INFORMATION */

    
    static String sendWelcome(int numGame){
        Game g=games.get(nbGames);
        g.gameStart();
        int h=g.getHeight();
        byte[] hBytes=new byte[]{(byte)(h & 0xFF), (byte)((h>>8) & 0xFF)};
        int w=g.getWidth();
        byte[] wBytes=new byte[]{(byte)(w & 0xFF), (byte)((w>>8) & 0xFF)};
        String toSend="WELCO "+(byte)numGame+" "+hBytes[0]+hBytes[1]+" "+wBytes[0]+wBytes[1]+" "
                +(byte)(g.getNbGhosts())+" "+g.getIP()+" "+g.getPort()+"***";
        return toSend;
    }

    public static void main(String[] args){
        int portTCP=6666;//un port par defaut?
        if(args.length>0){
            try{
                portTCP=Integer.parseInt(args[0]);
            }
            catch(NumberFormatException e){
                System.err.println("non recognized port number");
            }
        }

        try{
            ServerSocket server=new ServerSocket(portTCP);
            while(true){
                Socket sockClient=server.accept();
                ServiceClient client=new ServiceClient(sockClient);
                Thread th=new Thread(client);
                th.start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
            System.err.println("error creating server");
        }
    }
}