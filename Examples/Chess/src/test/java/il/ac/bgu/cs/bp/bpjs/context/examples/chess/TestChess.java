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
                dbPopulationScript, "test_population_data.js");
        contextService.run();
        BProgram bprog = contextService.getBProgram();

        // Simulation of external events
        Thread.sleep(1000);
        bprog.enqueueExternalEvent(new BEvent("Color", Color.Black));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Piece", new Piece(Color.Black, Piece.Type.Rook, 1));
        parameters.put("Row", 0);
        parameters.put("Col", 0);
        bprog.enqueueExternalEvent( new BEvent("AddPiece", Color.Black));
        bprog.enqueueExternalEvent( new BEvent("init_end"));

        //ContextService.close();
    }


    public static void main(String[] args) throws InterruptedException {
        new TestChess().run("db_population.js", "ContextDB");
    }
}
