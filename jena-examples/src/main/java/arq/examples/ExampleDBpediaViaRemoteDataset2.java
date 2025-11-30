package arq.examples;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.vocabulary.RDFS;

/**
 * Example that uses the {@link Resource} API over an HTTP−backed DatasetGraph.
 * Note, that each access fires an HTTP request. Hence, this pattern is only
 * meaningful for lightweight infrequent lookups.
 * In general, it is recommended to first create local in−memory snapshots of
 * a remote endpoint's data using e.g. CONSTRUCT queries first.
 */
public class ExampleDBpediaViaRemoteDataset2 {
    public static void main(String... args) {
        Dataset ds = DatasetFactory.wrap(DatasetGraphOverRDFLink.create(() ->
            RDFLinkHTTP.newBuilder()
                .destination("https://dbpedia.org/sparql")
                .build()));

        Model model = ds.getDefaultModel();
        Resource r = model.getResource("http://dbpedia.org/resource/Apache_Jena");
        List<RDFNode> list = Iter.toList(r.listProperties(RDFS.label).mapWith(Statement::getObject));
        int n = list.size();
        System.out.println("Got " + n + " labels from DBpedia:");
        IntStream.range(0, n).forEach(i -> {
            System.out.println((i + 1) + ": " + list.get(i));
        });

        /* Output is expected to be similar to:
             Got 5 labels from DBpedia:
             1: "Apache Jena"@en
             2: "Jena (Framework)"@de
             3: "Jena (framework)"@fr
             4: "Jena (informatica)"@it
                5: "아파치 제나"@ko
        */
    }
}
