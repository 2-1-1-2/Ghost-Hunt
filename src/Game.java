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
        while(maze[row][col].isWall() || maze[row][col].hasPlayer());
        this.maze[row][col].addPlayer(p);
        p.initialize(row, col);
        
        this.players.add(p);
        this.nbPlayers++;
    }

    synchronized void removePlayerFromGame(Player p){
        if(players.contains(p)){
            this.maze[p.getRow()][p.getCol()].removePlayer(p);
            p.initialize(-1, -1); //TODO: utile ? initialise deja dans addPlayerInGame
            
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
        generateItems();
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
            while(maze[row][col].isWall() || maze[row][col].hasPlayer());
            ghost=new Ghost(row, col, (int)(Math.random()*5)+1, this);
            maze[row][col].setGhost(ghost);
            ghosts.add(ghost);
            new Thread(ghost).start(); //pour automatiser le deplacement des ghosts
        }
    }
    
    void generateItems(){
        int row, col;
        Item item;
        for(int i=0; i<3; i++){ //un item de chaque par jeu
            do{
                row=(int)(Math.random()*this.getHeight());
                col=(int)(Math.random()*this.getWidth());
            }
            while(maze[row][col].isWall() || maze[row][col].hasPlayer());
            item=new Item(i);
            maze[row][col].dropItem(item);
        }
    }
    
    /* FONCTIONS DE DEPLACEMENT */
    //principe : si la prochaine case est un mur, player s'arrete a la case actuelle
    boolean[] moveUp(Player p, int nbStep){
        view.addText(p.getID()+" try to move up "+nbStep);
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3]; //0=getGhost ; 1=getItem
        boolean[] tmp;
        while(nbStep-->0 && row>0 && !maze[row-1][col].isWall()){
            removePlayer(p, row, col);
            row=p.moveUp();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return treateResMove(get, p);
    }
    
    boolean[] moveRight(Player p, int nbStep){
        view.addText(p.getID()+" try to move right "+nbStep);
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3]; //0=getGhost ; 1=getItem
        boolean[] tmp;
        while(nbStep-->0 && col<maze[0].length-1 && !maze[row][col+1].isWall()){
            removePlayer(p, row, col);
            col=p.moveRight();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return treateResMove(get, p);
    }
    
    boolean[] moveDown(Player p, int nbStep){
        view.addText(p.getID()+" try to move down "+nbStep);
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3]; //0=getGhost ; 1=getItem
        boolean[] tmp;
        while(nbStep-->0 && row<maze.length-1 && !maze[row+1][col].isWall()){
            removePlayer(p, row, col);
            row=p.moveDown();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return treateResMove(get, p);
    }
    
    boolean[] moveLeft(Player p, int nbStep){
        view.addText(p.getID()+" try to move left "+nbStep);
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3]; //0=getGhost ; 1=getItem
        boolean[] tmp;
        while(nbStep-->0 && col>0 && !maze[row][col-1].isWall()){
            removePlayer(p, row, col);
            col=p.moveLeft();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return treateResMove(get, p);
    }
    
    boolean[] treateResMove(boolean[] get, Player p){
        if(get[0]) view.addText(p.getID()+" caught ghost(s)");
        if(get[1]) view.addText(p.getID()+" got an item");
        if(get[2]){
            view.addText(p.getID()+" attacked player(s)");
            generateBombe();
        }
        return get;
    }
    
    boolean[] addPlayer(Player p, int row, int col){
        boolean[] res=maze[row][col].addPlayer(p);
        view.refreshMaze(row, col, maze[row][col].getColor());
        return res;
    }
    
    void removePlayer(Player p, int row, int col){
        maze[row][col].removePlayer(p);
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    /* FIN FONCTIONS DE DEPLACEMENT */

    
    //TODO:
    /* FONCTIONS DE DEROULEMENT DE JEU */
    void dropItem(Item item, int row, int col){
        this.maze[row][col].dropItem(item);
    }
    
    void generateBombe(){
        int row, col;
        do{
            row=(int)(Math.random()*this.getHeight());
            col=(int)(Math.random()*this.getWidth());
        }
        while(maze[row][col].isWall() || maze[row][col].hasPlayer());
        maze[row][col].dropItem(new Item(0));
    }
    
    String useItem(Player p){
        if(p.noItem() || p.hasBombe()) return "";
        if(p.hasLampe()) return useLampe(p.getRow(), p.getCol());
        return useRadar(p.getRow(), p.getCol());
    }
    
    String useLampe(int row, int col){
        String res="";
        if(maze[row-1][col].isWall()) res+="MURUP***";
        if(maze[row+1][col].isWall()) res+="MURDO***";
        if(maze[row][col-1].isWall()) res+="MURLE***";
        if(maze[row][col+1].isWall()) res+="MURRI***";
        return res;
    }
    
    String useRadar(int row, int col){
        //TODO: arranger octets
        String res="";
        for(int i=row-2; i<row+3; i++)
            for(int j=col-2; j<col+3; j++)
                if(maze[i][j].hasGhost()) res+="FNDGH "+i+" "+j+"***";
        return res;
    }
    
    synchronized boolean notValidForGhost(int row, int col){
        return maze[row][col].isWall() || maze[row][col].hasPlayer() || maze[row][col].hasGhost();
    }
    
    synchronized void moveGhost(Ghost ghost, int row, int col, int nRow, int nCol){
        maze[row][col].removeGhost();
        maze[nRow][nCol].setGhost(ghost);
    }
    
    synchronized void diffuse(String message){
        //TODO: multi-diffuse message
        
    }
    
    boolean gameIsEnd(){
        //TODO: tous les ghosts ont ete attrapes
        
        return false;
    }
    /* FIN FONCTIONS DE JEU */
}