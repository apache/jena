/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.clauses;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.clauses.PrologClause;
import org.apache.jena.arq.querybuilder.handlers.PrologHandler;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ResourceFactory ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.After;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(PrologClause.class)
public class PrologClauseTest<T extends PrologClause<?>>  {

	// the producer we will user
	private IProducer<T> producer;

	@Contract.Inject
	// define the method to set producer.
	public final void setProducer(IProducer<T> producer) {
		this.producer = producer;
	}

	protected final IProducer<T> getProducer() {
		return producer;
	}

	@After
	public final void cleanupDatasetClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void testGetPrologHandler() {
		PrologClause<?> prologClause = getProducer().newInstance();
		PrologHandler handler = prologClause.getPrologHandler();
		assertNotNull(handler);
	}

	@ContractTest
	public void testAddPrefixResource() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.addPrefix("pfx",
				ResourceFactory.createResource("uri"));
		Query q = builder.build();
		PrefixMapping map = q.getPrefixMapping();
		assertEquals( "uri", map.getNsPrefixURI("pfx"));
		assertEquals( 1, map.getNsPrefixMap().size() );
	}

	@ContractTest
	public void testAddPrefixNode() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.addPrefix("pfx",
				NodeFactory.createURI("uri"));

		Query q = builder.build();
		PrefixMapping map = q.getPrefixMapping();
		assertEquals( "uri", map.getNsPrefixURI("pfx"));
		assertEquals( 1, map.getNsPrefixMap().size() );
	}

	@ContractTest
	public void testAddPrefixString() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.addPrefix("pfx", "uri");
		Query q = builder.build();
		PrefixMapping map = q.getPrefixMapping();
		assertEquals( "uri", map.getNsPrefixURI("pfx"));
		assertEquals( 1, map.getNsPrefixMap().size() );
	}

	@ContractTest
	public void testAddPrefixes_Map() {
		Map<String, String> map = new HashMap<>();
		map.put("pfx", "uri");
		map.put("pfx2", "uri2");
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.addPrefixes(map);
		Query q = builder.build();
		PrefixMapping map2 = q.getPrefixMapping();
		assertEquals( "uri", map2.getNsPrefixURI("pfx"));
		assertEquals( "uri2", map2.getNsPrefixURI("pfx2"));
		assertEquals( 2, map2.getNsPrefixMap().size() );
	}

	@ContractTest
	public void testAddPrefixes_PrefixMapping() {
		PrefixMapping map = new PrefixMappingImpl();
		map.setNsPrefix("pfx", "uri");
		map.setNsPrefix("pfx2", "uri2");
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.addPrefixes(map);
		Query q = builder.build();
		PrefixMapping map2 = q.getPrefixMapping();
		assertEquals( map.getNsPrefixURI("pfx"), map2.getNsPrefixURI("pfx"));
		assertEquals( map.getNsPrefixURI("pfx2"), map2.getNsPrefixURI("pfx2"));
		assertEquals( 2, map2.getNsPrefixMap().size() );
	}

	
	@ContractTest
	public void testSetBaseResource() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.setBase(ResourceFactory
				.createResource("http://example.com/uri"));

		Query q = builder.build();
		assertEquals( "http://example.com/uri", q.getResolver().getBaseIRIasString());
	}

	@ContractTest
	public void testSetBaseNode() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.setBase(NodeFactory
				.createURI("http://example.com/uri"));

		Query q = builder.build();
		assertEquals( "http://example.com/uri", q.getResolver().getBaseIRIasString());
	}

	@ContractTest
	public void testSetBaseString() {
		PrologClause<?> prologClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = prologClause.setBase("http://example.com/uri");

		Query q = builder.build();
		assertEquals( "http://example.com/uri", q.getResolver().getBaseIRIasString());
	}

	@ContractTest
	public void testSetBaseTwice() {
		PrologClause<?> prologClause = getProducer().newInstance();
		prologClause.setBase("http://example.com/uri");
		AbstractQueryBuilder<?> builder = prologClause.setBase("http://example.com/uri2");

		Query q = builder.build();
		assertEquals( "http://example.com/uri2", q.getResolver().getBaseIRIasString());
	}

}
