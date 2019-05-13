package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Main {
    private UCI uci;
    private PrintWriter chessLog;

    public Main() throws FileNotFoundException, UnsupportedEncodingException {
        chessLog = new PrintWriter("chess.log","UTF-8");
        this.uci = new UCI(System.in, System.out, chessLog);
    }

    public void  run() {
        uci.initCommunication();
        Thread l = new Thread(uci);
        l.start();
        uci.newGame();
        System.out.println("end of run");
    }


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        new Main().run();


    }
}