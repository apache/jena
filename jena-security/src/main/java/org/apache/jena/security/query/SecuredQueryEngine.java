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
package org.apache.jena.security.query;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;

import java.security.Principal;
import java.util.Set;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.SecNode;
import org.apache.jena.security.SecurityEvaluator.SecNode.Type;
import org.apache.jena.security.graph.SecuredGraph;
import org.apache.jena.security.query.rewriter.OpRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecuredQueryEngine extends QueryEngineMain
{
	private static Logger LOG = LoggerFactory
			.getLogger(SecuredQueryEngine.class);

	private SecurityEvaluator securityEvaluator;
	private SecNode graphIRI;

	/*
	 * public SecuredQueryEngine( Op op, DatasetGraph dataset, Binding input,
	 * Context context )
	 * {
	 * super(op, dataset, input, context);
	 * setGraphIRI( dataset );
	 * }
	 */
	public SecuredQueryEngine( final Query query, final DatasetGraph dataset,
			final Binding input, final Context context )
	{
		super(query, dataset, input, context);
		setGraphIRI(dataset);
	}

	public SecurityEvaluator getSecurityEvaluator()
	{
		return securityEvaluator;
	}

	@Override
	protected Op modifyOp( final Op op )
	{
		final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
		SecuredQueryEngine.LOG.debug("Before: {}", op);
		op.visit(rewriter);
		Op result = rewriter.getResult();
		result = result == null ? op : result;
		SecuredQueryEngine.LOG.debug("After: {}", result);
		result = super.modifyOp(result);
		SecuredQueryEngine.LOG.debug("After Optimize: {}", result);
		return result;
	}

	private void setGraphIRI( final DatasetGraph dataset )
	{
		final Graph g = dataset.getDefaultGraph();
		if (g instanceof SecuredGraph)
		{
			final SecuredGraph sg = (SecuredGraph) g;
			graphIRI = sg.getModelNode();
			this.securityEvaluator = sg.getSecurityEvaluator();
		}
		else
		{
			graphIRI = new SecNode(Type.URI, "urn:x-arq:DefaultGraph");
			this.securityEvaluator = new SecurityEvaluator() {

				@Override
				public boolean evaluate( final Action action,
						final SecNode graphIRI )
				{
					return true;
				}

				@Override
				public boolean evaluate( final Action action,
						final SecNode graphIRI, final SecTriple triple )
				{
					return true;
				}

				@Override
				public boolean evaluate( final Set<Action> action,
						final SecNode graphIRI )
				{
					return true;
				}

				@Override
				public boolean evaluate( final Set<Action> action,
						final SecNode graphIRI, final SecTriple triple )
				{
					return true;
				}

				@Override
				public boolean evaluateAny( final Set<Action> action,
						final SecNode graphIRI )
				{
					return true;
				}

				@Override
				public boolean evaluateAny( final Set<Action> action,
						final SecNode graphIRI, final SecTriple triple )
				{
					return true;
				}

				@Override
				public boolean evaluateUpdate( final SecNode graphIRI,
						final SecTriple from, final SecTriple to )
				{
					return true;
				}

				@Override
				public Principal getPrincipal()
				{
					return null;
				}
			};

		}
	}
}
