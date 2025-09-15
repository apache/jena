package rdfconnection.examples;

import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.resultset.ResultsFormat;

/**
 * Example that showcases querying DBpedia with disabled parse check and
 * different accept headers.
 * <p>
 *
 * The expected output should be similar to this snippet:
 * <pre>
 * Trying to query with content type: application/sparql-results+thrift
 * Request failed: Not Acceptable
 *
 * Trying to query with content type: application/sparql-results+json
 * -------------------------
 * | l                     |
 * =========================
 * | "Jena (Framework)"@de |
 * -------------------------
 * </pre>
 */
public class RDFConnectionExampleDBpedia01 {
    public static void main(String[] args) {
        List<String> acceptHeaders = List.of(WebContent.contentTypeResultsThrift, WebContent.contentTypeResultsJSON);

        // Note that the query string uses the prefix 'rdfs:' without declaring it.
        // The DBpedia server can parse such a query against its configured default prefixes.
        String queryString = "SELECT * { <http://dbpedia.org/resource/Apache_Jena> rdfs:label ?l } ORDER BY ?l LIMIT 1";

        for (String acceptHeader : acceptHeaders) {
            System.out.println("Trying to query with content type: " + acceptHeader);
            try (RDFConnection conn = RDFConnectionRemote.service("http://dbpedia.org/sparql")
                    .acceptHeaderSelectQuery(acceptHeader).parseCheckSPARQL(false).build()) {
                try (QueryExecution qe = conn.query(queryString)) {
                    ResultSet rs = qe.execSelect();
                    ResultSetFormatter.output(System.out, rs, ResultsFormat.FMT_TEXT);
                } catch (Exception e) {
                    System.out.println("Request failed: " + e.getMessage());
                }
            }
            System.out.println();
        }

        System.out.println("Done.");
    }
}
