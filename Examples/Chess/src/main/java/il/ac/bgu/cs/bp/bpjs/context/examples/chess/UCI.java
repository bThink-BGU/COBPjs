package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
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
    private static final String AUTHOR = "Achiya Elyasaf";


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
            else if (line.startsWith("go")) bprog.enqueueExternalEvent(new BEvent("My Turn"));
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
            Color color = input.contains("moves") ? Color.Black : Color.White;
            bprog.enqueueExternalEvent(new BEvent("Color", Color.Black));
            blackEventsListener.setColor(Color.Black);
            bprog.enqueueExternalEvent(new BEvent("init_end"));
        }

        if (input.contains("moves")) {
            input = input.substring(input.length() - 5, input.length() - 1);
            if (input.length() > 0) {
                bprog.enqueueExternalEvent(MoveTranslator.StringToMove(MoveTranslate(input)));
            }
        }
    }

    private void splitFen(String fen) {
        String[] lines = fen.split("/");
        int bRooks = 1, wRooks = 1, bKnights = 1, wKnights = 1, bBishops = 1, wBishops = 1, bPawns = 1, wPawns = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (int j = 0; j < line.length(); j++) {
                int x = 0;
                int y = 7;
                String piece = "";
                Piece p = null;
                if (line.charAt(j) == 'r') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.Rook, bRooks++);
                } else if (line.charAt(j) == 'R') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.Rook, wRooks++);
                } else if (line.charAt(j) == 'n') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.Knight, bKnights++);
                } else if (line.charAt(j) == 'N') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.Knight, wKnights++);
                }else if (line.charAt(j) == 'b') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.Bishop, bBishops++);
                }
                else if (line.charAt(j) == 'B') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.Bishop, wBishops++);
                }
                else if (line.charAt(j) == 'p') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.Pawn, bPawns++);
                }
                else if (line.charAt(j) == 'P') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.Pawn, wPawns++);
                }
                else if (line.charAt(j) == 'q') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.Queen, 1);
                } else if (line.charAt(j) == 'Q') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.Queen, 1);
                } else if (line.charAt(j) == 'k') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.Black, Piece.Type.King, 1);
                } else if (line.charAt(j) == 'K') {
                    x = getRow(line, j);
                    y = y - i;
                    p = new Piece(Color.White, Piece.Type.King, 1);
                } else if (!Character.isDigit(line.charAt(j))) {
                    throw new UnsupportedOperationException("Need to support other types of pieces");
                }
                insert(p, x, y);
            }
        }
    }

    private void insert(Piece p, int x, int y) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Piece", p);
        parameters.put("Row", x);
        parameters.put("Col", y);
        bprog.enqueueExternalEvent(new BEvent("AddPiece", parameters));
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
