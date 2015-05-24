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

package org.apache.jena.propertytable.graph;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashMap ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter1 ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.FmtUtils ;

/**
 * Split the incoming BasicPattern by subjects, (i.e. it becomes multiple sub BasicPatterns grouped by the same subjects.
 *
 */
public class QueryIterPropertyTable extends QueryIter1 {
	
	private BasicPattern pattern;
	private Graph graph;
	private QueryIterator output;

	public static QueryIterator create(QueryIterator input,
			BasicPattern pattern, ExecutionContext execContext) {
		return new QueryIterPropertyTable(input, pattern, execContext);
	}

	private QueryIterPropertyTable(QueryIterator input, BasicPattern pattern,
			ExecutionContext execContext) {
		super(input, execContext);
		this.pattern = pattern;
		graph = execContext.getActiveGraph();
		// Create a chain of triple iterators.
		QueryIterator chain = getInput();
		Collection<BasicPattern> patterns = sort(pattern);
		for (BasicPattern p : patterns)
			chain = new QueryIterPropertyTableRow(chain, p, execContext);
		output = chain;
	}

	private Collection<BasicPattern> sort(BasicPattern pattern) {
		HashMap<Node, BasicPattern> map = new HashMap<Node, BasicPattern>();
		for (Triple triple : pattern.getList()) {
			Node subject = triple.getSubject();
			if (!map.containsKey(subject)) {
				List<Triple> triples = new ArrayList<Triple>();
				BasicPattern p = BasicPattern.wrap(triples);
				map.put(subject, p);
				p.add(triple);
			} else {
				map.get(subject).add(triple);
			}
		}
		return map.values();
	}

	@Override
	protected boolean hasNextBinding() {
		return output.hasNext();
	}

	@Override
	protected Binding moveToNextBinding() {
		return output.nextBinding();
	}

	@Override
	protected void closeSubIterator() {
		if (output != null)
			output.close();
		output = null;
	}

	@Override
	protected void requestSubCancel() {
		if (output != null)
			output.cancel();
	}

	@Override
	protected void details(IndentedWriter out, SerializationContext sCxt) {
		out.print(Lib.className(this));
		out.println();
		out.incIndent();
		FmtUtils.formatPattern(out, pattern, sCxt);
		out.decIndent();
	}
}
