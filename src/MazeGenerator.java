import java.util.Collections;
import java.util.Arrays;

public class MazeGenerator{
    final int height;
    final int width;
    final int[][] maze; //le labyrinthe en dur

    public MazeGenerator(int height, int width){
        this.height=height;
        this.width=width;
        maze=new int[this.height][this.width];
        generateMaze(0, 0);
    }
     
    private void generateMaze(int cx, int cy){
        DIR[] dirs=DIR.values();
        Collections.shuffle(Arrays.asList(dirs));
        int nx, ny;
        for(DIR dir : dirs){
            nx=cx+dir.dx;
            ny=cy+dir.dy;
            if(between(nx, height) && between(ny, width) && (maze[nx][ny]==0)){
                maze[cx][cy]|=dir.bit;
                maze[nx][ny]|=dir.opposite.bit;
                generateMaze(nx, ny);
            }
        }
    }

    private enum DIR{
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
        private final int bit;
        private final int dx;
        private final int dy;
        private DIR opposite;

        //use the static initializer to resolve forward references
        static{
            N.opposite=S;
            S.opposite=N;
            E.opposite=W;
            W.opposite=E;
        }

        private DIR(int bit, int dx, int dy){
            this.bit=bit;
            this.dx=dx;
            this.dy=dy;
        }
    };
    
    private static boolean between(int v, int upper){
        return (v>=0) && (v<upper);
    }
}