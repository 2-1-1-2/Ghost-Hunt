import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServiceClient implements Runnable{//en fait, c'est une extension du Server
    Socket sock;
    String id;
    BufferedReader reader;
    PrintWriter writer;
    Player player=null;//a instancier seulement si le client s'inscrit ou cree une partie
    int port;
    Game game=null;
    
    public ServiceClient(Socket socket) throws IOException{
        this.sock=socket;
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

    /* FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */
    void parseReplyBeforeStart() throws IOException{
        //TODO : lire sur reader et appeler l'une des 
        //methodes traitement des reponses
        char[] reading=new char[1];
        int nbStars=0;
        String msg="";
        while(reader.read(reading, 0, 1)!=-1 && nbStars!=3){//while !vide
            if(reading[0]=='*') nbStars++;
            else nbStars=0;
            msg+=reading[0];
            //TODO: gerer quand on pourrait avoir *** puis une suite de message encore (MALL)
            //if(nbStars==3) //enleverda ns la cdt du while
        }
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        if(type.contains("NEWPL")){
            this.id=sc.next();
            if(!Server.idOk()) dunno();
            else{
                this.player=new Player(id);
                this.port=sc.nextInt();
                createGame(this.port);
            }
            
        }
        else if(type.contains("REGIS")){
            if(player==null){
                this.id=sc.next();
                if(!Server.idOk()) dunno();
                else{
                    this.player=new Player(id);
                    this.port=sc.nextInt();
                }
            }
            else 
                for(int i=0; i<2; i++) sc.next();//e.g si apres un UNREG et le joueur existe deja
            register(this.port, sc.nextInt());
        }
        else if(type.contains("START")){}
        else if(type.contains("SIZE?")){}
        else if(type.contains("LIST?")){}
        else if(type.contains("GAME?")){}
        else dunno();
    }

    void parseGameCommand(){

    }
    /* FIN FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */

    /* TRAITEMENT DES REPONSES AVANT LA PARTIE */
    void register(int port, int numGame){//s'inscrire a la partie no.numGame
        //TODO
        if(numGame>=0 && numGame<Server.getNbGames()){
            Server.addInGame(this.player, numGame);
        }
        else dunno();
    }

    void createGame(int port){
        this.game=new Game(player, Server.getNbGames());
        Server.addGame(this.game);
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
        while(player==null || player.isWaiting()){
            try{
                parseReplyBeforeStart();
            }
            catch(IOException e){}
        }

        //COMMUNICATION PENDANT LA PARTIE
        while(true){//?
            //TODO
            parseGameCommand();
        }
    }
}
