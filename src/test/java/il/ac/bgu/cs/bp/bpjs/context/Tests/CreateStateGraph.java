package il.ac.bgu.cs.bp.bpjs.context.Tests;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.Exporter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class CreateStateGraph {
  public static void mapSpace(BProgram bprog, String name) throws Exception {
    StateSpaceMapper mpr = new StateSpaceMapper();
    var res = mpr.mapSpace(bprog);
    System.out.println("// completed mapping the states graph");
    System.out.println(res.toString());
    writeGraphs(res, name);

    // Generates a compressed file with all possible paths. Could be huge.
//    writeCompressedPaths(name + ".csv", null, res, "exports");
  }

  private static Exporter formatExporter(Exporter e){
    e.setVertexAttributeProvider(mapperVertex -> Map.of());
    return e;
  }

  private static void writeGraphs(MapperResult res, String name) throws IOException {
    System.out.println("// Export to GraphViz...");
    var outputDir = "exports";
    var path = Paths.get(outputDir, name + ".dot").toString();
    formatExporter(new DotExporter(res, path, name)).export();
  }
}
