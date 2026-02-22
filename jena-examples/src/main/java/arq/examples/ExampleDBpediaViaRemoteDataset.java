package arq.examples;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.adapter.SparqlAdapterRegistry;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;
import org.apache.jena.sparql.exec.QueryExecWrapper;
import org.apache.jena.sparql.exec.RowSetOps;

/**
 * An example that sends a query to the DBpedia endpoint via the {@link SparqlAdapterRegistry} abstraction.
 *
 */
public class ExampleDBpediaViaRemoteDataset {
    public static void main(String... args) {
        execLocal("Local Execution");
        System.out.println();
        execRemote("Remote Execution");
    }

    private static void execLocal(String label) {
        execQuery(label, DatasetGraphFactory.empty(), "SELECT * { BIND('test' AS ?x) }");
    }

    private static void execRemote(String label) {
        // The query string is sent to the DBpedia endpoint as is (without parsing).
        // By default, Jena would fail to parse it because of the undeclared prefixes.
        String queryString = """
            SELECT *
              FROM <http://dbpedia.org>
            {
              ?s rdfs:label ?o .
              ?o bif:contains 'Leipzig'
            }
            LIMIT 3
            """;

        // Execution via DatasetGraph
        DatasetGraph dsg = new DatasetGraphOverRDFLink(() ->
            RDFLinkHTTP.newBuilder()
                .destination("http://dbpedia.org/sparql")
                .build());

        execQuery(label, dsg, queryString);
        System.out.println();

        // Execution via RDFLink (via DatasetGraph)
        try (RDFConnection conn = RDFConnection.connect(DatasetFactory.wrap(dsg))) {
            QueryExecBuilder builder = QueryExecBuilderAdapter.adapt(conn.newQuery()).query(queryString);
            execQuery(label + " via Connection", dsg, builder);
        }
    }

    private static void execQuery(String label, DatasetGraph dsg, String queryString) {
        QueryExecBuilder builder;
        builder = QueryExec.dataset(dsg).query(queryString);
        execQuery(label + " Direct", dsg, builder);
        System.out.println();
        builder = QueryExec.newBuilder().dataset(dsg).query(queryString);
        execQuery(label + " Deferred", dsg, builder);
    }

    private static void execQuery(String label, DatasetGraph dsg, QueryExecBuilder builder) {
        try (QueryExec qe = builder
                .timeout(10, TimeUnit.SECONDS)
                .transformExec(e -> new QueryExecWrapperDemo(label, e)).build()) {
            System.out.println(label + ": Dataset type: " + className(dsg));
            System.out.println(label + ": QueryExecBuilder type: " + className(builder));
            System.out.println(label + ": QueryExec type: " + className(qe));
            RowSetOps.out(System.out, qe.select());
        }
    }

    private static class QueryExecWrapperDemo extends QueryExecWrapper<QueryExec> {
        private final String label;

        public QueryExecWrapperDemo(String label, QueryExec delegate) {
            super(delegate);
            this.label = label;
        }

        @Override
        protected <T> T exec(Supplier<T> supplier) {
            T r = supplier.get();
            System.out.println(label + ": Execution result object type: " + className(r));
            return r;
        };
    }

    private static String className(Object obj) {
        return obj == null ? "(null)" : className(obj.getClass());
    }

    private static String className(Class<?> clz) {
        return clz.getSimpleName();
    }
}
