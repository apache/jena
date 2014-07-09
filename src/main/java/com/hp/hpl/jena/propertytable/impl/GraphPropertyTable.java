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

package com.hp.hpl.jena.propertytable.impl;

import java.util.ArrayList;
import java.util.Locale;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.propertytable.Column;
import com.hp.hpl.jena.propertytable.PropertyTable;
import com.hp.hpl.jena.propertytable.Row;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class GraphPropertyTable extends GraphBase {

	private PropertyTable pt;

	public GraphPropertyTable(PropertyTable pt) {
		this.pt = pt;
	}

	public PropertyTable getPropertyTable() {
		return pt;
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
		//System.out.println(m);

		if (this.pt == null) {
			return NullIterator.instance();
		}

		ExtendedIterator<Triple> iter = null;

		Node s = m.getMatchSubject();
		Node p = m.getMatchPredicate();
		Node o = m.getMatchObject();

		if (isConcrete(p) && isConcrete(o)) {
			//System.out.println("1");
			iter = pt.getTripleIterator(pt.getColumn(p), o);
		} else if (isConcrete(p)) {
			//System.out.println("2");
			Column column = this.pt.getColumn(p);
			if (column != null) {
				iter = pt.getTripleIterator(column);
			} else {
				return NullIterator.instance();
			}
		} else if (isConcrete(o)) {
			//System.out.println("3");
			iter = pt.getTripleIterator(o);
		} else{
			//System.out.println("4");
			iter = pt.getTripleIterator();
		}

		return iter.filterKeep(new TripleMatchFilterEquality(m.asTriple()));

	}
	
	protected ExtendedIterator<Row> propertyTableBaseFind(RowMatch m) {
		
		if (this.pt == null) {
			return NullIterator.instance();
		}
		
		ExtendedIterator<Row> iter = null;

		Node s = m.getMatchSubject();

		if ( isConcrete(s) ){
			Row row= pt.getRow(s);
			if (row == null){
				return NullIterator.instance();
			} else {
				ArrayList<Row> rows = new ArrayList<Row>();
				rows.add(row);
				return WrappedIterator.create(rows.iterator());
			}
		} else {
			iter = pt.getRowIterator();
		}
		
		return iter.filterKeep(new RowMatchFilterEquality( m ));
		
	}
	
	static class RowMatchFilterEquality extends Filter<Row> {
		final protected RowMatch rMatch;

		public RowMatchFilterEquality(RowMatch rMatch) {
			this.rMatch = rMatch;
		}

		@Override
		public boolean accept(Row r) {
			return rowContained(rMatch, r);
		}

	}
	
	static boolean rowContained(RowMatch rMatch, Row row) {
			
		boolean contained = equalNode(rMatch.getSubject(), row.getRowKey());
		if(contained){
			BasicPattern pattern =rMatch.getBasicPattern();
			for(Triple triple: pattern ){
				contained = equalNode(triple.getObject(), row.getValue( triple.getPredicate()) );
				if (! contained){
					break;
				}
			}
		} 
		return contained;
	}
	

	static class TripleMatchFilterEquality extends Filter<Triple> {
		final protected Triple tMatch;

		/** Creates new TripleMatchFilter */
		public TripleMatchFilterEquality(Triple tMatch) {
			this.tMatch = tMatch;
		}

		@Override
		public boolean accept(Triple t) {
			return tripleContained(tMatch, t);
		}

	}

	static boolean tripleContained(Triple patternTriple, Triple dataTriple) {
		return equalNode(patternTriple.getSubject(), dataTriple.getSubject())
				&& equalNode(patternTriple.getPredicate(),
						dataTriple.getPredicate())
				&& equalNode(patternTriple.getObject(), dataTriple.getObject());
	}

	private static boolean equalNode(Node m, Node n) {
		// m should not be null unless .getMatchXXXX used to get the node.
		// Language tag canonicalization
		n = fixupNode(n);
		m = fixupNode(m);
		return (m == null) || (m == Node.ANY) || m.equals(n);
	}

	private static Node fixupNode(Node node) {
		if (node == null || node == Node.ANY)
			return node;

		// RDF says ... language tags should be canonicalized to lower case.
		if (node.isLiteral()) {
			String lang = node.getLiteralLanguage();
			if (lang != null && !lang.equals(""))
				node = NodeFactory.createLiteral(node.getLiteralLexicalForm(),
						lang.toLowerCase(Locale.ROOT),
						node.getLiteralDatatype());
		}
		return node;
	}

	private boolean isConcrete(Node node) {
		boolean wild = (node == null || node == Node.ANY);
		return !wild;
	}

}
