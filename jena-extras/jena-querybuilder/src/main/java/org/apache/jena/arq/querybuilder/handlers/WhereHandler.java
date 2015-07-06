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
package org.apache.jena.arq.querybuilder.handlers;

import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.clauses.ConstructClause;
import org.apache.jena.arq.querybuilder.rewriters.ElementRewriter;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.lang.sparql_11.ParseException ;
import org.apache.jena.sparql.syntax.* ;
import org.apache.jena.sparql.util.ExprUtils ;

/**
 * The where handler
 *
 */
public class WhereHandler implements Handler {

	// the query to modify
	private final Query query;

	/**
	 * Constructor.
	 * @param query The query to manipulate.
	 */
	public WhereHandler(Query query) {
		this.query = query;
	}

	/**
	 * Add all where attributes from the Where Handler argument.
	 * @param whereHandler The Where Handler to copy from.
	 */
	public void addAll(WhereHandler whereHandler) {
		Element e = whereHandler.query.getQueryPattern();
		Element locE = query.getQueryPattern();
		if (e != null) {
			if (locE == null) {
				query.setQueryPattern(e);
			} else {
				ElementTriplesBlock locEtb = (ElementTriplesBlock) locE;
				ElementTriplesBlock etp = (ElementTriplesBlock) e;
				Iterator<Triple> iter = etp.patternElts();
				while (iter.hasNext()) {
					locEtb.addTriple(iter.next());
				}
			}
		}
	}

	/**
	 * Get the base element from the where clause.
	 * If the clause does not contain an element return the element group, otherwise return the 
	 * enclosed elelment.
	 * @return the base element.
	 */
	private Element getElement() {
		ElementGroup eg = getClause();
		if (eg.getElements().size() == 1) {
			return eg.getElements().get(0);
		}
		return eg;
	}

	/**
	 * Get the element group for the clause.
	 * if HTe element group is not set, create and set it.
	 * @return The element group.
	 */
	private ElementGroup getClause() {
		ElementGroup e = (ElementGroup) query.getQueryPattern();
		if (e == null) {
			e = new ElementGroup();
			query.setQueryPattern(e);
		}
		return e;
	}

	/**
	 * Test that a triple is valid.
	 * Throws an IllegalArgumentException if the triple is not valid.
	 * @param t The trip to test.
	 */
	private void testTriple(Triple t) {
		// verify Triple is valid
		boolean validSubject = t.getSubject().isURI()
				|| t.getSubject().isBlank() || t.getSubject().isVariable()
				|| t.getSubject().equals(Node.ANY);
		boolean validPredicate = t.getPredicate().isURI()
				|| t.getPredicate().isVariable()
				|| t.getPredicate().equals(Node.ANY);
		boolean validObject = t.getObject().isURI()
				|| t.getObject().isLiteral() || t.getObject().isBlank()
				|| t.getObject().isVariable() || t.getObject().equals(Node.ANY);

		if (!validSubject || !validPredicate || !validObject) {
			StringBuilder sb = new StringBuilder();
			if (!validSubject) {
				sb.append(String
						.format("Subject (%s) must be a URI, blank, variable, or a wildcard. %n",
								t.getSubject()));
			}
			if (!validPredicate) {
				sb.append(String
						.format("Predicate (%s) must be a URI , variable, or a wildcard. %n",
								t.getPredicate()));
			}
			if (!validObject) {
				sb.append(String
						.format("Object (%s) must be a URI, literal, blank, , variable, or a wildcard. %n",
								t.getObject()));
			}
			if (!validSubject || !validPredicate) {
				sb.append(String
						.format("Is a prefix missing?  Prefix must be defined before use. %n"));
			}
			throw new IllegalArgumentException(sb.toString());
		}
	}

	/**
	 * Add the triple to the where clause
	 * @param t The triple to add.
	 * @throws IllegalArgumentException If the triple is not a valid triple for a where clause.
	 */
	public void addWhere(Triple t) throws IllegalArgumentException {
		testTriple(t);
		ElementGroup eg = getClause();
		List<Element> lst = eg.getElements();
		if (lst.isEmpty()) {
			ElementTriplesBlock etb = new ElementTriplesBlock();
			etb.addTriple(t);
			eg.addElement(etb);
		} else {
			Element e = lst.get(lst.size() - 1);
			if (e instanceof ElementTriplesBlock) {
				ElementTriplesBlock etb = (ElementTriplesBlock) e;
				etb.addTriple(t);
			} else {
				ElementTriplesBlock etb = new ElementTriplesBlock();
				etb.addTriple(t);
				eg.addElement(etb);
			}

		}
	}

	/**
	 * Add an optional triple to the where clause
	 * @param t The triple to add.
	 * @throws IllegalArgumentException If the triple is not a valid triple for a where clause.
	 */
	public void addOptional(Triple t) throws IllegalArgumentException {
		testTriple(t);
		ElementTriplesBlock etb = new ElementTriplesBlock();
		etb.addTriple(t);
		ElementOptional opt = new ElementOptional(etb);
		getClause().addElement(opt);
	}

	/**
	 * Add an expression string as a filter.
	 * @param expression The expression string to add.
	 * @throws ParseException If the expression can not be parsed.
	 */
	public void addFilter(String expression) throws ParseException {
		getClause().addElement( new ElementFilter( ExprUtils.parse( query, expression, true ) ) );
	}

	/**
	 * add an expression as a filter.
	 * @param expr The expression to add.
	 */
	public void addFilter(Expr expr) {
		getClause().addElement(new ElementFilter(expr));
	}

	/**
	 * Add a subquery to the where clause.
	 * @param subQuery The sub query to add.
	 */
	public void addSubQuery(SelectBuilder subQuery) {
		getClause().addElement(makeSubQuery(subQuery));
	}

	/**
	 * Convert a subquery into a subquery element.
	 * @param subQuery The sub query to convert
	 * @return THe converted element.
	 */
	private ElementSubQuery makeSubQuery(SelectBuilder subQuery) {
		Query q = new Query();
		PrologHandler ph = new PrologHandler(query);
		ph.addAll(subQuery.getPrologHandler());

		for (Var v : subQuery.getVars()) {
			q.addResultVar(v);
			q.setQuerySelectType();
		}

		if (subQuery instanceof ConstructClause) {
			ConstructHandler ch = new ConstructHandler(q);
			ch.addAll(((ConstructClause<?>) subQuery).getConstructHandler());

		}
		DatasetHandler dh = new DatasetHandler(q);
		dh.addAll( subQuery.getDatasetHandler() );
		SolutionModifierHandler smh = new SolutionModifierHandler(q);
		smh.addAll( subQuery.getSolutionModifierHandler() );
		WhereHandler wh = new WhereHandler(q);
		wh.addAll( subQuery.getWhereHandler() );
		return new ElementSubQuery(q);

	}

	/**
	 * Add a union to the where clause.
	 * @param subQuery The subquery to add as the union.
	 */
	public void addUnion(SelectBuilder subQuery) {
		ElementUnion union=null; 
		ElementGroup clause = getClause();
		// if the last element is a union make sure we add to it.
		if ( ! clause.isEmpty() ) {
			Element lastElement =  clause.getElements().get(clause.getElements().size()-1);
			if (lastElement instanceof ElementUnion)	
			{
				union = (ElementUnion) lastElement;
			}
		}	
		if (union == null)
		{
			union = new ElementUnion();
			clause.addElement( union );
		}
		if (subQuery.getVars().size() > 0) {
			union.addElement(makeSubQuery(subQuery));
		} else {
			PrologHandler ph = new PrologHandler(query);
			ph.addAll(subQuery.getPrologHandler());
			union.addElement( subQuery.getWhereHandler().getClause() );
		}
		
	}

	/**
	 * Add a graph to the where clause.
	 * @param graph The name of the graph.
	 * @param subQuery The where handler that defines the graph.
	 */
	public void addGraph(Node graph, WhereHandler subQuery) {
		getClause().addElement(
				new ElementNamedGraph(graph, subQuery.getElement()));
	}
	
	/**
	 * Add a binding to the where clause.
	 * @param expr The expression to bind.
	 * @param var The variable to bind it to.
	 */
	public void addBind( Expr expr, Var var )
	{
		getClause().addElement(
				new ElementBind(var,expr)
				);
	}

	/**
	 * Add a binding to the where clause.
	 * @param expression The expression to bind.
	 * @param var The variable to bind it to.
	 * @throws ParseException 
	 */
	public void addBind( String expression, Var var ) throws ParseException
	{
		getClause().addElement(
				new ElementBind(var, ExprUtils.parse( query, expression, true ))
				);
	}
	
	@Override
	public void setVars(Map<Var, Node> values) {
		if (values.isEmpty()) {
			return;
		}

		Element e = query.getQueryPattern();
		if (e != null) {
			ElementRewriter r = new ElementRewriter(values);
			e.visit(r);
			query.setQueryPattern(r.getResult());
		}
	}

	@Override
	public void build() {
		// no special operations required.
	}
	
}
