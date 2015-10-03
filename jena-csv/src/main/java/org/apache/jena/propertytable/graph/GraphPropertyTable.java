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
import java.util.Locale ;
import java.util.function.Predicate;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.propertytable.Column ;
import org.apache.jena.propertytable.PropertyTable ;
import org.apache.jena.propertytable.Row ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 * GraphPropertyTable implements the Graph interface (read-only) over a PropertyTable.
 * This is subclass from GraphBase and implements find().
 * The graphBaseFind()(for matching a Triple) and propertyTableBaseFind()(for matching a whole Row) methods can choose the access route based on the find arguments.
 * GraphPropertyTable holds/wraps an reference of the PropertyTable instance, so that such a Graph can be treated in a more table-like fashion.
 *
 */
public class GraphPropertyTable extends GraphBase {

	private PropertyTable pt;

	public GraphPropertyTable(PropertyTable pt) {
		this.pt = pt;
	}

	public PropertyTable getPropertyTable() {
		return pt;
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple triple) {
		//System.out.println(m);

		if (this.pt == null) {
			return NullIterator.instance();
		}

		ExtendedIterator<Triple> iter = null;

		Node s = triple.getMatchSubject();
		Node p = triple.getMatchPredicate();
		Node o = triple.getMatchObject();

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

		return iter.filterKeep(new TripleMatchFilterEquality(triple));

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
			iter = WrappedIterator.create(pt.getAllRows().iterator());
		}
		
		return iter.filterKeep(new RowMatchFilterEquality( m ));
		
	}
	
	static class RowMatchFilterEquality implements Predicate<Row> {
		final protected RowMatch rMatch;

		public RowMatchFilterEquality(RowMatch rMatch) {
			this.rMatch = rMatch;
		}

		@Override
		public boolean test(Row r) {
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
	

	static class TripleMatchFilterEquality implements Predicate<Triple> {
		final protected Triple tMatch;

		/** Creates new TripleMatchFilter */
		public TripleMatchFilterEquality(Triple tMatch) {
			this.tMatch = tMatch;
		}

		@Override
		public boolean test(Triple t) {
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
