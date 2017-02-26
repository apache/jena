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

import java.util.Arrays;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.sparql.core.Var;
import org.junit.After;
import org.junit.Before;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(ValuesClause.class)
public class ValuesClauseTest<T extends ValuesClause<?>> extends AbstractClauseTest {

	// the producer we will user
	private IProducer<T> producer;
	
	private T valuesClause;

	@Contract.Inject
	// define the method to set producer.
	public final void setProducer(IProducer<T> producer) {
		this.producer = producer;
	}

	protected final IProducer<T> getProducer() {
		return producer;
	}
	
	@Before
	public final void setupValuesClauseTest()
	{
		valuesClause = producer.newInstance();
	}

	@After
	public final void cleanupValuesClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void testSetOneVar() {
		
		AbstractQueryBuilder<?> builder = valuesClause.addValueVar( "?x");
		builder.addDataBlock( Arrays.asList( "foo"));
		assertContainsRegex(VALUES + var("x")+OPT_SPACE+OPEN_CURLY + quote("foo")
				+ OPT_SPACE + CLOSE_CURLY, builder.buildString());
	}
	
	@ContractTest
	public void testSetOneVarTwoValues() {
		
		AbstractQueryBuilder<?> builder = valuesClause.addValueVar( "?x");
		builder.addDataBlock( Arrays.asList( "foo"));
		builder.addDataBlock( Arrays.asList( "bar"));		
		assertContainsRegex(VALUES + var("x")+OPT_SPACE+OPEN_CURLY + quote("foo") + SPACE +
				quote("bar")
				+ OPT_SPACE + CLOSE_CURLY, builder.buildString());
	}
	
	@ContractTest
	public void testSetTwoVarTwoValues() {
		
		AbstractQueryBuilder<?> builder = valuesClause.addValueVar( "?x");
		builder.addValueVar( "?y");
		builder.addDataBlock( Arrays.asList( "foo", "fu"));
		builder.addDataBlock( Arrays.asList( "bar", "bear"));
		
		String s = builder.buildString();
		
		assertContainsRegex(VALUES + OPEN_PAREN+ var("x")+ SPACE + var("y")+ CLOSE_PAREN + OPT_SPACE+OPEN_CURLY +
				OPEN_PAREN + quote("foo") + SPACE + quote("fu") +CLOSE_PAREN + OPT_SPACE +
				OPEN_PAREN + quote("bar") + SPACE + quote("bear") +CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
	}
}
