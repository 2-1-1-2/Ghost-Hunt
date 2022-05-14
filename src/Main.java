public class Main{
    public static void main(String[] args) throws InterruptedException{
        Player p1=new Player("p1");
        Player p2=new Player("p2");
        /*Player p3=new Player("p3");
        Player p4=new Player("p4");*/
        Game g=new Game(p1);
        g.addPlayerInGame(p2);
        /*g.addPlayerInGame(p3);
        g.addPlayerInGame(p4);*/
        /*g.gameStart();

        g.moveUp(p1, 50);
        g.moveRight(p1, 50);
        g.moveDown(p1, 50);
        g.moveLeft(p1, 50);

        Thread.sleep(2000);

        g.moveUp(p2, 50);
        g.moveRight(p2, 50);
        g.moveDown(p2, 50);
        Thread.sleep(2000);
        g.moveLeft(p2, 50);*/
        
        /*Ghost ghost=new Ghost(1, 1, 5, g);
        new Thread(ghost).start();
        Thread.sleep(2000);
        System.out.println(ghost.catchGhost());
        Thread.sleep(5000);
        System.out.println(ghost.loseGhost(3, 3));*/
    }
}