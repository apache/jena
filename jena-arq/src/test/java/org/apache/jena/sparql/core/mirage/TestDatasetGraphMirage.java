package org.apache.jena.sparql.core.mirage;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatasetGraphMirage {

	static final Node EX = NodeFactory.createURI("urn:ex:");

	static final Node X = NodeFactory.createURI("urn:ex:x");
	
	static DatasetGraphMirage datasetGraphMirage;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		datasetGraphMirage = new DatasetGraphMirage(new Context());

//		datasetGraphMirage.addRay(new RayTime());
		
		final RayFolder rayFolder = new RayFolder(System.getProperty("user.dir") + "/src/test/resources", RayFolder.RDF_LANGUAGES);
		rayFolder.getResolvers().add(new RayFolderResolverRoot("urn:example:src/test/resources/", "file://" + System.getProperty("user.dir") + "/src/test/resources/"));
		datasetGraphMirage.addRay(rayFolder);
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
//	@Test
	public void testListGraphNodes() {
		datasetGraphMirage.listGraphNodes().forEachRemaining(System.err::println);
	}
	
	@Test
	public void testContainsGraph() {
		assertTrue(datasetGraphMirage.containsGraph(NodeFactory.createURI("urn:example:src/test/resources/xBooks.ttl")));
	}

//	@Test
	public void testXBooks() {
		datasetGraphMirage.find(new Quad(NodeFactory.createURI("xBooks.ttl"), Node.ANY, Node.ANY, Node.ANY)).forEachRemaining(System.out::println);
	}
	
//	@Test
	public void testFind() {
		datasetGraphMirage.find().forEachRemaining(System.out::println);
	}

//	@Test
	public void testFindQuad() {
		datasetGraphMirage.find(DatasetGraphMirage.QUAD_ANY).forEachRemaining(System.out::println);
		datasetGraphMirage.find(new Quad(Node.ANY, Node.ANY, RayTime.NOW, Node.ANY)).forEachRemaining(System.out::println);
		datasetGraphMirage.find(new Quad(EX, X, RayTime.NOW, Node.ANY)).forEachRemaining(System.out::println);
	}
}
