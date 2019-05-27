package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.HashMap;
import java.util.Map;

public class TestChess {
    void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
        System.out.println(">>>>>>>>>>>>>>>>>> Chess example <<<<<<<<<<<<<<<<<<<");

        ContextService contextService = ContextService.getInstance();
        contextService.initFromResources(persistenceUnit,
                dbPopulationScript, "program.js", "test_population_data.js");
        contextService.run();
        BProgram bprog = contextService.getBProgram();

        // Simulation of external events
        bprog.enqueueExternalEvent(new BEvent("Color", Color.Black));
        bprog.enqueueExternalEvent( new BEvent("AddPiece", new HashMap<String, Object>(){{
            put("Piece", new Piece(Color.Black, Piece.Type.Rook, 1));
            put("Row", 0);
            put("Col", 0);
        }}));
        bprog.enqueueExternalEvent( new BEvent("AddPiece", new HashMap<String, Object>(){{
            put("Piece", new Piece(Color.Black, Piece.Type.King, 1));
            put("Row", 0);
            put("Col", 1);
        }}));
        bprog.enqueueExternalEvent( new BEvent("AddPiece", new HashMap<String, Object>(){{
            put("Piece", new Piece(Color.White, Piece.Type.King, 1));
            put("Row", 3);
            put("Col", 6);
        }}));
        bprog.enqueueExternalEvent( new BEvent("init_end"));
        Thread.sleep(2000);
        bprog.enqueueExternalEvent(new BEvent("EnemyMove", new int[]{3,6,3,7}));
        Thread.sleep(2000);
        contextService.close();
    }


    public static void main(String[] args) throws InterruptedException {
        new TestChess().run("db_population.js", "ContextDB");
    }
}
