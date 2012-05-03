/**
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

package com.hp.hpl.jena.sparql.engine.http;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.util.Context;


public class TestService {
    

	@Test
	public void testNumericTimeout()
	{
		BasicPattern basicPattern = new BasicPattern();
		basicPattern.add( Triple.ANY );
		Node serviceNode = Node.createURI("http://example.com:40000");
		OpService opService = new OpService( serviceNode, new OpBGP( basicPattern ), false);

		Context context = new Context();
		ARQ.setNormalMode(context);

		context.set(Service.queryTimeout, 10 );	

		try {
			Service.exec(opService, context);
			Assert.fail( "Expected QueryExceptionHTTP");
		}
		catch (QueryExceptionHTTP expected)
		{
			if (expected.getCause() instanceof java.net.SocketTimeoutException)
			{
				// expected
			}
			else
			{
				Assert.fail( "Expected SocketTimeoutException" );
			}
		}

	}

	@Test
	public void testStringTimeout()
	{
		BasicPattern basicPattern = new BasicPattern();
		basicPattern.add( Triple.ANY );
		Node serviceNode = Node.createURI("http://example.com:40000");
		OpService opService = new OpService( serviceNode, new OpBGP( basicPattern ), false);

		Context context = new Context();
		ARQ.setNormalMode(context);

		context.set(Service.queryTimeout, "10" );	

		try {
			Service.exec(opService, context);
			Assert.fail( "Expected QueryExceptionHTTP");
		}
		catch (QueryExceptionHTTP expected)
		{
			if (expected.getCause() instanceof java.net.SocketTimeoutException)
			{
				// expected
			}
			else
			{
				Assert.fail( "Expected SocketTimeoutException" );
			}
		}	
	}

	@Test
	public void testStringTimeout2()
	{
		BasicPattern basicPattern = new BasicPattern();
		basicPattern.add( Triple.ANY );
		Node serviceNode = Node.createURI("http://example.com:40000");
		OpService opService = new OpService( serviceNode, new OpBGP( basicPattern ), false);

		Context context = new Context();
		ARQ.setNormalMode(context);

		context.set(Service.queryTimeout, "10,10000" );	

		try {
			Service.exec(opService, context);
			Assert.fail( "Expected QueryExceptionHTTP");
		}
		catch (QueryExceptionHTTP expected)
		{
			if (expected.getCause() instanceof java.net.SocketTimeoutException)
			{
				// expected
			}
			else
			{
				Assert.fail( "Expected SocketTimeoutException" );
			}
		}	
	}
}

