package org.apache.jena.sparql.core.thrift;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.SKOS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatasetGraphThrift {

	static Node gXBooks = NodeFactory.createURI("urn:example:xBooks");
	
	static DatasetGraph datasetGraphX;

	static DatasetGraphThriftIFace iFace;
	
	static DatasetGraphThriftClient client;

	static String QUERY_AUTHOR = "select * where {graph ?g {?s <http://purl.org/dc/elements/1.1/author> ?o}}";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JenaSystem.init();
		
		datasetGraphX = DatasetGraphFactory.createTxnMem();
		Txn.executeWrite(datasetGraphX, () -> {RDFDataMgr.read(datasetGraphX.getGraph(gXBooks), "xBooks.ttl", Lang.TURTLE);});
		
		final Context iFaceContext = new Context();
		iFaceContext.set(DatasetGraphThriftFactory.THRIFT_SERVER_DATASET_GRAPH, datasetGraphX);
		iFaceContext.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME, "localhost");
		iFaceContext.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT, 1972);
		
		iFace = new DatasetGraphThriftIFace(iFaceContext);
		
		final Context clientContext = new Context();
		clientContext.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME, "localhost");
		clientContext.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT, 1972);
		
		client = new DatasetGraphThriftClient(clientContext);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		client.close();
		
		iFace.close(null);
	}

	@Test
	public void testListGrapNodes() {
		Txn.executeRead(client, () -> {
			final AtomicInteger c = new AtomicInteger();
			final Iterator<Node> i = client.listGraphNodes();
			while (i.hasNext()) {
				System.out.println(i.next());
				c.incrementAndGet();
			}
			assertTrue(c.get() == 1);
		});
	}

	@Test
	public void testFind() {
		Txn.executeRead(client, () -> {
			final AtomicInteger c = new AtomicInteger();
			client.find().forEachRemaining((q) -> {
				System.out.println(q);
				c.incrementAndGet();
			});
			assertTrue(c.get() == 9);
		});
	}

	@Test
	public void testFindQ() {
		Txn.executeRead(client, () -> {
			final AtomicInteger c = new AtomicInteger();
			client.find(new Quad(gXBooks, Node.ANY, SKOS.narrower.asNode(), Node.ANY)).forEachRemaining((q) -> {
				System.out.println(q);
				c.incrementAndGet();
			});
			assertTrue(c.get() == 1);
		});
	}

	@Test
	public void testClientQuery() {
		Txn.executeRead(client, () -> {;
			final AtomicInteger c = new AtomicInteger(); 
			SPARQLSelect(DatasetFactory.wrap(client), QUERY_AUTHOR).forEachRemaining((q) -> {
				c.incrementAndGet();
				System.out.println(q);
			});
			assertTrue(c.get() == 2);
		});
	}

	
	protected ResultSet SPARQLSelect(final Dataset dataset, final String q) {
		final Query query = QueryFactory.create(q);
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
		return queryExecution.execSelect();
	}

}
