package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RoomsExampleTest {
    private RoomsExample roomsExample = new RoomsExample();

    @BeforeEach
    void setUp() {
    }

    @Test
    void run() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);
        roomsExample.run("test_population_data.js", "test");
        System.out.flush();

        System.setOut(old);
        System.out.println(baos.toString());

        Pattern regex = Pattern.compile("(.*)Started\n(.*)",Pattern.DOTALL);
        Matcher matcher = regex.matcher(baos.toString());
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (matcher.find()) {
            System.out.println(matcher.group(2));
//			System.out.println(matcher.group(2));
        }
    }
}