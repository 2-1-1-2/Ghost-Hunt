import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;

public class Game{
    private int numGame;
    private boolean onGoing=false;
    private View view=null;
    //TODO: mettre tous les messages envoyes/recus dans chatbox
        //black pour player, blue pour ghost, red pour item
    
    private Case[][] maze;
    private LinkedList<Ghost> ghosts=new LinkedList<Ghost>();
    private LinkedList<Player> players=new LinkedList<Player>();
    
    private InetSocketAddress addressMultiD;
    private int portMultiD;

    Game(Player creator){
        this.numGame=Server.getNbGames();
        
        //creation de l'adresse de multidiffusion en fonction du nb de parties créées
        this.portMultiD=6000+numGame;
        this.addressMultiD=new InetSocketAddress("225.066.066.066", portMultiD);
        
        generateMaze();
        this.addPlayerInGame(creator);
    }
    
    int getNum(){
        return numGame;
    }
    
    boolean isOnGoing(){
        return this.onGoing;
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
        return this.ghosts.size();
    }

    int getNbPlayers(){
        return this.players.size();
    }

    String getListPlayers(){
        String res="";
        for(Player p: this.players) res+=p; //appel a p.toString()
        return res;
    }
    
    String listPlayersCurrent(){
        String res="GLIS! "+(byte)this.getNbPlayers();
        for(Player p: players) res+="GPLYR "+p.currentInfo(true);
        return res;
    }
    
    String getIP(){
        String res=this.addressMultiD.getAddress().toString();
        for(int i=res.length(); i<16; i++) res+="#";
        //System.out.println(res);
        return res;
    }
    
    int getPort(){
        return this.portMultiD;
    }

    /* GENERATION DU LABYRINTHE ET DE SES COMPOSANTES */
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
    
    void generateGhosts(int nbGhosts){
        int row, col;
        Ghost ghost;
        for(int i=0; i<nbGhosts; i++){
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
    
    void generateBombe(){
        int row, col;
        do{
            row=(int)(Math.random()*this.getHeight());
            col=(int)(Math.random()*this.getWidth());
        }
        while(maze[row][col].isWall() || maze[row][col].hasPlayer());
        maze[row][col].dropItem(new Item(0));
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
    }

    synchronized void removePlayerFromGame(Player p){
        this.maze[p.getRow()][p.getCol()].removePlayer(p);
        this.players.remove(p);
        this.onGoing=!this.players.isEmpty();
    }
    
    boolean canStart(){
        for(Player p: this.players)
            if(!p.sentStart()) return false;
        return true;
    }
    
    
    /* LA PARTIE COMMENCE */
    Game gameStart(){
        onGoing=true;
        generateGhosts(((int)(Math.random()*getNbPlayers()))+getNbPlayers()+1);
        generateItems();
        this.view=new View(this, this.getHeight(), this.getWidth());
        return this;
    }
    
    
    /* FONCTIONS DE DEPLACEMENT */
    //principe : si la prochaine case est un mur, player s'arrete a la case actuelle
    synchronized boolean[] moveUp(Player p, int nbStep){
        view.addText(p.getID()+" try to move up "+nbStep, "black");
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3], tmp; //0=getGhost ; 1=getItem ; 2=attack player
        while(nbStep-->0 && row>0 && !maze[row-1][col].isWall()){
            removePlayer(p, row, col);
            row=p.moveUp();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return get;
    }
    
    synchronized boolean[] moveRight(Player p, int nbStep){
        view.addText(p.getID()+" try to move right "+nbStep, "black");
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3], tmp; //0=getGhost ; 1=getItem ; 2=attack player
        while(nbStep-->0 && col<maze[0].length-1 && !maze[row][col+1].isWall()){
            removePlayer(p, row, col);
            col=p.moveRight();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return get;
    }
    
    synchronized boolean[] moveDown(Player p, int nbStep){
        view.addText(p.getID()+" try to move down "+nbStep, "black");
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3], tmp; //0=getGhost ; 1=getItem ; 2=attack player
        while(nbStep-->0 && row<maze.length-1 && !maze[row+1][col].isWall()){
            removePlayer(p, row, col);
            row=p.moveDown();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return get;
    }
    
    synchronized boolean[] moveLeft(Player p, int nbStep){
        //view.addText(p.getID()+" try to move left "+nbStep, "black");
        int row=p.getRow(), col=p.getCol();
        boolean[] get=new boolean[3], tmp; //0=getGhost ; 1=getItem ; 2=attack player
        while(nbStep-->0 && col>0 && !maze[row][col-1].isWall()){
            removePlayer(p, row, col);
            col=p.moveLeft();
            tmp=addPlayer(p, row, col);
            for(int i=0; i<tmp.length; i++) get[i]|=tmp[i];
        }
        return get;
    }
    
    //0=getGhost ; 1=getItem ; 2=attack player
    boolean[] addPlayer(Player p, int row, int col){
        Case c=maze[row][col];
        boolean[] res=new boolean[3];
        
        //attrape le ghost s'il y en a un
        res[0]=catchGhost(p, c);
        
        //prend l'item de la case, s'il existe
        res[1]=c.getItem(p);
        if(c.hasPlayer()){
            //attaque avec la bombe
            if((res[2]=!p.noItem() && p.hasBombe())) res[1]|=useBombe(p, c);
            //vole un item aux joueurs adversaires, si possible
            res[1]|=p.noItem() && p.stoleItem(c.stole());
        }
        if(res[1]) view.addText(p.getID()+" got an item", "red");
        
        c.addPlayer(p);
        view.refreshMaze(row, col, c.getColor());
        return res;
    }
    
    void removePlayer(Player p, int row, int col){
        maze[row][col].removePlayer(p);
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    
    
    synchronized int[] moveGhost(Ghost g, int row, int col, int points){
        int nRow, nCol, limit=0;
        //si aucune case libre autour en 100 essaies, alors teleportation
        do{
            do{
                if(limit>100) nRow=(int)(Math.random()*getWidth());
                else nRow=(int)(Math.random()*points*2+2)+row-points;
            }
            while(nRow<1 || nRow>getHeight()-2);
            do{
                if(limit>100) nCol=(int)(Math.random()*getHeight());
                else nCol=(int)(Math.random()*points*2+2)+col-points;
            }
            while(nCol<1 || nCol>getWidth()-2);
            limit++;
        }
        while(notValidForGhost(nRow, nCol));
        
        removeGhost(row, col);
        addGhost(g, nRow, nCol);
        view.addText("ghost move from ("+row+", "+col+") to ("+nRow+", "+nCol+")", "blue");
        diffuse("GHOST "+Server.intToNChar(nRow, 3)+" "+Server.intToNChar(nCol, 3)+"+++");
        return new int[]{nRow, nCol};
    }
    
    synchronized boolean notValidForGhost(int row, int col){
        return maze[row][col].isWall() || maze[row][col].hasPlayer() || maze[row][col].hasGhost();
    }
    
    void addGhost(Ghost g, int row, int col){
        maze[row][col].setGhost(g);
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    
    void removeGhost(int row, int col){
        maze[row][col].removeGhost();
        view.refreshMaze(row, col, maze[row][col].getColor());
    }
    /* FIN FONCTIONS DE DEPLACEMENT */

    
    /* FONCTIONS DE DEROULEMENT DE JEU */
    boolean catchGhost(Player p, Case c){
        Ghost ghost=c.catchGhost(p);
        if(ghost!=null){
            this.ghosts.remove(ghost);
            view.addText(p.getID()+" caught ghost(s)", "blue");
            diffuse(p.currentInfoCatch());
            this.onGoing=!this.ghosts.isEmpty();
            return true;
        }
        return false;
    }
    
    void dropItem(Item item, int row, int col){
        this.maze[row][col].dropItem(item);
    }
    
    String useItem(Player p){
        if(p.noItem() || p.hasBombe()) return "";
        if(p.hasLampe()) return useLampe(p.getRow(), p.getCol());
        return useRadar(p.getRow(), p.getCol());
    }
    
    boolean useBombe(Player p, Case c){
        LinkedList<Ghost> toRemove=c.attack();
        p.dropItem();
        view.addText(p.getID()+" attacked player(s)", "red");
        if(!toRemove.isEmpty())
            for(Ghost g: toRemove) ghosts.remove(g);
        generateBombe();
        return c.getItem(p); //renvoie l'item de la case s'il existe
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
        String res="";
        for(int x=row-2; x<row+3; x++)
            for(int y=col-2; y<col+3; y++)
                if(maze[x][y].hasGhost()) res+="FNDGH "+Server.intToNChar(x, 3)
                                              +" "+Server.intToNChar(y, 3)+"***";
        return res;
    }
    
    synchronized void diffuse(String msg){
        try{
            DatagramSocket dso=new DatagramSocket();
            byte[] data=msg.getBytes();
            DatagramPacket paquet=new DatagramPacket(data, data.length, addressMultiD);
            dso.send(paquet);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        view.addText(msg, "orange");
    }
    
    //il n'y a plus de ghosts a attraper, on envoie le message qui indique le gagnant
    void noMoreGhost(){
        if(!this.ghosts.isEmpty()) return;
        Player winner=players.get(0);
        for(int i=1; i<players.size(); i++)
            if(winner.getScore()<players.get(i).getScore()
                || (winner.getScore()==players.get(i).getScore()
                    && winner.getNbGhostsCaught()<players.get(i).getNbGhostsCaught()))
                winner=players.get(i);
        view.addText("winner is "+winner.getID()+" ("+winner.getScore()+" points and "+winner.getNbGhostsCaught()+" ghosts)", "orange");
        diffuse("ENDGA "+winner.getID()+" "+Server.intToNChar(winner.getScore(), 4)+"+++");
    }
    /* FIN FONCTIONS DE JEU */
}