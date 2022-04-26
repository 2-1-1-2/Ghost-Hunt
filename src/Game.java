import java.net.InetSocketAddress;
import java.util.LinkedList;

public class Game{
    private int numGame;
    private LinkedList<Player> players=new LinkedList<Player>();
    private int nbPlayers=0;
    private InetSocketAddress addressMultiD;
    private int portMultiD;
    private boolean onGoing=false;
    private Case[][] maze;

    Game(Player creator, int nbGames){
        Server.incGames();
        this.numGame=nbGames+1;
        this.players.add(creator);
        this.nbPlayers++;

        //creation de l'adresse de multidiffusion en fonction du nb de parties créées
        //a voir si ok pcq d'apres le cours toutes les adresses ne sont pas dispo
        this.portMultiD=7777;
        this.addressMultiD=new InetSocketAddress("225.066.066."+nbGames, portMultiD);
    }

    int getNum(){
        return numGame;
    }

    int getWidth(){
        return (onGoing)?maze[0].length:-1;
    }

    int getHeight(){
        return (onGoing)?maze.length:-1;
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

    //TODO : generer maze, a appeler une fois la partie commencee (e.g. tous les joueurs ont envoye "START")
    void generateMaze(){
        int height=(int)(Math.random()*nbPlayers)+nbPlayers*2+1;
        int width=(int)(Math.random()*nbPlayers)+nbPlayers*2+1;
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
    
    //TODO : mettre onGoing à true en commencant la partie

    //TODO:
    /* FONCTIONS DE DEROULEMENT DE JEU */

    /* FIN FONCTIONS DE JEU */
}