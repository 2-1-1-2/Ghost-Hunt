import java.net.InetSocketAddress;
import java.util.LinkedList;

public class Game{
    private int numGame;
    private boolean onGoing=false;
    
    private Case[][] maze;
    
    private LinkedList<Ghost> ghosts=new LinkedList<Ghost>();
    private int nbGhostsRemain;
    
    private LinkedList<Player> players=new LinkedList<Player>();
    private int nbPlayers=0;
    
    private InetSocketAddress addressMultiD;
    private int portMultiD;
    
    private View view=null;

    Game(Player creator){
        Server.incGames();
        this.numGame=Server.getNbGames();
        
        //creation de l'adresse de multidiffusion en fonction du nb de parties créées
        //a voir si ok pcq d'apres le cours toutes les adresses ne sont pas dispo
        this.portMultiD=7777;
        this.addressMultiD=new InetSocketAddress("225.066.066."+numGame, portMultiD);
        
        generateMaze();
        this.addPlayerInGame(creator);
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
    
    int[][] getMazeColor(){
        int[][] res=new int[getHeight()][getWidth()];
        for(int i=0; i<getHeight(); i++)
            for(int j=0; j<getWidth(); j++)
                res[i][j]=maze[i][j].getColor();
        return res;
    }
    
    int getNbGhosts(){
        return this.nbGhostsRemain;
    }

    int getNbPlayers(){
        return nbPlayers;
    }

    String getListPlayers(){
        String res="";
        for(Player p: this.players) res+=p; //appel a p.toString()
        return res;
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

    /* GENERATION DU LABYRINTHE */
    void generateMaze(){
        int height=(int)(Math.random()*20)+10;
        int width=(int)(Math.random()*20)+10;
        MazeGenerator generator=new MazeGenerator(height, width);
        
        maze=new Case[2*height+1][2*width+1];
        
        //mur de droite
        for(int i=0; i<maze.length; i++) maze[i][maze[0].length-1]=new Case(true);
        
        //mur du bas
        for(int i=0; i<maze[0].length; i++) maze[maze.length-1][i]=new Case(true);
        
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

    /* AVANT QU'UNE PARTIE COMMENCE */
    synchronized void addPlayerInGame(Player p){
        int row, col;
        do{
            row=(int)(Math.random()*getHeight());
            col=(int)(Math.random()*getWidth());
        }
        while(maze[row][col].isWall() || maze[row][col].havePlayer());
        this.maze[row][col].addPlayer();
        p.initializePosition(row, col);
        
        this.players.add(p);
        this.nbPlayers++;
    }

    synchronized void removePlayerFromGame(Player p){
        if(players.contains(p)){
            this.maze[p.getRow()][p.getCol()].removePlayer();
            p.initializePosition(-1, -1);
            
            this.players.remove(p);
            this.nbPlayers--;
        }
    }
    
    boolean canStart(){
        for(Player p: this.players)
            if(!p.sentStart()) return false;
        return true;
    }
    
    /* LA PARTIE COMMENCE */
    void gameStart(){
        onGoing=true;
        nbGhostsRemain=((int)(Math.random()*nbPlayers))+nbPlayers+1;
        generateGhosts();
        this.view=new View(this, this.getHeight(), this.getWidth());
    }
    
    void generateGhosts(){
        int row, col;
        Ghost ghost;
        for(int i=0; i<this.nbGhostsRemain; i++){
            do{
                row=(int)(Math.random()*this.getHeight());
                col=(int)(Math.random()*this.getWidth());
            }
            while(maze[row][col].isWall() || maze[row][col].havePlayer());
            ghost=new Ghost(row, col, (int)(Math.random()*5)+1);
            maze[row][col].addGhost(ghost);
            ghosts.add(ghost);
        }
    }
    
    /* FONCTIONS DE DEPLACEMENT */
    //principe : si la prochaine case est un mur, player s'arrete a la case actuelle
    void moveUp(Player p, int nbStep){
        //view.addText(p.getID()+" try to move up "+nbStep);
        view.addText(p.getID()+" try to move up "+nbStep);
        int row=p.getRow(), col=p.getCol();
        while(nbStep-->0 && row>0 && !maze[row-1][col].isWall()){
            removePlayer(row, col);
            row=p.moveUp();
            addPlayer(row, col);
        }
    }
    
    void moveRight(Player p, int nbStep){
        view.addText(p.getID()+" try to move right "+nbStep);
        int row=p.getRow(), col=p.getCol();
        while(nbStep-->0 && col<maze[0].length-1 && !maze[row][col+1].isWall()){
            removePlayer(row, col);
            col=p.moveRight();
            addPlayer(row, col);
        }
    }
    
    void moveDown(Player p, int nbStep){
        view.addText(p.getID()+" try to move down "+nbStep);
        int row=p.getRow(), col=p.getCol();
        while(nbStep-->0 && row<maze.length-1 && !maze[row+1][col].isWall()){
            removePlayer(row, col);
            row=p.moveDown();
            addPlayer(row, col);
        }
    }
    
    void moveLeft(Player p, int nbStep){
        view.addText(p.getID()+" try to move left "+nbStep);
        int row=p.getRow(), col=p.getCol();
        while(nbStep-->0 && col>0 && !maze[row][col-1].isWall()){
            removePlayer(row, col);
            col=p.moveLeft();
            addPlayer(row, col);
        }
    }
    
    //TODO: eventuellement rajouter player en argument pour la vue des items ?
    void removePlayer(int row, int col){
        maze[row][col].removePlayer();
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    
    //TODO: idem que pour removePlayerFromGame ?
    void addPlayer(int row, int col){
        maze[row][col].addPlayer();
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    /* FIN FONCTIONS DE DEPLACEMENT */

    
    //TODO:
    /* FONCTIONS DE DEROULEMENT DE JEU */

    /* FIN FONCTIONS DE JEU */
}