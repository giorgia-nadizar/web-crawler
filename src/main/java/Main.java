public class Main {

    public static void main(String[] args) throws Exception {
        VisitedPages visitedPages = new VisitedPages();
        Frontier frontier = new Frontier(6, "https://bartoli.inginf.units.it/", "http://univ.trieste.it/", "https://www.fixfit.it/gambe-snelle-glutei-sodi-e-addome-tonico-allenamento-g", "http://www2.units.it/ramponi/teaching/DIP/materiale/");
        //Frontier frontier = new Frontier(5, "https://www.units.it");
        Spider s1 = new Spider(frontier, visitedPages);
        Spider s2 = new Spider(frontier, visitedPages);
        Spider s3 = new Spider(frontier, visitedPages);
        Spider s4 = new Spider(frontier, visitedPages);
        Spider s5 = new Spider(frontier, visitedPages);
        Spider s6 = new Spider(frontier, visitedPages);
        Refresher r = new Refresher(frontier, visitedPages);
        System.out.println("threads all created");
        s1.start();
        s2.start();
        s3.start();
        s4.start();
        s5.start();
        s6.start();
        r.start();
        System.out.println("threads all launched");
    }
}
