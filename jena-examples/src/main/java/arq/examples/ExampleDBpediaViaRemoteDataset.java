package arq.examples;

import java.util.concurrent.TimeUnit;

import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;

/**
 * An example that sends a query to the DBpedia endpoint via the {@link DatasetGraphOverRDFLink} abstraction.
 * Technically, the query is passed through Jena's default {@link SparqlDispatcherRegistry}.
 */
public class ExampleDBpediaViaRemoteDataset {
    public static void main(String... argv) {
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

        DatasetGraph dsg = new DatasetGraphOverRDFLink(() ->
            RDFLinkHTTP.newBuilder()
                .destination("http://dbpedia.org/sparql")
                .build());

        Table table = QueryExec.dataset(dsg)
            .timeout(10, TimeUnit.SECONDS)
            .query(queryString)
            .table();

        RowSetOps.out(System.out, table.toRowSet());
    }
}
