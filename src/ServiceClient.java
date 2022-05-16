import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
TODO:
OK NEWPL id portUDP***
OK REGIS id portUDP game.numGame***

OK START***
OK UNREG***
OK SIZE? game.numGame***
OK LIST? game.numGame***
OK GAME?***


OK UPMOV nbPas***
OK RIMOV nbPas***
OK DOMOV nbPas***
OK LEMOV nbPas***

OK ACTEX***
OK CLOEX***
OK DRPIT***
OK CHKIT***

X GLIS?***
X MALL? message***
X SEND? player.username message***

X IQUIT***
*/

public class ServiceClient implements Runnable{//en fait, c'est une extension du Server
    private Socket sock;
    private String id=null;
    private BufferedReader reader;
    private PrintWriter writer;
    private Player player=null;//a instancier seulement si le client s'inscrit ou cree une partie
    private int portUDP;
    private Game game=null;
    private boolean extensionActived=false; //TODO: "ACTEX***" et "CLOEX***"
    
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
        for(int i=0; i<4; i++) id+="0123456789".charAt((int)(Math.random()*10));
        return id;
    }
    
    String getID(){
        return this.id;
    }
    
    int getPort(){
        return this.portUDP;
    }
    
    boolean isPlayer(String id, Game game){
        return this.id.equals(id) && this.game==game;
    }

    /* FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */
    String parsing() throws IOException{
        //TODO: lire sur reader et appeler l'une des 
        //methodes traitement des reponses
        //char[] reading=new char[1];
        //TODO: changer le while et le read()
        char reading;
        int nbStars=0;
        String msg="";
        while(nbStars!=3){
            reading=(char)reader.read();
            msg+=reading;
            if(reading=='*') nbStars++;
            else nbStars=0;
            //TODO: gerer quand on pourrait avoir *** puis une suite de message encore (MALL)
            //if(nbStars==3) //enlever dans la cdt du while
        }
        System.out.println("player sent : "+msg);
        return msg;
    }
    
    void parseReplyBeforeStart(String msg){
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        if(type.contains("NEWPL")){
            this.id=sc.next();
            if(!Server.idOk(this.id)) dunno();
            else{
                this.player=new Player(id);
                this.portUDP=Integer.valueOf(sc.next().substring(0, 4));
                createGame();
            }
        }
        else if(type.contains("REGIS")){
            //TODO: sortir la condition du joueur existant et le mettre juste apres avoir obtenu String type ?
            if(player==null){
                this.id=sc.next();
                if(!Server.idOk(this.id)) dunno();
                else{
                    this.player=new Player(id);
                    this.portUDP=Integer.valueOf(sc.next().substring(0, 4));
                }
            }
            else 
                for(int i=0; i<2; i++) sc.next();//e.g si apres un UNREG et le joueur existe deja
            register(sc.next().charAt(0));
        }
        else if(type.contains("START")) start();
        else if(type.contains("UNREG")) unregister();
        else if(type.contains("SIZE?")) size(sc.next().charAt(0));
        else if(type.contains("LIST?")) listPlayers(sc.next().charAt(0));
        else if(type.contains("GAME?")) listGames();
        else dunno();
    }

    void parseGameCommand(String msg) throws IOException{
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        if(game.gameIsEnd()) quit();
        else if(type.contains("UPMOV")) moveUp(sc.nextInt());
        else if(type.contains("RIMOV")) moveRight(sc.nextInt());
        else if(type.contains("DOMOV")) moveDown(sc.nextInt());
        else if(type.contains("LEMOV")) moveLeft(sc.nextInt());
        else if(type.contains("ACTEX")) activeExtension();
        else if(type.contains("CLOEX")) closeExtension();
        else if(type.contains("DRPIT")) dropItem();
        else if(type.contains("CHKIT")) checkItem();
        else if(type.contains("GLIS?")) playersList();
        else if(type.contains("MALL?")) messageToAll(sc.next());
        else if(type.contains("SEND?")) sendToPlayer(sc.next(), sc.next());
        else if(type.contains("IQUIT")) quit();
        else dunno();
    }
    /* FIN FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */

    
    /* TRAITEMENT DES REPONSES AVANT LA PARTIE */
    //creer une nouvelle partie
    void createGame(){
        this.game=Server.addGame(this.player);
    }

    //s'inscrire a la partie no.numGame
    void register(int numGame){
        if(numGame>=0 && numGame<Server.getNbGames())
            this.game=Server.addInGame(this.player, numGame);
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
            if(Server.canStart(game.getNum())) Server.sendWelcome(game.getNum());
            //TODO: Server.sendWelcome renvoie un String
        }
        else if(this.game==null) dunno();
        //si le joueur a deja start, on ne fait rien du tout
        //comme un "bloquage"
    }

    //TODO: a appeler avec UNREG***
    void unregister(){
        if(this.game!=null){
            this.game.removePlayerFromGame(player);
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
    void moveUp(int nbStep){
        writer.print(moveRes(game.moveUp(player, nbStep)));
        writer.flush();
    }
    
    void moveRight(int nbStep){
        writer.print(moveRes(game.moveRight(player, nbStep)));
        writer.flush();
    }
    
    void moveDown(int nbStep){
        writer.print(moveRes(game.moveDown(player, nbStep)));
        writer.flush();
    }
    
    void moveLeft(int nbStep){
        writer.print(moveRes(game.moveLeft(player, nbStep)));
        writer.flush();
    }
    
    //TODO: arranger la reponse en bytes
    String moveRes(boolean[] get){
        String res=(extensionActived && get[1])?"GETIT***":"";
        if(extensionActived){
            if(get[2]) res+="ATKPL***";
            res+=game.useItem(player);
        }
        if(get[0]) return "MOVEF "+player.getRow()+" "+player.getCol()+" "+player.getScore()+"***"+res;
        else return "MOVE! "+player.getRow()+" "+player.getCol()+"***"+res;
    }
    
    void activeExtension(){
        this.extensionActived=true;
        writer.print("EXTON***");
        writer.flush();
    }
    
    void closeExtension(){
        this.extensionActived=false;
        writer.print("EXTOF***");
        writer.flush();
    }
    
    void dropItem(){
        Item item=player.dropItem();
        if(item!=null){
            this.game.dropItem(item, player.getRow(), player.getCol());
            if(extensionActived) writer.print("DRPOK***");
        }
        else if(extensionActived) writer.print("NOITM***");
        if(extensionActived) writer.flush();
    }
    
    void checkItem(){
        if(extensionActived){
            writer.print(player.checkItem());
            writer.flush();
        }
    }
    
    void playersList(){
        //TODO: afficher la liste des joueurs courants dans la partie
            //GLIS! s*** avec s=nombre de joueurs presents
            //GPLYR id x y p***
        
    }
    
    void messageToAll(String msg){
        //TODO: envoyer un message a tous les joueurs, sur le port multi-diffuse
        
        writer.print("MALL!");
        writer.flush();
    }
    
    void sendToPlayer(String id, String msg){
        if(getPlayerUDP(id)==-1){
            writer.print("NSEND***");
            writer.flush();
            return;
        }
        //TODO: envoyer un message au portUDP du Player(id) 
        
        writer.print("SEND!***");
        writer.flush();
    }
    
    int getPlayerUDP(String id){
        return Server.getPlayerUDP(id, this.game);
    }
    
    void quit(){
        //TODO: supprimer le client de la partie et envoyer gobye
        
        writer.print("GOBYE");
        writer.flush();
    }
    /* FIN TRAITEMENT DES COMMANDES */

    
    public void run(){
        //envoyer la liste des parties
        listGames();

        //COMMUNICATION AVANT LA PARTIE
        while(player==null || !player.sentStart()){
            try{
                parseReplyBeforeStart(parsing());
            }
            catch(Exception e){
                e.printStackTrace();
                System.err.println("parsing before start");
            }
        }


        //COMMUNICATION PENDANT LA PARTIE
        while(true){
            //TODO
            try{
                parseGameCommand(parsing());
            }
            catch(Exception e){
                e.printStackTrace();
                System.err.println("parsing during game");
            }
        }
    }
}
