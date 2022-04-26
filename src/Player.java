public class Player{
    private int score, nbGhostCaught;//le deuxieme a voir si c'est utile
    private int row, col;
    private String username;//"id" dans le sujet
    private boolean waiting=false;//avant que la partie commence?

    Player(String username){
        this.username=username;
    }

    public String toStrinG(){
        return "PLAYR "+username+"***";
    }

    boolean isWaiting(){
        return waiting;
    }

    void setWaitStatus(boolean isWaiting){
        this.waiting=isWaiting;
    }

    void catchGhost(Ghost ghost){
        this.score+=ghost.getPoints();
        nbGhostCaught++;
    }

    /* FONCTIONS DE DEPLACEMENT */
    //principe : si la prochaine case est un mur, on s'arrete a la case actuelle
    void moveUp(Case[][] maze, int rowDest){
        while(this.row>0 && this.row!=rowDest){
            if(maze[this.row-1][this.col].isWall()) break;
            maze[this.row][this.col].removePlayer();
            this.row--;
            maze[this.row][this.col].addPlayer();
        }
    }
    
    void moveRight(Case[][] maze, int colDest){
        while(this.col<maze[0].length-1 || this.col!=colDest){
            if(maze[this.row][this.col+1].isWall()) break;
            maze[this.row][this.col].removePlayer();
            this.col++;
            maze[this.row][this.col].addPlayer();
        }
    }
    
    void moveDown(Case[][] maze, int rowDest){
        while(this.row<maze.length-1 && this.row!=rowDest){
            if(maze[this.row+1][this.col].isWall()) break;
            maze[this.row][this.col].removePlayer();
            this.row++;
            maze[this.row][this.col].addPlayer();
        }
    }
    
    void moveLeft(Case[][] maze, int colDest){
        while(this.col>0 || this.col!=colDest){
            if(maze[this.row][this.col-1].isWall()) break;
            maze[this.row][this.col].removePlayer();
            this.col--;
            maze[this.row][this.col].addPlayer();
        }
    }
    /* FIN FONCTIONS DE DEPLACEMENT */
}