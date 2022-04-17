import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceClient implements Runnable{//en fait, c'est une extension du Server
    Socket sock;
    String id;
    BufferedReader reader;
    PrintWriter writer;
    Player player=null;//a instancier seulement si le client s'inscrit ou cree une partie
    Game game=null;
    
    public ServiceClient(Socket socket) throws IOException{
        this.sock=socket;
        String tmpId=createID();
        while(!Server.idOk(tmpId)) tmpId=createID();
        this.id=tmpId;
        this.reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * @return un id alphanum aleatoire
     */
    static String createID(){
        String id="user";
        for(int i=0; i<4; i++)
            id+="0123456789".charAt((int)(Math.random()*10));
        return id;
    }

    void parseClientReply(){
        //TODO : lire sur reader et appeler l'une des 
        //methodes traitement des reponses
    }

    /* TRAITEMENT DES REPONSES AVANT LA PARTIE */
    void register(int port, int numGame){//s'inscrire a la partie no.numGame
        //TODO
    }

    void createGame(int port){
        //TODO
    }

    void unregister(){
        //TODO
    }

    void size(int numGame){
        if(Server.gameExists(numGame)){
            String dimensions=Server.sizeMaze(numGame);
            writer.print(dimensions);
            writer.flush();
        }
        else dunno();
    }

    void listPlayers(int numGame){
        if(Server.gameExists(numGame)){
            String players=Server.listPlayers(numGame);
            writer.print(players);
            writer.flush();
        }
        else dunno();
    }

    void dunno(){
        writer.print("DUNNO***");
        writer.flush();
    }
    /* FIN TRAITEMENT DES REPONSES */
    
    /* TRAITEMENT DES COMMANDES LORS D'UNE PARTIE */
    void quit(){
        //TODO : envoyer gobye et supprimer le client de la partie
    }
    /* FIN TRAITEMENT DES COMMANDES */

    public void run(){
        //envoyer la liste des parties
        String games=Server.listGames();
        writer.print(games);
        writer.flush();

        //COMMUNICATION AVANT LA PARTIE
        while(player==null || player.isWaiting()) 
            parseClientReply();

        //COMMUNICATION PENDANT LA PARTIE
        while(true){//?
            //TODO
        }
    }
}
