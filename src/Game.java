import java.net.InetSocketAddress;
import java.util.LinkedList;

public class Game{
    //MazeGenerator maze;
    private Case[][] maze;
    private int numGame;
    private LinkedList<Player> players=new LinkedList<Player>();
    private int nbPlayers=0;
    private InetSocketAddress addressMultiD;
    private int portMultiD;
    private boolean onGoing;

    Game(Player creator, int numGame, int nbGames){
        this.numGame=numGame;
        this.players.add(creator);
        this.nbPlayers++;

        //TODO : generer maze

        //creation de l'adresse de multidiffusion 
        //en fonction du nb de parties créées
        //a voir si ok pcq d'apres le cours toutes les 
        //adresses ne sont pas dispo
        this.portMultiD=7777;
        this.addressMultiD=new InetSocketAddress("225.066.066."+nbGames, portMultiD);
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

    //TODO:
    /* FONCTIONS DE DEROULEMENT DE JEU */

    /* FIN FONCTIONS DE JEU */
}