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
      g.gameStart();
      g.moveRight(p1, 50);
      g.moveRight(p2, 50);
  }
}
