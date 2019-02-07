package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MainTest {
    private Main main = new Main();

    @BeforeEach
    void setUp() {
    }

    @Test
    void run() throws InterruptedException {
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream psOut = new PrintStream(baosOut);
        PrintStream psErr = new PrintStream(baosErr);
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        System.setOut(psOut);
        System.setErr(psErr);
        main.run("test_population_data.js", "ContextDB-Test");
        System.out.flush();
        System.err.flush();
        System.setOut(oldOut);
        System.setErr(oldErr);
//        System.out.println(baosOut.toString());

        Pattern regex = Pattern.compile("(.*)Started\n(.*)",Pattern.DOTALL);
        Matcher matcher = regex.matcher(baosOut.toString());
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