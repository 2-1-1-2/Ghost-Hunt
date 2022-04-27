import java.net.InetSocketAddress;
import java.util.LinkedList;

public class Game{
    private int numGame;
    private boolean onGoing=false;
    
    private Case[][] maze;
    private int nbGhostsRemain;
    
    private LinkedList<Player> players=new LinkedList<Player>();
    private int nbPlayers=0;
    
    private InetSocketAddress addressMultiD;
    private int portMultiD;

    Game(Player creator, int nbGames){
        Server.incGames();
        this.numGame=nbGames+1;
        this.players.add(creator);
        this.nbPlayers++;
        
        //creation de l'adresse de multidiffusion en fonction du nb de parties créées
        //a voir si ok pcq d'apres le cours toutes les adresses ne sont pas dispo
        this.portMultiD=7777;
        this.addressMultiD=new InetSocketAddress("225.066.066."+numGame, portMultiD);
        
        generateMaze();
    }
    
    int getNum(){
        return numGame;
    }

    int getWidth(){
        return maze[0].length;
    }

    int getHeight(){
        return maze.length;
    }
    
    int getNbGhosts(){
        return this.nbGhostsRemain;
    }

    int getNbPlayers(){
        return nbPlayers;
    }

    LinkedList<Player> getListPlayers(){
        return players;
    }

    synchronized void addPlayer(Player p){
        this.players.add(p);
        this.nbPlayers++;
    }
    
    //TODO: verifier si c'est bien ca qu'il faut retourner (ip=225.10.12.4#### par exemple)
    String getIP(){
        String res=this.addressMultiD.getAddress().toString();
        for(int i=res.length(); i<16; i++) res+="#";
        return res;
    }
    
    int getPort(){
        return this.portMultiD;
    }

    void generateMaze(){
        int height=(int)(Math.random()*20)+10;
        int width=(int)(Math.random()*20)+10;
        MazeGenerator generator=new MazeGenerator(height, width);
        
        maze=new Case[2*height+1][2*width+1];
        
        //mur de droite
        for(int i=0; i<maze.length; i++) maze[maze[0].length-1][i]=new Case(true);
        
        //mur du bas
        for(int i=0; i<maze[0].length; i++) maze[i][maze.length-1]=new Case(true);
        
        for(int i=0; i<width; i++){
            for(int j=0; j<height; j++){
                if((generator.maze[j][i] & 1)==0){
                    maze[2*j][2*i]=new Case(true);
                    maze[2*j+1][2*i]=new Case(true);
                }
                else{
                    maze[2*j][2*i]=new Case(true);
                    maze[2*j+1][2*i]=new Case(false);
                }
            }
            
            for(int j=0; j<height; j++){
                if((generator.maze[j][i] & 8)==0){
                    maze[2*j][2*i+1]=new Case(true);
                    maze[2*j+1][2*i+1]=new Case(false);
                }
                else{
                    maze[2*j][2*i+1]=new Case(false);
                    maze[2*j+1][2*i+1]=new Case(false);
                }
            }
        }
    }
    
    //la partie commence
    void gameStart(){
        onGoing=true;
        nbGhostsRemain=(int)(Math.random()*nbPlayers)+nbPlayers+1;
    }

    //TODO:
    /* FONCTIONS DE DEROULEMENT DE JEU */

    /* FIN FONCTIONS DE JEU */
}