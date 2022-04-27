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

    //TODO: check si le reste est encore utile (pas de version terminal normalement ?)
    /*public void display() {
        for (int i = 0; i < width; i++) {
            // draw the north edge
            for (int j = 0; j < height; j++) System.out.print((maze[j][i] & 1) == 0 ? "+---" : "+   ");
            System.out.println("+");
            
            // draw the west edge
            for (int j = 0; j < height; j++) {
                System.out.print((maze[j][i] & 8) == 0 ? "| " : "  ");
                String pion_aff = "  ";
                for (int k = 0; k < perso.length; k++) {
                    if (perso[k].getX() == j && perso[k].getY() == i) {
                        if(pion_aff == "  ") pion_aff = k+" ";
                        else{
                            pion_aff = "* ";
                            break;
                        }
                    }
                }
                System.out.print(pion_aff);
                //System.out.print( ( p.getX() == j && p.getY() == i)? "1 " : "  ");
                //System.out.print( ( (maze[j][i] & 8) == 0 && !(p.getX() == j && p.getY() == i))? "  " : "");
            }
            System.out.println("|");
        }
        
        // draw the bottom line
        for (int j = 0; j < height; j++) System.out.print("+---");
        System.out.println("+");
    }

    public void go_up(Pion p){
        if (!((maze[p.getX()][p.getY()] & 1) == 0)){
            p.setY(p.getY()-1);
            System.out.println("up");
        }
        else System.out.println("on ne peut pas monter");
    }

    public void go_down(Pion p){
        if (p.getY() < width - 1 && !((maze[p.getX()][p.getY()+1] & 1) == 0)){
            p.setY(p.getY()+1);
            System.out.println("down");
        }
        else System.out.println("on ne peut pas descendre");
    }

    public void go_left(Pion p){
        if (p.getX() > 0 && !((maze[p.getX()][p.getY()] & 8) == 0)){
            p.setX(p.getX()-1);
            System.out.println("left");
        }
        else System.out.println("on ne peut pas aller à gauche");
    }

    public void go_right(Pion p){
        if (p.getX() < height - 1 && !((maze[p.getX()+1][p.getY()] & 8) == 0)){
            p.setX(p.getX()+1);
            System.out.println("right");
        }
        else System.out.println("on ne peut pas aller à droite");
    }

    public static void main(String[] args) {
        int x = args.length >= 1 ? (Integer.parseInt(args[0])) : 8;
        int y = args.length == 2 ? (Integer.parseInt(args[1])) : 8;
        Random r = new Random();

        x = r.nextInt(20-10) + 10;
        y = r.nextInt(20-10) + 10;
        System.out.println("taille du labyrinthe :"+x+"x"+y);
        MazeGenerator maze = new MazeGenerator(x, y, 5);

        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < maze.perso.length; j++) {
                Random random = new Random();
                int n = random.nextInt(4);
                switch (n) {
                    case 0: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_up(maze.perso[j]);
                        break;
                    case 1: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_down(maze.perso[j]);
                        break;
                    case 2: 
                        System.out.print("joueur n°"+j+" : ");

                        maze.go_left(maze.perso[j]);
                        break;
                    case 3: 
                        System.out.print("joueur n°"+j+" : ");
                        maze.go_right(maze.perso[j]);
                        break;
                }
            }
            maze.display();
        }
        
        EventQueue.invokeLater(() -> new View(maze));
    }*/
}