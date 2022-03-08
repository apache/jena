package org.apache.jena.riot.rowset.rw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ext.com.google.common.base.StandardSystemProperty;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetMem;
import org.apache.jena.sparql.util.Context;

public class RowSetJSONStreamingBenchmark {

    public static void download(Path dataFile, Path dataFileTmp, Callable<InputStream> inSupp) throws Exception {
        if (!Files.exists(dataFile)) {
            System.out.println("Attempting to dowload test data");

            if (Files.exists(dataFileTmp)) {
                throw new RuntimeException("Partial data found " + dataFileTmp +
                        " - Either wait for the process writing it or if there is none then delete it manually");
            }

            try (OutputStream out = Files.newOutputStream(dataFileTmp)) {
                try (InputStream in = inSupp.call()) {
                    IOUtils.copy(in, out);
                }
                out.flush();
            }
            try {
                Files.move(dataFileTmp, dataFile, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(dataFileTmp, dataFile);
            }
            System.out.println("Data retrieved");
        }

    }

    public static <T> T benchmark(String label, Callable<T> action) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        T result = action.call();
        float elapsed = sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f;
        System.out.println("Time taken for " + label + ": " + elapsed + "s");
        return result;
    }

    public static void main(String[] args) throws Exception {

        Path dataFile = Path.of(StandardSystemProperty.JAVA_IO_TMPDIR.value()).resolve("jena-rs.json");
        Path dataFileTmp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");

        download(dataFile, dataFileTmp, () ->
            new URL("http://moin.aksw.org/sparql?query=SELECT%20*%20{%20?s%20?p%20?o%20}").openStream());

        for (int i = 0; i < 30; ++i) {
            runOnce("iteration" + i, dataFile);
        }

    }


    public static void runOnce(String label, Path dataFile) throws Exception {

        // TODO Read test data from class path resource
        Context cxt = ARQ.getContext().copy();
        cxt.setTrue(ARQ.inputGraphBNodeLabels);


        RowSet expectedsInit = benchmark(label + ":expected:setup", () ->
            RowSetReaderJSON.factory.create(ResultSetLang.RS_JSON).read(Files.newInputStream(dataFile), cxt));

        RowSet expecteds = benchmark(label + ":expected:consumption", () -> RowSetMem.create(expectedsInit));

        RowSet actualsInit = benchmark(label + ":actual:setup", () ->
            RowSetReaderJSONStreaming.factory.create(ResultSetLang.RS_JSON).read(Files.newInputStream(dataFile), cxt));

        RowSet actuals = benchmark(label + ":actual:consumption", () -> RowSetMem.create(actualsInit));


        long seenItems = 0;
        boolean isOk = true;
        while (true) {
            boolean ahn = actuals.hasNext();
            boolean ehn = expecteds.hasNext();

            if (ahn == ehn) {
                if (ahn) {
                    Binding a = actuals.next();
                    Binding e = expecteds.next();

                    if (!Objects.equals(a, e)) {
                        System.out.println(String.format("Difference at %d/%d: %s != %s",
                                actuals.getRowNumber(), expecteds.getRowNumber(), a , e));

                        isOk = false;
                    }
                } else {
                    break;
                }
            } else {
                System.out.println("Result set lengths differ");
                isOk = false;
                break;
            }
            ++seenItems;
        }

        System.out.println("Result sets are " + (isOk ? "" : "NOT ") + "equal - items seen: " + seenItems);

        // boolean isIsomorphic = ResultSetCompare.isomorphic(actuals, expecteds);
        // System.out.println("Isomorphic: " + isIsomorphic);

        actuals.close();
        expecteds.close();
    }
}
