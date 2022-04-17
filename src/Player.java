public class Player{
    int score, nbGhostCaught;//le deuxieme a voir si c'est utile
    int row, col;
    String username;//"id" dans le sujet
    boolean waiting;//avant que la partie commence?

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
        this.score+=ghost.points;
        nbGhostCaught++;
    }

    /* FONCTIONS DE DEPLACEMENT */
    //principe : si la prochaine case est un mur, on s'arrete a la case actuelle
    void moveUp(Case[][] maze, int rowDest){
        int currentRow=this.row;
        while(currentRow>0 && currentRow!=rowDest){
            if(maze[currentRow-1][this.col].isWall()) break;
            currentRow--;
        }
        this.row=currentRow;
    }
    
    void moveRight(Case[][] maze, int colDest){
        int currentCol=this.col;
        while(currentCol<maze[0].length-1 || currentCol!=colDest){
            if(maze[this.row][currentCol+1].isWall()) break;
            currentCol++;
        }
        this.col=currentCol;
    }
    
    void moveDown(Case[][] maze, int rowDest){
        int currentRow=this.row;
        while(currentRow<maze.length-1 && currentRow!=rowDest){
            if(maze[currentRow+1][this.col].isWall()) break;
            currentRow++;
        }
        this.row=currentRow;
    }
    
    void moveLeft(Case[][] maze, int colDest){
        int currentCol=this.col;
        while(currentCol>0 || currentCol!=colDest){
            if(maze[this.row][currentCol-1].isWall()) break;
            currentCol--;
        }
        this.col=currentCol;
    }
    /* FIN FONCTIONS DE DEPLACEMENT */
}