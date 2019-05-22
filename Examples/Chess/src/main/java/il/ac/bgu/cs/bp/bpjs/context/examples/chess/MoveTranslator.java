package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.events.Move;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;

public class MoveTranslator {
    private static int rowTranslator(char c) {
        return 8 - c + 48;
    }

    private static int colTranslator(char c) {
        return c - 'a';
    }

    private static char NumberToChar(int n) {
        return (char) ('a' + n);
    }

    public static String MoveToString(Move move) {
        return "" + NumberToChar(move.source.j) + (8-move.source.i) + NumberToChar(move.target.j) + (8-move.target.i) ;
    }

    public static int[] MoveTranslate(String move) {
        return new int[]{
                rowTranslator(move.charAt(1)),
                colTranslator(move.charAt(0)),
                rowTranslator(move.charAt(3)),
                colTranslator(move.charAt(2))
        };
    }
}
