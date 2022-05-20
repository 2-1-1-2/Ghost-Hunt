import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;

/*
A envoyer
OK GAMES nbGames*** -> listGames()
OK     OGAME game.numGame game.nbPlayers*** -> listGames()

OK REGOK game.numGame*** -> register()
OK REGNO*** -> register()
OK UNROK game.numGame*** -> unregister()
OK SIZE! game.numGame game.height game.width*** -> sizeMaze(numGame)
OK LIST! game.numGame game.nbPlayers*** -> listPlayers(numGame)
OK     PLAYR player.username*** -> listPlayers(numGame)
OK DUNNO*** -> dunno()

OK WELCO g.numGame g.height g.width g.nbGhostsRemain g.ip g.portMultiD*** -> sendWelcome(numGame)
OK POSIT player.username player.row player.col*** -> start()


OK MOVE! player.row player.col*** -> moveRes()
OK MOVEF player.row player.col player.score*** -> moveRes()

OK GHOST ghost.row ghost.col+++ -> moveGhost()
OK SCORE player.username player.score player.row player.col+++ -> addPlayer(player, row, col)

OK EXTON*** -> activeExtension()
OK EXTOF*** -> closeExtension()

OK GETIT*** -> moveRes()
OK ATKPL*** -> moveRes()
OK DRPOK*** -> dropItem()

OK NOITM*** -> dropItem() ou checkItem()
OK BOMBE*** -> checkItem()
OK LAMPE*** -> checkItem()
OK RADAR*** -> checkItem()

OK MURUP*** -> useLampe()
OK MURDO*** -> useLampe()
OK MURLE*** -> useLampe()
OK MURRI*** -> useLampe()

OK FNDGH x y*** -> useRadar()


OK GLIS! game.nbPlayers*** -> listPlayersCurrent()
OK     GPLYR player.username player.row player.col player.score*** -> listPlayersCurrent()

OK MESSA playerSender.username message*** -> messageToAll(message)
OK MALL!*** -> messageToAll(message)
OK MESSP playerSender.username message*** -> sendToPlayer(id, message)
OK SEND!*** -> sendToPlayer(id, message)
OK NSEND*** -> sendToPlayer(id, message)

OK GOBYE*** -> quit()
OK ENDGA playerWinner.username playerWinner.score+++ -> noMoreGhost()
*/

public class Server{
    private static ArrayList<Game> games=new ArrayList<Game>();
    private static LinkedList<ServiceClient> connectedUsers=new LinkedList<ServiceClient>();

    static void addClient(ServiceClient client){
        connectedUsers.add(client);
    }
    
    static void removeClient(ServiceClient client){
        connectedUsers.remove(client);
    }

    synchronized static int getNbClients(){
        return connectedUsers.size();
    }

    /* FONCTIONS DE MODIF SUR LES PARTIES */
    //creation d'une nouvelle partie apres NEWPL
    synchronized static Game createGame(Player p){
        Game g=new Game(p);
        games.add(g);
        return g;
    }
    
    //ajout du joueur suite a REGIS
    synchronized static Game addInGame(Player p, int numGame){
        Game g=games.get(numGame);
        g.addPlayerInGame(p);
        return g;
    }

    synchronized static int getNbGames(){
        return games.size();
    }
    /* FIN FONCTIONS DE MODIF SUR LES PARTIES */

    
    /* FONCTIONS TESTS DE VALIDITE */
    static boolean idOk(String id, ServiceClient c){
        for(ServiceClient client:connectedUsers){//check qu'il n'y a pas de joueur qui porte deja ce pseudo
            if(client.getID()!=null && client!=c && client.getID().equals(id)) return false;
        }
        return id.matches("[a-zA-Z0-9]{8}");
    }
    /* FIN FONCTIONS TESTS DE VALIDITE */
    
    
    /* FONCTIONS DE CONVERSION */
    //convertit un entier en un String de n char, avec des 0 au debut
    static String intToNChar(int toConvert, int n){
        String res=Integer.toString(toConvert);
        while(res.length()<n) res="0"+res;
        return res;
    }
    
    //convertit un entier en little endian
    static byte[] intToLE(int n){
        ByteBuffer bb = ByteBuffer.allocate(2); 
        bb.putShort((short)n);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.array();
    }
    /* FIN FFONCTIONS DE CONVERSION */

    
    /* FONCTIONS D'INFORMATION SUR LES PARTIES */
    synchronized static boolean gameExists(int numGame){
        for(Game g: games)
            if(g.getNum()==numGame) return true;
        return false;
    }

    //tester si tous les joueurs ont envoye START
    synchronized static boolean canStart(int numGame){
        return games.get(numGame).canStart();
    }

    //premiere fonction a executer, quand le joueur se connecte
    synchronized static String listGames(){
        String toSend="GAMES "+(byte)getNbGames()+"***";
        for(Game g: games) toSend+="OGAME "+(byte)g.getNum()+" "+(byte)g.getNbPlayers()+"***";
        return toSend;
    }

    //reponse a SIZE?
    static String sizeMaze(int numGame){
        Game g=games.get(numGame);
        byte[] hBytes=intToLE(g.getHeight());
        byte[] wBytes=intToLE(g.getWidth());
        return "SIZE! "+(byte)numGame+" "+hBytes[0]+hBytes[1]+" "+wBytes[0]+wBytes[1]+"***";
    }

    //reponse a LIST?
    synchronized static String listPlayers(int numGame){
        Game g=games.get(numGame);
        String toSend="LIST! "+(byte)numGame+" "+(byte)g.getNbPlayers()+"***";
        toSend+=g.getListPlayers();
        System.out.println(toSend);
        return toSend;
    }
    
    static LinkedList<ServiceClient> getPlayers(Game game){
        LinkedList<ServiceClient> res=new LinkedList<ServiceClient>();
        for(ServiceClient servC : connectedUsers)
            if(servC.sameGame(game)) res.add(servC);
        return res;
    }
    
    static ServiceClient getServiceClient(String id, Game game){
        for(ServiceClient servC : connectedUsers)
            if(servC.isPlayer(id, game)) return servC;
        return null;
    }
    /* FIN FONCTIONS D'INFORMATION */

    
    static String sendWelcome(int numGame){
        Game g=games.get(numGame);
        games.remove(g.gameStart());
        byte[] hBytes=intToLE(g.getHeight());
        byte[] wBytes=intToLE(g.getWidth());
        return "WELCO "+(byte)numGame+" "+hBytes[0]+hBytes[1]+" "+wBytes[0]+wBytes[1]+" "
                +(byte)g.getNbGhosts()+" "+g.getIP()+" "+g.getPort()+"***";
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