package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Piece;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Type;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static il.ac.bgu.cs.bp.bpjs.context.examples.chess.MoveTranslator.MoveTranslate;


public class UCI extends BProgramRunnerListenerAdapter implements Runnable {
    private InputStream in;
    private PrintStream out;
    private Scanner scanner;
    private PrintWriter logger;
    private boolean wasInitialized = false;
    private ContextService contextService;
    private BProgram bprog;
    private BlackEventsListener blackEventsListener;

    private static final String ENGINENAME = "BPChess";
    private static final String AUTHOR = "Ronit and Banuel";


    public UCI(InputStream in, PrintStream out, PrintWriter chessLog) {
        this.in = in;
        this.out = out;
        this.scanner = new Scanner(in);
        this.logger = chessLog;
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {

    }

    public void closeCommunication() {
        scanner.close();
    }

    public void initEnded() {
        out.println("id name " + ENGINENAME);
        out.println("id author " + AUTHOR);
        out.println("uciok");
        logger.println("Ended startInputUCI");
    }

    public void initCommunication() {
        String line = "";
        while (!(line = scanner.nextLine()).equals("uci")) {
            logger.println(line);
        }
        initEnded();
    }

    public void run() {
        String line = "";
        while (true) {
            line = scanner.nextLine();
            logger.println(line);
            if (line.startsWith("setoption")) setOptions(line);
            else if ("isready".equals(line)) isReady();
            else if ("ucinewgame".equals(line)) newGame();
            else if ("stop".equals(line)) newGame();
            else if (line.startsWith("position")) newPosition(line);
            else if (line.startsWith("go"))
                bprog.enqueueExternalEvent(new BEvent("My Turn"));
            else if ("quit".equals(line)) {
                quit();
                return;
            }
        }
    }

    public void sendMove(String move) {
        out.println("bestmove " + move);
    }

    private static void setOptions(String input) {
        // set options
    }

    private static void isReady() {
        System.out.println("readyok");
    }

    public void newGame() {
        contextService = ContextService.getInstance();
        //contextService.alwaysUpdateEntities();
        this.bprog = contextService.getBProgram();
        contextService.initFromResources("ContextDB", "ContextualChess-Population.js", "ContextualChess.js");
        bprog = contextService.getBProgram();
        bprog.setWaitForExternalEvents(true);


        try {
            contextService.addListener(new PrintBProgramRunnerListener(new PrintStream("bp.log")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        contextService.addListener(this);
        blackEventsListener = new BlackEventsListener(this);
        contextService.addListener(blackEventsListener);
        contextService.run();

        //restart the main
        wasInitialized = false;

    }

    private void newPosition(String input) {
        input = input.substring(9).concat(" ");
        // Normal Start
        if (input.contains("startpos ")) {
            input = input.substring(9);
        }
        // Different start
        else if (input.contains("fen") && !wasInitialized) {
            wasInitialized = true;
            input = input.substring(4);
            String fenBoard = input.substring(0, input.indexOf(" w"));
            splitFen(fenBoard);
            if (input.contains("moves")) {
                bprog.enqueueExternalEvent(new BEvent("color", "black"));
                blackEventsListener.setColor(Color.Black);
            } else {
                bprog.enqueueExternalEvent(new BEvent("color", "white"));
                blackEventsListener.setColor(Color.White);
            }
            bprog.enqueueExternalEvent(new BEvent("init_end"));
        }

        if (input.contains("moves")) {
            input = input.substring(input.length() - 5, input.length() - 1);
            if (input.length() > 0) {
                bprog.enqueueExternalEvent(new BEvent("input-" + MoveTranslate(input)));
            }
        }
    }

    private void splitFen(String fen) {
        String[] lines = fen.split("/");
        int bRooks = 1;
        int wRooks = 1;
        int bKnights = 1;
        int wKnights = 1;
        int bBishops = 1;
        int wBishops = 1;
        int bPawns = 1;
        int wPawns = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (int j = 0; j < line.length(); j++) {
                int x = 0;
                int y = 7;
                String piece = "";
                Piece p;
                if (line.charAt(j) == 'r') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.Rook, bRooks);
                    bRooks++;
                    insert(p, x, y);
                } else if (line.charAt(j) == 'R') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.Rook, wRooks);
                    wRooks++;
                    insert(p, x, y);
                } else if (line.charAt(j) == 'n') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.Knight, bKnights);
                    bKnights++;
                    insert(p, x, y);
                } else if (line.charAt(j) == 'N') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.Knight, wKnights);
                    wKnights++;
                    insert(p, x, y);
                }else if (line.charAt(j) == 'b') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.Bishop, bBishops);
                    bBishops++;
                    insert(p, x, y);
                }
                else if (line.charAt(j) == 'B') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.Bishop, wBishops);
                    wBishops++;
                    insert(p, x, y);
                }
                else if (line.charAt(j) == 'p') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.Pawn, bPawns);
                    bPawns++;
                    insert(p, x, y);
                }
                else if (line.charAt(j) == 'P') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.Pawn, wPawns);
                    wPawns++;
                    insert(p, x, y);
                }
                else if (line.charAt(j) == 'q') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.Queen, 1);
                    insert(p, x, y);
                } else if (line.charAt(j) == 'Q') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.Queen, 1);
                    insert(p, x, y);
                } else if (line.charAt(j) == 'k') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Type.King, 1);
                    insert(p, x, y);
                } else if (line.charAt(j) == 'K') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Type.King, 1);
                    insert(p, x, y);
                } else if (!Character.isDigit(line.charAt(j))) {
                    throw new UnsupportedOperationException("Need to support other types of pieces");
                }


            }
        }
    }

    private void insert(Piece p, int x, int y) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("piece", p);
        parameters.put("cell", new Cell(x, y, p));
        bprog.enqueueExternalEvent(new ContextService.UpdateEvent("UpdateCell", parameters));
    }

    private int getRow(String line, int index) {
        int sum = 0;
        for (int i = 0; i < index; i++) {
            if (Character.isDigit(line.charAt(i)))
                sum = sum + Character.getNumericValue(line.charAt(i));
            else
                sum++;
        }
        return sum;
    }

    private void quit() {
        contextService.close();
        out.println("Good game");
    }

}
