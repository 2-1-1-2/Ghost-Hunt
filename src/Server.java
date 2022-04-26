import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class Server{
    //tout en static puisque de toute facon 1 serveur pour tous ?
    static ArrayList<Game> games=new ArrayList<Game>();
    static int nbGames=0;
    static LinkedList<ServiceClient> connectedUsers=new LinkedList<ServiceClient>();
    static int nbClients=0;

    /* FONCTIONS DE MODIF SUR LES PARTIES */
    synchronized static void incGames(){
        nbGames++;
    }

    synchronized static int getNbGames(){
        return nbGames;
    }

    synchronized static void addInGame(Player p, int numGame){
        
    }

    synchronized static void addGame(Game g){
        games.add(g);
    }
    /* FIN FONCTIONS DE MODIF SUR LES PARTIES */

    /* FONCTIONS TESTS DE VALIDITE */
    static boolean idOk(String id){
        for(int i=0; i<id.length(); i++){//check si c'est bien un alphanumerique (ASCII)
            if(!((id.charAt(i)>='a' && id.charAt(i)<='z') ||
                (id.charAt(i)>='A' && id.charAt(i)<='Z')) ||
                (id.charAt(i)>='0' && id.charAt(i)<='9'))
                return false;
        }
        for(ServiceClient client:connectedUsers){//check qu'il n'y a pas de joueur qui porte deja ce pseudo
            if(client.id.equals(id)) return false;
        }
        return true && id.length()==8;
    }
    /* FIN FONCTIONS TESTS DE VALIDITE */

    /* FONCTIONS D'INFORMATION SUR LES PARTIES */
    synchronized static boolean gameExists(int numGame){
        for(Game g: games){
            if(g.getNum()==numGame) return true;
        }
        return false;
    }

    synchronized static String listGames(){
        String toSend="GAMES "+nbGames+"***";
        for(Game g: games)
            toSend+="OGAME "+g.getNum()+" "+g.getNbPlayers()+"***";
        return toSend;
    }

    static String sizeMaze(int numGame){
        Game g=games.get(numGame);
        return "SIZE! "+numGame+" "+g.getHeight()+" "+g.getWidth()+"***";
    }

    synchronized static String listPlayers(int numGame){
        Game g=games.get(numGame);
        String toSend="LIST! "+numGame+" "+g.getNbPlayers()+"***";
        for(Player p: g.getListPlayers())
            toSend+=p;//appel a p.toString()
        return toSend;
    }
    /* FIN FONCTIONS D'INFORMATION */

    static void sendWelcome(){
        //TODO: quand tous les joueurs ont envoye START
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