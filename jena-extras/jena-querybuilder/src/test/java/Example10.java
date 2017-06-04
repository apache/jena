

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.DC_11;
import org.junit.Test;

public class Example10 {

	/**
	 * 	Example 10: 
	 * 
	 *  @see https://www.w3.org/TR/sparql11-update/#example_10
	 */
	@Test
	public void example10()
	{

		Resource book1 = ResourceFactory.createResource( "http://example/book1" );
		Resource book3 = ResourceFactory.createResource( "http://example/book3" );
		Resource book4 = ResourceFactory.createResource( "http://example/book4" );
		
		Literal d1996 = ResourceFactory.createTypedLiteral("1996-01-01T00:00:00-02:00", XSDDatatype.XSDdateTime);
		
		Node graphName1 = NodeFactory.createURI("http://example/bookStore");
		Node graphName2 = NodeFactory.createURI("http://example/bookStore2");
		
		Model m1 = ModelFactory.createDefaultModel();
		m1.add( book1, DC_11.title, "Fundamentals of Compiler Design");
		m1.add( book1, DC_11.date, d1996 );
		m1.add( book1, DC_11.date, DCTypes.PhysicalObject );
			
		m1.add( book3, DC_11.title, "SPARQL 1.1 Tutorial" );
		
		Model m2 = ModelFactory.createDefaultModel();
		m2.add( book4, DC_11.title, "SPARQL 1.1 Tutorial" );
		
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphName1.getURI(), m1);
		ds.addNamedModel(graphName2.getURI(), m2);
	
		String s = "PREFIX dc:  <http://purl.org/dc/elements/1.1/>\n"
				+ "PREFIX dcmitype: <http://purl.org/dc/dcmitype/>\n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "INSERT\n"
				+ "  { GRAPH <http://example/bookStore2> { ?book ?p ?v } }\n"
				+ "WHERE\n"
				+ "  { GRAPH  <http://example/bookStore>\n"
				+ "     { ?book dc:date ?date .\n"
				+ "       FILTER ( ?date < \"2000-01-01T00:00:00-02:00\"^^xsd:dateTime )\n"
				+ "       ?book ?p ?v\n"
				+ "     }"
				+ "  } ;\n\n"
				+ "WITH <http://example/bookStore>\n"
				+ "DELETE\n"
				+ " { ?book ?p ?v }\n"
				+ "WHERE\n"
				+ " { ?book dc:date ?date ;\n"
				+ "         dc:type dcmitype:PhysicalObject .\n"
				+ "   FILTER ( ?date < \"2000-01-01T00:00:00-02:00\"^^xsd:dateTime )\n"
				+ "   ?book ?p ?v\n"
				+ " } ";
		UpdateRequest req  = UpdateFactory.create(s);
		
		System.out.println( req );
		
		UpdateAction.execute( req, ds );
		
		m1 = ds.getNamedModel( graphName1.getURI());
		assertEquals( "DELETE failed", 1, m1.listStatements().toList().size());

		assertTrue( m1.contains( book3, DC_11.title, "SPARQL 1.1 Tutorial"));

		
		m2 = ds.getNamedModel( graphName2.getURI());
		assertEquals( "Insert failed", 4, m2.listStatements().toList().size());


		assertEquals( 3, m1.listStatements( book1, null, (RDFNode)null).toList().size());
		assertTrue( m2.contains( book1, DC_11.title, "Fundamentals of Compiler Design"));
		assertTrue( m2.contains( book1, DC_11.date, d1996 ));
		assertTrue( m2.contains( book1, DC_11.date, DCTypes.PhysicalObject ));
		
		assertEquals( 1, m1.listStatements( book4, null, (RDFNode)null).toList().size());
		assertTrue( m1.contains( book4, DC_11.title, "SPARQL 1.1 Tutorial"));
		
	}
}
