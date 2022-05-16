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

OK UPMOV nbPas***
OK RIMOV nbPas***
OK DOMOV nbPas***
OK LEMOV nbPas***

X IQUIT***

OK DRPIT***
OK CHKIT***

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
    private boolean extensionActived; //TODO: "ACTEX***" et "CLOEX***"
    
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
        System.out.println("entre");
        //TODO: lire sur reader et appeler l'une des 
        //methodes traitement des reponses
        //char[] reading=new char[1];
        char reading;
        int nbStars=0;
        String msg="";
        while(nbStars!=3){//while !vide
            reading=(char)reader.read();
            msg+=reading;
            if(reading=='*') nbStars++;
            else nbStars=0;
            System.out.println(msg+" "+nbStars);
            //TODO: gerer quand on pourrait avoir *** puis une suite de message encore (MALL)
            //if(nbStars==3) //enlever dans la cdt du while
        }
        System.out.println("ok");
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        //TODO:
        if(type.contains("NEWPL")){
            this.id=sc.next();
            System.out.println(id);
            if(!Server.idOk(this.id)) dunno();
            else{
                this.player=new Player(id);
                this.port=Integer.valueOf(sc.next().substring(0, 4));
                createGame(this.port);
            }
            
        }
        else if(type.contains("REGIS")){
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
        else if(type.contains("START"))
            start();
        else if(type.contains("SIZE?")) 
            size(sc.next().charAt(0));
        else if(type.contains("LIST?"))
            listPlayers(sc.next().charAt(0));
        else if(type.contains("GAME?"))
            listGames();
        else dunno();
        System.out.println("fin");
    }

    void parseGameCommand(){
        //UPMOV nbPas***
        //RIMOV nbPas***
        //DOMOV nbPas***
        //LEMOV nbPas***
        //IQUIT***
        //DRPIT***
        //CHKIT***
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
            catch(Exception e){
                e.printStackTrace();
                System.err.println("while principal1");
            }
        }

        System.out.println("sorti du while main");

        //COMMUNICATION PENDANT LA PARTIE
        /*while(true){//?
            //TODO
            parseGameCommand();
        }*/
    }
}
