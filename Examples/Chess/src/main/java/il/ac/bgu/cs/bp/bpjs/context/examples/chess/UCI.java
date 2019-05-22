package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.events.Move;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static il.ac.bgu.cs.bp.bpjs.context.examples.chess.MoveTranslator.MoveTranslate;

public class UCI extends BProgramRunnerListenerAdapter implements Runnable {
    private PrintStream uciOut;
    private Scanner uciIn;
    private PrintStream logger;
    private boolean wasInitialized = false;
    private ContextService contextService;
    private BProgram bprog;
    private Color myColor;

    private static final String ENGINENAME = "BPChess";
    private static final String AUTHOR = "Achiya Elyasaf";


    public UCI(InputStream in, PrintStream uciOut, PrintStream logger) {
        this.uciOut = uciOut;
        this.uciIn = new Scanner(in);
        this.logger = logger;

    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        if (theEvent instanceof Move) {
            Move mv = (Move) theEvent;
            if(mv.source.piece.color.equals(myColor))
                sendMove(MoveTranslator.MoveToString(mv));
        }
    }
    
    private void writeToUci(String message) {
        writeToLogger("Sending to uci - "+ message);
        uciOut.println(message);
    }

    private void writeToLogger(String message) {
        logger.println("UCI.java: " + message);
        logger.flush();
    }

    private void closeCommunication() {
        uciIn.close();
    }

    private void initCommunication() {
        String line;
        while (!(line = uciIn.nextLine()).equals("uci")) {
            writeToLogger("Received from UCI -" + line);
        }
        writeToUci("id name " + ENGINENAME);
        writeToUci("id author " + AUTHOR);
        writeToUci("uciok");
    }

    public void run() {
        initCommunication();
        String line;
        while (true) {
            line = uciIn.nextLine();
            writeToLogger("Received from UCI -" + line);
            if (line.startsWith("setoption")) setOptions(line);
            else if ("isready".equals(line)) isReady();
            else if ("ucinewgame".equals(line)) newGame();
            else if ("stop".equals(line)) newGame();
            else if (line.startsWith("position")) newPosition(line);
            else if (line.startsWith("go")) bprog.enqueueExternalEvent(new BEvent("My Turn"));
            else if ("quit".equals(line)) {
                quitGame();
                return;
            }
        }
    }

    private void sendMove(String move) {
        writeToUci("bestmove " + move);
    }

    private static void setOptions(String input) {
        // set options
    }

    private void isReady() {
        writeToUci("readyok");
    }

    public void newGame() {
        quitGame();
        contextService = ContextService.getInstance();
        //contextService.alwaysUpdateEntities();
        this.bprog = contextService.getBProgram();
        contextService.initFromResources("ContextDB", "db_population.js", "program.js");
        bprog = contextService.getBProgram();
        bprog.setWaitForExternalEvents(true);

//        contextService.addListener(new PrintBProgramRunnerListener());
        contextService.addListener(this);
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
            String fen[] = input.split(" ");
            placePieces(fen[0]);
            Color color = input.contains("moves") ? Color.Black : Color.White;
            bprog.enqueueExternalEvent(new BEvent("Color", Color.Black));
            this.myColor = Color.Black;
            bprog.enqueueExternalEvent(new BEvent("init_end"));
        }

        if (input.contains("moves")) {
            input = input.substring(input.length() - 5, input.length() - 1);
            if (input.length() > 0) {
                bprog.enqueueExternalEvent(new BEvent("EnemyMove", MoveTranslate(input)));
            }
        }
    }

    private void placePieces(String fen) {
        Map<Character, Piece.Type> pieceChar = ImmutableMap.<Character, Piece.Type>builder()
                .put('r', Piece.Type.Rook)
                .put('k', Piece.Type.King)
                .put('q', Piece.Type.Queen)
                .put('p', Piece.Type.Pawn)
                .put('b', Piece.Type.Bishop)
                .put('n', Piece.Type.Knight).build();

        Map<Character, Integer> pieceCounter = new HashMap<>();

        String[] rows = fen.split("/");
        Map<Character, Integer> piecesCounter = Maps.newHashMap();
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            int j = 0;
            for(char c : row.toCharArray()){
                if (Character.isDigit(c)) {
                    j += (c - 48);
                } else if (Character.isAlphabetic(c)) {
                    Color color = Character.isLowerCase(c) ? Color.Black : Color.White;
                    int id = pieceCounter.compute(c,(key, count) -> count == null ? 1 : count + 1);
                    Piece piece = new Piece(color, pieceChar.get(Character.toLowerCase(c)), id);
                    placePiece(piece, i, j);
                    j++;
                }
            }
        }
    }

    private void placePiece(Piece p, int x, int y) {
        Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put("Piece", p)
            .put("Row", x)
            .put("Col", y).build();
        bprog.enqueueExternalEvent(new BEvent("AddPiece", parameters));
    }

    private void quitGame() {
        try {
            contextService.close();
            writeToUci("Good game");
        } catch (Exception e) { }
    }

}
