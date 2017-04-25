package org.apache.jena.sparql.core.mirage;

import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
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
		
		final RayFolder rayFolder = new RayFolder(System.getProperty("user.dir") + "/src/test/resources", RayFolder.RDF_LANGUAGES_FILTER);
		rayFolder.getResolvers().add(new RayFolderResolverPrefix("urn:example:src/test/resources/", "file://" + System.getProperty("user.dir") + "/src/test/resources/"));
		datasetGraphMirage.addRay(rayFolder);
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected void read(final DatasetGraph datasetGraph, final Runnable runnable) {
		if (datasetGraph.supportsTransactions()) {
			datasetGraph.begin(ReadWrite.READ);
		}
		try {
			runnable.run();
		} finally {
			if (datasetGraph.supportsTransactions()) {
				datasetGraph.end();
			}
		}
	}
	
	@Test
	public void testTransactionalRead() {
		read(datasetGraphMirage, () -> {assertTrue(Objects.equals(datasetGraphMirage.getType(), ReadWrite.READ));});
	}
	
	@Test
	public void testListGraphNodes() {
		read(datasetGraphMirage, () -> {datasetGraphMirage.listGraphNodes().forEachRemaining(System.err::println);});
	}
	
	@Test
	public void testContainsGraph() {
		read(datasetGraphMirage, () -> {assertTrue(datasetGraphMirage.containsGraph(NodeFactory.createURI("urn:example:src/test/resources/xBooks.ttl")));});
	}

//	@Test
	public void testXBooks() {
		datasetGraphMirage.find(new Quad(NodeFactory.createURI("urn:example:src/test/resources/xBooks.ttl"), Node.ANY, Node.ANY, Node.ANY)).forEachRemaining(System.out::println);
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
