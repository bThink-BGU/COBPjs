package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import java.io.*;
import java.util.Scanner;

/**
 * Created By: Assaf, On 14/02/2020
 * Description:
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        String inData = "position fen r3k2r/8/8/8/8/8/8/4K3 w k - 0 1\n";
        InputStream stdin = System.in;
        System.setIn(new ByteArrayInputStream(inData.getBytes()));

        UCI uci = new UCI();
        Thread t = new Thread(uci);
        t.start();
        t.join();
    }
}