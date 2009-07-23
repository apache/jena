package dev;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.Test;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class ReportTDB
{
    public static void main(String[] args) throws Exception
    {
        Dataset ds = TDBFactory.createDataset();

        Model model = ds.getNamedModel("http://example.org/");

        String rdf =
            "@prefix ex: <http://example.org/ontology.owl#> . " +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . " +

            "<http://event1/> a ex:Event . " +
            "<http://event1/> ex:size 1 . " +

            "<http://event2/> a ex:Event . " +
            "<http://event2/> ex:size 5 . ";

        model.read(new StringReader(rdf), null, "TURTLE");

        String query =
            "PREFIX ex: <http://example.org/ontology.owl#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +

            "SELECT ?event WHERE { " +
            "?event a ex:Event . " +
            "?event ex:size ?size . " +
            "FILTER (?size < 3) " +
            "}";

        ResultSet resultSet =
            QueryExecutionFactory.create(QueryFactory.create(query),
                                         model).execSelect();

        ResultSetFormatter.out(resultSet) ;
        //String uri = resultSet.nextSolution().getResource("event").getURI();
    }
    
    @Test
    public void testTDBNamedModelWithFilterQuery() {
        Dataset ds = TDBFactory.createDataset();

        Model model = ds.getNamedModel("http://example.org/");

        String rdf =
            "@prefix ex: <http://example.org/ontology.owl#> . " +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . " +

            "<http://event1/> a ex:Event . " +
            "<http://event1/> ex:size 1 . " +

            "<http://event2/> a ex:Event . " +
            "<http://event2/> ex:size 5 . ";

        model.read(new StringReader(rdf), null, "TURTLE");

        String query =
            "PREFIX ex: <http://example.org/ontology.owl#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +

            "SELECT ?event WHERE { " +
            "?event a ex:Event . " +
            "?event ex:size ?size . " +
            "FILTER (?size < 3) " +
            "}";

        ResultSet resultSet =
            QueryExecutionFactory.create(QueryFactory.create(query),
                                         model).execSelect();

        assertThat(resultSet.hasNext(), is(true));

        String uri = resultSet.nextSolution().getResource("event").getURI();
        assertThat(uri, equalTo("http://event1/"));

        assertThat(resultSet.hasNext(), is(false));
    }
}


