import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

/*
TODO: recevoir
OK NEWPL id portUDP*** -> createGame()
OK REGIS id portUDP game.numGame*** -> register(numGame)

OK START*** -> start()
OK UNREG*** -> unregister()
OK SIZE? game.numGame*** -> sizeMaze(numGame)
OK LIST? game.numGame*** -> listPlayers(numGame)
OK GAME?*** -> listGames()


OK UPMOV nbPas*** -> moveUp(nbPas)
OK RIMOV nbPas*** -> moveRight(nbPas)
OK DOMOV nbPas*** -> moveDown(nbPas)
OK LEMOV nbPas*** -> moveLeft(nbPas)

OK ACTEX*** -> activeExtension()
OK CLOEX*** -> closeExtension()
OK DRPIT*** -> dropItem()
OK CHKIT*** -> checkItem()

OK GLIS?*** -> listPlayersCurrent()
X MALL? message*** -> messageToAll(message)
X SEND? player.username message*** -> sendToPlayer(username, message)

OK IQUIT*** -> quit()
*/

public class ServiceClient implements Runnable{//en fait, c'est une extension du Server
    private Socket sock;
    private BufferedReader reader;
    private PrintWriter writer;
    
    private Player player=null;//a instancier seulement si le client s'inscrit ou cree une partie
    private String id=null;
    private int portUDP;
    
    private Game game=null;
    private boolean extensionActived=false;
    
    public ServiceClient(Socket socket) throws IOException{
        this.sock=socket;
        this.reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        Server.addClient(this);
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
    
    boolean sameGame(Game game){
        return this.game==game;
    }

    /* FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */
    String parsing() throws IOException{
        char reading;
        int nbStars=0;
        String msg="";
        while(nbStars!=3){
            reading=(char)reader.read();
            msg+=reading;
            if(reading=='*') nbStars++;
            else nbStars=0;
        }
        System.out.println("player sent : "+msg);
        return msg;
    }
    
    void parseReplyBeforeStart(String msg){
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        if(type.equals("NEWPL") && game==null){
            this.id=sc.next();
            if(!Server.idOk(this.id, this)) dunno();
            else{
                this.player=new Player(id);
                this.portUDP=Integer.parseInt(sc.next().substring(0, 4));
                createGame();
            }
        }
        else if(type.equals("REGIS") && game==null){
            this.id=sc.next();
            if(!Server.idOk(id, this)) dunno();
            else{
                this.portUDP=Integer.parseInt(sc.next().substring(0, 4));
                register(Integer.parseInt(sc.next().replace("***", "")));
            }
        }
        else if(type.equals("START***")) start();
        else if(type.equals("UNREG***")) unregister();
        else if(type.equals("SIZE?")) sizeMaze(sc.next().charAt(0));
        else if(type.equals("LIST?")) listPlayers(sc.next().charAt(0));
        else if(type.equals("GAME?***")) listGames();
        else if(type.equals("IQUIT***")) quit(false);
        else dunno();
    }

    void parseGameCommand(String msg) throws IOException{
        Scanner sc=new Scanner(msg);
        String type=sc.next();
        if(!game.isOnGoing()) quit(true);
        else if(type.equals("UPMOV")) moveUp(sc.next());
        else if(type.equals("RIMOV")) moveRight(sc.next());
        else if(type.equals("DOMOV")) moveDown(sc.next());
        else if(type.equals("LEMOV")) moveLeft(sc.next());
        
        else if(type.equals("ACTEX***")) activeExtension();
        else if(type.equals("CLOEX***")) closeExtension();
        else if(type.equals("DRPIT***")) dropItem();
        else if(type.equals("CHKIT***")) checkItem();
        
        else if(type.equals("GLIS?***")) listPlayersCurrent();
        else if(type.equals("MALL?")) messageToAll(sc.next());
        else if(type.equals("SEND?")) sendToPlayer(sc.next(), sc.next());
        else if(type.equals("IQUIT***")) quit(true);
        else dunno();
    }
    
    void send(String toSend){
        writer.print(toSend);
        writer.flush();
    }
    /* FIN FONCTIONS PRINCIPALES DE TRAITEMENT DES REQUETES */

    
    /* TRAITEMENT DES REPONSES AVANT LA PARTIE */
    //creer une nouvelle partie
    void createGame(){
        this.game=Server.createGame(this.player);
        send("REGOK "+(byte)game.getNum()+"***");
    }

    //s'inscrire a la partie no.numGame
    void register(int numGame){
        System.out.println(numGame);
        if(numGame>=0 && numGame<Server.getNbGames()){
            if(player==null) this.player=new Player(id);
            else this.player.changeID(this.id);
            this.game=Server.addInGame(this.player, numGame);
            send("REGOK "+(byte)numGame+"***");
        }
        else send("REGNO***");
    }
    
    //regarder la partie dans laquelle il est inscrit (=game)
    //pour voir si on peut faire appel a Server.sendWelcome()
    //si tous les joueurs sont en train de waiting, alors lancer la partie
    //sinon, juste changer le boolean waiting
    void start(){
        if(this.game!=null && !this.player.sentStart()){
            this.player.setStartStatus(true);
            if(Server.canStart(game.getNum()))
                send(Server.sendWelcome(game.getNum())+"POSIT "+player.currentInfo(false));
        }
        else if(this.game==null) dunno();
        //si le joueur a deja start, on ne fait rien du tout
        //comme un "bloquage"
    }

    void unregister(){
        if(this.game!=null){
            game.removePlayerFromGame(player);
            send("UNROK "+(byte)game.getNum()+"***");
            this.game=null;
        }
        else dunno();
    }

    void sizeMaze(int numGame){
        if(Server.gameExists(numGame)) send(Server.sizeMaze(numGame));
        else dunno();
    }

    void listPlayers(int numGame){
        if(Server.gameExists(numGame)) send(Server.listPlayers(numGame));
        else dunno();
    }

    void listGames(){
        send(Server.listGames());
    }

    void dunno(){
        send("DUNNO***");
    }
    /* FIN TRAITEMENT DES REPONSES */
    
    
    /* TRAITEMENT DES COMMANDES LORS D'UNE PARTIE */


    void moveUp(String nbStep){
        if(nbStep.length()==3){
            send(moveRes(game.moveUp(player, Integer.parseInt(nbStep))));
            game.noMoreGhost();
        }
        else dunno();
    }
    
    void moveRight(String nbStep){
        if(nbStep.length()==3){
            send(moveRes(game.moveRight(player, Integer.parseInt(nbStep))));
            game.noMoreGhost();
        }
        else dunno();
    }
    
    void moveDown(String nbStep){
        if(nbStep.length()==3){
            send(moveRes(game.moveDown(player, Integer.parseInt(nbStep))));
            game.noMoreGhost();
        }
        else dunno();
    }
    
    void moveLeft(String nbStep){
        if(nbStep.length()==3){
            send(moveRes(game.moveLeft(player, Integer.parseInt(nbStep))));
            game.noMoreGhost();
        }
        else dunno();
    }
    
    String moveRes(boolean[] get){
        String itemS=(extensionActived && get[1])?"GETIT***":"";
        if(extensionActived){
            if(get[2]) itemS+="ATKPL***";
            itemS+=game.useItem(player);
        }
        String pos=Server.intToNChar(player.getRow(), 3)+" "+Server.intToNChar(player.getCol(), 3);
        return (get[0]?"MOVEF "+pos+" "+Server.intToNChar(player.getScore(), 4):"MOVE! "+pos)+"***"+itemS;
    }
    
    void activeExtension(){
        this.extensionActived=true;
        send("EXTON***");
    }
    
    void closeExtension(){
        this.extensionActived=false;
        send("EXTOF***");
    }
    
    void dropItem(){
        Item item=player.dropItem();
        String toSend="";
        if(item!=null){
            this.game.dropItem(item, player.getRow(), player.getCol());
            if(extensionActived) toSend="DRPOK***";
        }
        else if(extensionActived) toSend="NOITM***";
        if(extensionActived) send(toSend);
    }
    
    void checkItem(){
        if(extensionActived) send(player.checkItem());
    }
    
    void listPlayersCurrent(){
        send(game.listPlayersCurrent());
    }
    
    void messageToAll(String msg){
        msg="MESSA "+this.id+" "+msg+"+++";
        LinkedList<ServiceClient> clients=Server.getPlayers(this.game);
        for(ServiceClient servC : clients){
            try{
                DatagramSocket dso=new DatagramSocket();
                byte[] data=msg.getBytes();
                InetSocketAddress isa=new InetSocketAddress(servC.sock.getInetAddress().getHostAddress(), servC.portUDP);
                DatagramPacket paquet=new DatagramPacket(data, data.length, isa);
                dso.send(paquet);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        send("MALL!***");
    }
    
    ServiceClient getServiceClient(String id){
        return Server.getServiceClient(id, this.game);
    }
    
    void sendToPlayer(String dest, String msg){
        ServiceClient servC=getServiceClient(dest);
        if(servC==null){
            send("NSEND***");
            return;
        }
        msg="MESSP "+this.id+" "+msg+"+++";
        try{
            DatagramSocket dso=new DatagramSocket();
            byte[] data=msg.getBytes();
            InetSocketAddress isa=new InetSocketAddress(servC.sock.getInetAddress().getHostAddress(), servC.portUDP);
            DatagramPacket paquet=new DatagramPacket(data, data.length, isa);
            dso.send(paquet);
            send("SEND!***");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    void quit(boolean inGame){
        if(inGame){
            game.removePlayerFromGame(player);
            this.game=null;
        }
        send("GOBYE***");
        try{
            this.reader.close();
            this.writer.close();
            this.sock.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Server.removeClient(this);
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
