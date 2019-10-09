package il.ac.bgu.cs.bp.bpjs.context.examples.gol;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.hibernate.Session;
import org.sqlite.Function;

import java.sql.SQLException;

public class Main {
    void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
        System.out.println(">>>>>>>>>>>>>>>>>> Game-of-Life example <<<<<<<<<<<<<<<<<<<");

        ContextService contextService = ContextService.getInstance();
        /*contextService.addEntityManagerCreateHook(em -> {
            Session session = em.unwrap(Session.class);

            session.doWork(connection -> Function.create(connection, "NGB", new Function() {
                @Override
                protected void xFunc() throws SQLException {
                    int i = value_int(0);
                    int j = value_int(1);
                    result(
                            String.format("(SELECT n FROM Cell n WHERE (" +
                                    "(n.i=%1$s-1 AND n.j=%2$s-1 AND n.alive) OR " +
                                    "(n.i=%1$s-1 AND n.j=%2$s AND n.alive) OR " +
                                    "(n.i=%1$s-1 AND n.j=%2$s+1 AND n.alive) OR " +
                                    "(n.i=%1$s AND n.j=%2$s-1 AND n.alive) OR " +
                                    "(n.i=%1$s AND n.j=%2$s+1 AND n.alive) OR " +
                                    "(n.i=%1$s+1 AND n.j=%1$s-1 AND n.alive) OR " +
                                    "(n.i=%1$s+1 AND n.j=%2$s AND n.alive) OR " +
                                    "(n.i=%1$s+1 AND n.j=%2$s+1 AND n.alive) ))", i, j));
                }
            }));
            session.doWork(connection -> Function.create(connection, "NGB_COUNT_OLD", new Function() {
                @Override
                protected void xFunc() throws SQLException {
                    int i = value_int(0);
                    int j = value_int(1);
                    String query = String.format("SELECT COUNT(n) from Cell n WHERE (" +
                            "(n.i=%1$s-1 AND n.j=%2$s-1 AND n.alive) OR " +
                            "(n.i=%1$s-1 AND n.j=%2$s AND n.alive) OR " +
                            "(n.i=%1$s-1 AND n.j=%2$s+1 AND n.alive) OR " +
                            "(n.i=%1$s AND n.j=%2$s-1 AND n.alive) OR " +
                            "(n.i=%1$s AND n.j=%2$s+1 AND n.alive) OR " +
                            "(n.i=%1$s+1 AND n.j=%2$s-1 AND n.alive) OR " +
                            "(n.i=%1$s+1 AND n.j=%2$s AND n.alive) OR " +
                            "(n.i=%1$s+1 AND n.j=%2$s+1 AND n.alive) ))", i, j);
                    System.out.println(query);
                    result(query);
                }
            }));
            session.doWork(connection -> Function.create(connection, "NGB_COUNT", new Function() {
                @Override
                protected void xFunc() throws SQLException {
                    String paramName = value_text(0);
                    String query = String.format("(SELECT COUNT(n) from Cell n WHERE (" +
                            "(n.i=%1$s.i-1  AND n.j=%1$s.j-1 AND n.alive = true) OR " +
                            "(n.i=%1$s.i-1  AND n.j=%1$s.j AND n.alive = true) OR " +
                            "(n.i=%1$s.i-1  AND n.j=%1$s.j+1 AND n.alive = true) OR " +
                            "(n.i=%1$s.i    AND n.j=%1$s.j-1 AND n.alive = true) OR " +
                            "(n.i=%1$s.i    AND n.j=%1$s.j+1 AND n.alive = true) OR " +
                            "(n.i=%1$s.i+1  AND n.j=%1$s.j.j-1 AND n.alive = true) OR " +
                            "(n.i=%1$s.i+1  AND n.j=%1$s.j AND n.alive = true) OR " +
                            "(n.i=%1$s.i+1  AND n.j=%1$s.j+1 AND n.alive = true) ))", paramName);
                    System.out.println(query);
                    result(query);
                }
            }));
        });*/
        contextService.initFromResources(persistenceUnit, dbPopulationScript, "program.js");
        BProgram bprog = contextService.getBProgram();
        bprog.setWaitForExternalEvents(false);
        contextService.run();

        // Simulation of external events
        /*Thread.sleep(3000);
        ContextService.getContextInstances("Generation", Generation.class)
                .forEach(cell -> bprog.enqueueExternalEvent(new BEvent("Click", cell)));*/

//        Thread.sleep(6000);
//        contextService.close();
    }


    public static void main(String[] args) throws InterruptedException {
        new Main().run("db_population.js", "ContextDB");
    }
}
