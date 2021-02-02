/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.graph;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import static org.junit.Assert.*;

import org.apache.jena.shared.JenaException;
import org.xenei.junit.contract.IProducer;
import org.apache.jena.util.CollectionFactory;
import static org.apache.jena.testing_framework.GraphHelper.*;

/**
 * AbstractTestGraph provides a bunch of basic tests for something that purports
 * to be a Graph. The abstract method getGraph must be overridden in subclasses
 * to deliver a Graph of interest.
 */
@Contract(TransactionHandler.class)
public class TransactionHandlerContractTest {

	private IProducer<TransactionHandler> producer;

	public TransactionHandlerContractTest() {
	}

	@Contract.Inject
	public void setProducer(IProducer<TransactionHandler> producer) {
		this.producer = producer;
	}

	protected IProducer<TransactionHandler> getTransactionHandlerProducer() {
		return producer;
	}

	/**
	 * Test that Graphs have transaction support methods, and that if they fail
	 * on some g they fail because they do not support the operation.
	 */
    @ContractTest
	public void testTransactionsExistAsPerTransactionSupported() {
        TransactionHandler th = getTransactionHandlerProducer().newInstance();

		if (th.transactionsSupported()) {
			th.begin();
			th.abort();
			th.begin();
			th.commit();
            th.execute( ()->{} ) ;
			th.calculate(()->null);
		} else {
			try {
				th.begin();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}

			try {
				th.abort();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}
			try {
				th.commit();
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) {
			}
			/* */
			try {
				th.execute(()->{});
				fail("Should have thrown UnsupportedOperationException");
			} catch (UnsupportedOperationException x) { }
            try {
                th.calculate(()->null);
                fail("Should have thrown UnsupportedOperationException");
            } catch (UnsupportedOperationException x) { }
		}
	}

    @ContractTest
	public void testExecuteInTransactionCatchesThrowable() {
		TransactionHandler th = getTransactionHandlerProducer().newInstance();

		if (th.transactionsSupported()) {
			try {
                th.execute(()-> { throw new Error() ; });
                fail("Should have thrown JenaException");
            } catch (JenaException x) { }
            try {
                th.calculate(()->{ throw new Error() ; });
                fail("Should have thrown JenaException");
            } catch (JenaException x) { }
		}
	}

	static final Triple[] tripleArray = tripleArray("S P O; Foo R B; X Q Y");

	static final List<Triple> tripleList = Arrays
			.asList(tripleArray("i lt j; p equals q"));

	static final Triple[] setTriples = tripleArray("scissors cut paper; paper wraps stone; stone breaks scissors");

	static final Set<Triple> tripleSet = CollectionFactory
			.createHashedSet(Arrays.asList(setTriples));

}
