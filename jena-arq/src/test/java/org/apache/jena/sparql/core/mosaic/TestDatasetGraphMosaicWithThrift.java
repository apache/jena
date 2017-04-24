package org.apache.jena.sparql.core.mosaic;

import static com.jayway.awaitility.Awaitility.await ;

import static org.junit.Assert.*;

import java.nio.file.AtomicMoveNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.thrift.DatasetGraphThriftClient;
import org.apache.jena.sparql.core.thrift.DatasetGraphThriftFactory;
import org.apache.jena.sparql.core.thrift.DatasetGraphThriftIFace;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.system.Txn;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatasetGraphMosaicWithThrift {

	static Node gXBooks = NodeFactory.createURI("urn:example:xBooks");
	
	static Node gYBooks = NodeFactory.createURI("urn:example:yBooks");
	
	static Node gZBooks = NodeFactory.createURI("urn:example:zBooks");

	static DatasetGraph datasetGraphX;

	static DatasetGraph datasetGraphY;

	static DatasetGraph datasetGraphZ;

	static DatasetGraphThriftIFace xIFace;

	static DatasetGraphThriftIFace yIFace;

	static DatasetGraphThriftIFace zIFace;

	static DatasetGraphThriftClient xClient;

	static DatasetGraphThriftClient yClient;

	static DatasetGraphThriftClient zClient;
	
	static DatasetGraphMosaic datasetGraphMosaic;

	static String QUERY_AUTHOR = "select * where {graph ?g {?s <http://purl.org/dc/elements/1.1/author> ?o}}";

	static DatasetGraphThriftIFace iFace(final DatasetGraph datasetGraph, final int port) {
		final Context context = new Context();
		context.set(DatasetGraphThriftFactory.THRIFT_SERVER_DATASET_GRAPH, datasetGraph);
		context.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME, "localhost");
		context.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT, port);
		return new DatasetGraphThriftIFace(context);
	}

	static DatasetGraphThriftClient client(final int port) {
		final Context context = new Context();
		context.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME, "localhost");
		context.set(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT, port);
		return new DatasetGraphThriftClient(context);
	}
	
	static final AtomicInteger tests = new AtomicInteger(3);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JenaSystem.init();
		
		datasetGraphX = DatasetGraphFactory.createTxnMem();
		Txn.executeWrite(datasetGraphX, () -> {RDFDataMgr.read(datasetGraphX.getGraph(gXBooks), "xBooks.ttl", Lang.TURTLE);});
		xIFace = iFace(datasetGraphX, 1972);
		xClient = client(1972);

		datasetGraphY = DatasetGraphFactory.createTxnMem();
		Txn.executeWrite(datasetGraphY, () -> {RDFDataMgr.read(datasetGraphY.getGraph(gYBooks), "yBooks.ttl", Lang.TURTLE);});
		yIFace = iFace(datasetGraphY, 1973);
		yClient = client(1973);

		datasetGraphZ = DatasetGraphFactory.createTxnMem();
		zIFace = iFace(datasetGraphZ, 1974);
		zClient = client(1974);

		datasetGraphMosaic = new DatasetGraphMosaic(new Context());
		datasetGraphMosaic.add(xClient);
		datasetGraphMosaic.add(yClient);
		datasetGraphMosaic.add(zClient);

		System.out.println(datasetGraphMosaic);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		await().untilAtomic(tests, CoreMatchers.equalTo(0));
		
		System.out.println("\n" + datasetGraphMosaic);

		datasetGraphMosaic.close();
	}
	
//	@Test
	public void testMosaicQuery() {
		Txn.executeRead(datasetGraphMosaic, () -> {;
			final AtomicInteger c = new AtomicInteger(); 
			SPARQLSelect(DatasetFactory.wrap(datasetGraphMosaic), QUERY_AUTHOR).forEachRemaining((q) -> {
				System.out.println(q);
				c.incrementAndGet();
			});
//			assertTrue(c.get() == 6);
		});
	}

	@Test
	public void testMosaicFind() {
		Txn.executeRead(datasetGraphMosaic, () -> {
			final AtomicInteger c = new AtomicInteger();
			datasetGraphMosaic.find().forEachRemaining((q) -> {
				System.out.println(q);
				c.incrementAndGet();
			});
			assertTrue(c.get() == 21);
		});
		tests.decrementAndGet();
	}

	@Test
	public void testMosaicRead() {
		Txn.executeRead(datasetGraphMosaic, () -> {
			assertTrue(datasetGraphMosaic.isInTransaction());
		});
		tests.decrementAndGet();
	}

	@Test
	public void testMosaicWrite() {
		Txn.executeWrite(datasetGraphMosaic, () -> {
			assertTrue(datasetGraphMosaic.isInTransaction());
		});
		tests.decrementAndGet();
	}

	enum Status {Write, View, Commit};

//	@Test
	public void testMosaicMW() {
		final AtomicReference<Status> w1 = new AtomicReference<>();

		final AtomicReference<Status> w2 = new AtomicReference<>();

		new Thread(() -> {
			Txn.executeWrite(datasetGraphMosaic, () -> {
				assertTrue(datasetGraphMosaic.isInTransaction());
				w1.set(Status.Write);
				await().untilAtomic(w1, CoreMatchers.equalTo(Status.View));
			});
			assertTrue(!datasetGraphMosaic.isInTransaction());
			w1.set(Status.Commit);
		}).start();

		new Thread(() -> {
			Txn.executeWrite(datasetGraphMosaic, () -> {
				assertTrue(datasetGraphMosaic.isInTransaction());
				w2.set(Status.Write);
				await().untilAtomic(w2, CoreMatchers.equalTo(Status.View));
			});
			assertTrue(!datasetGraphMosaic.isInTransaction());
			w2.set(Status.Commit);
		}).start();
		
		await().untilAtomic(w1, CoreMatchers.equalTo(Status.Write));
		w1.set(Status.View);

		await().untilAtomic(w2, CoreMatchers.equalTo(Status.Write));
		w2.set(Status.View);

		await().untilAtomic(w1, CoreMatchers.equalTo(Status.Commit));
		await().untilAtomic(w2, CoreMatchers.equalTo(Status.Commit));

		assertTrue(!datasetGraphMosaic.isInTransaction());
	}
	
	protected ResultSet SPARQLSelect(final Dataset dataset, final String q) {
		final Query query = QueryFactory.create(q);
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
		return queryExecution.execSelect();
	}

}
