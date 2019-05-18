package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.events.Move;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;

public class MoveTranslator {
    private static int ChartoNumber(char c) {
        return c - 'a';
    }

    private static char NumberToChar(int n) {
        return (char) ('a' + n);
    }

    public static String MoveToString(Move move) {
        return "" + NumberToChar(move.source.i) + (move.source.j+ 1) + NumberToChar(move.target.i) + (move.target.j + 1);
    }

    public static String MoveTranslate(String move) {
        return "" + ChartoNumber(move.charAt(0))+ (Character.getNumericValue(move.charAt(1)) - 1) + ChartoNumber(move.charAt(2))+ (Character.getNumericValue(move.charAt(3)) - 1);
    }

    public static Move StringToMove(String move) {
        String translated = MoveTranslate(move);
        Cell source = new Cell(translated.charAt(0) - 48, translated.charAt(1) - 48);
        Cell target = new Cell(translated.charAt(2) - 48, translated.charAt(3) - 48);
        return new Move(source, target);
    }
}
