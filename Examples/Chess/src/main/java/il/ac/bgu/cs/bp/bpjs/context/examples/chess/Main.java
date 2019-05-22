package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Main {
    private UCI uci;
    private PrintStream logger;
    private PrintStream stdout;

    public Main() throws FileNotFoundException, UnsupportedEncodingException {
        logger = new PrintStream("bp.log","UTF-8");
        stdout = System.out;
        System.setOut(logger);
        System.setErr(logger);
        this.uci = new UCI(System.in, stdout, logger);
    }

    public void run() {
//        uci.initCommunication();
        Thread l = new Thread(uci);
        l.start();
//        uci.newGame();
//        System.out.println("end of run");
    }


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        new Main().run();
    }
}