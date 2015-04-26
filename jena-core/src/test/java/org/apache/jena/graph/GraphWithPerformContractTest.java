package org.apache.jena.graph;

import static org.apache.jena.testing_framework.GraphHelper.graphWith;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.apache.jena.testing_framework.GraphHelper.txnBegin;
import static org.apache.jena.testing_framework.GraphHelper.txnCommit;

import static org.junit.Assert.*;

import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.testing_framework.ContractTemplate;

import org.xenei.junit.contract.IProducer;

/**
 * GraphWithPerform is an implementation interface that extends Graph with the
 * performAdd and performDelete methods used by GraphBase to invoke
 * non-notifying versions of add and delete.
 */
@Contract(GraphWithPerform.class)
public class GraphWithPerformContractTest<T extends GraphWithPerform> extends
		ContractTemplate<IProducer<T>> {
	// Recording listener for tests
	protected RecordingGraphListener GL = new RecordingGraphListener();

	private static final Logger LOG = LoggerFactory
			.getLogger(GraphWithPerformContractTest.class);

	@SuppressWarnings("unchecked")
	public GraphWithPerformContractTest() {
//		setProducer((IProducer<T>) new IProducer<GraphWithPerform>() {
//
//			@Override
//			public GraphWithPerform newInstance() {
//				return new GraphMem();
//			}
//
//			@Override
//			public void cleanUp() {
//
//			}
//		});
	}

	@Contract.Inject
	public void setGraphWithPerformContractTestProducer(IProducer<T> producer) {
		super.setProducer(producer);
	}

	@After
	public final void afterGraphWithPerformContractTest() {
		getProducer().cleanUp();
	}

	@ContractTest
	public void testPerformAdd_Triple() {
		GraphWithPerform g = (GraphWithPerform) graphWith(getProducer()
				.newInstance(), "S P O; S2 P2 O2");
		g.getEventManager().register(GL);
		txnBegin(g);
		g.performAdd(triple("S3 P3 O3"));
		txnCommit(g);
		GL.assertEmpty();
		assertTrue(g.contains(triple("S3 P3 O3")));
	}

	@ContractTest
	public void testPerformDelete_Triple() {
		GraphWithPerform g = (GraphWithPerform) graphWith(getProducer()
				.newInstance(), "S P O; S2 P2 O2");
		g.getEventManager().register(GL);
		txnBegin(g);
		g.performDelete(triple("S2 P2 O2"));
		txnCommit(g);
		GL.assertEmpty();
		assertFalse(g.contains(triple("S2 P2 O2")));
	}

}
