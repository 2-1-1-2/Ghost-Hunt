import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
TODO:
OK NEWPL id port***
OK REGIS id port game.numGame***

X START***
X UNREG***
OK SIZE? game.numGame***
OK LIST? game.numGame***
X GAME?***

X UPMOV nbPas***
X RIMOV nbPas***
X DOMOV nbPas***
X LEMOV nbPas***

X IQUIT***

X GLIS?***

X MALL? message***

X SEND? player.username message***
*/

public class ServiceClient implements Runnable{//en fait, c'est une extension du Server
    private Socket sock;
    private String id=null;
    private BufferedReader reader;
    private PrintWriter writer;
    private Player player=null;//a instancier seulement si le client s'inscrit ou cree une partie
    private int port;
    private Game game=null;
    
    public ServiceClient(Socket socket) throws IOException{
        this.sock=socket;
        this.reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        Server.incClients();
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
    
    String getID(){
        return this.id;
    }
    
    int getPort(){
        return this.port;
    }

    /* FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */
    void parseReplyBeforeStart() throws IOException{
        //TODO: lire sur reader et appeler l'une des 
        //methodes traitement des reponses
        char[] reading=new char[1];
        int nbStars=0;
        String msg="";
        while(reader.read(reading, 0, 1)!=-1 && nbStars!=3){//while !vide
            if(reading[0]=='*') nbStars++;
            else nbStars=0;
            msg+=reading[0];
            //TODO: gerer quand on pourrait avoir *** puis une suite de message encore (MALL)
            //if(nbStars==3) //enlever dans la cdt du while
        }
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        //TODO:
        if(type.equals("NEWPL")){
            this.id=sc.next();
            System.out.println(id);
            if(!Server.idOk(this.id)) dunno();
            else{
                this.player=new Player(id);
                this.port=Integer.valueOf(sc.next().substring(0, 4));
                createGame(this.port);
            }
            
        }
        else if(type.equals("REGIS")){
            //TODO: sortir la condition du joueur existant et le mettre juste apres avoir obtenu String type ?
            if(player==null){
                this.id=sc.next();
                if(!Server.idOk(this.id)) dunno();
                else{
                    this.player=new Player(id);
                    this.port=Integer.valueOf(sc.next().substring(0, 4));
                }
            }
            else 
                for(int i=0; i<2; i++) sc.next();//e.g si apres un UNREG et le joueur existe deja
            register(this.port, sc.next().charAt(0));
        }
        else if(type.equals("START"))
            start();
        else if(type.equals("SIZE?")) 
            size(sc.next().charAt(0));
        else if(type.equals("LIST?"))
            listPlayers(sc.next().charAt(0));
        else if(type.equals("GAME?"))
            listGames();
        else dunno();
    }

    void parseGameCommand(){

    }
    /* FIN FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */

    
    /* TRAITEMENT DES REPONSES AVANT LA PARTIE */
    //creer une nouvelle partie
    void createGame(int port){
        this.game=Server.addGame(this.player);
    }

    //s'inscrire a la partie no.numGame
    void register(int port, int numGame){
        if(numGame>=0 && numGame<Server.getNbGames()){
            this.game=Server.addInGame(this.player, numGame);
        }
        else dunno();
    }
        
    //TODO: quand un joueur envoie START
    //regarder la partie dans laquelle il est inscrit (=game)
    //pour voir si on peut faire appel a Server.sendWelcome()
    //si tous les joueurs sont en train de waiting, alors lancer la partie
    //sinon, juste changer le boolean waiting
    void start(){
        if(this.game!=null && !this.player.sentStart()){
            this.player.setStartStatus(true);
            if(Server.canStart(this.game.getNum()))
                Server.sendWelcome(this.game.getNum());
        }
        else if(this.game==null) dunno();
        //si le joueur a deja start, on ne fait rien du tout
        //comme un "bloquage"
    }

    void unregister(){
        if(this.game!=null){
            this.game.removePlayer(player);
            this.game=null;
        }
        else dunno();
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

    void listGames(){
        String games=Server.listGames();
        writer.print(games);
        writer.flush();
    }

    void dunno(){
        writer.print("DUNNO***");
        writer.flush();
    }
    /* FIN TRAITEMENT DES REPONSES */
    
    
    /* TRAITEMENT DES COMMANDES LORS D'UNE PARTIE */
    void quit(){
        //TODO: envoyer gobye et supprimer le client de la partie
    }
    /* FIN TRAITEMENT DES COMMANDES */

    
    public void run(){
        //envoyer la liste des parties
        listGames();

        //COMMUNICATION AVANT LA PARTIE
        while(player==null || !player.sentStart()){
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
