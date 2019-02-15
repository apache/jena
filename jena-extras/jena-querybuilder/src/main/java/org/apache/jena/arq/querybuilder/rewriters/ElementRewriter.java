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
package org.apache.jena.arq.querybuilder.rewriters;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.syntax.* ;

/**
 * A rewriter that implements an ElementVisitor
 *
 */
public class ElementRewriter extends AbstractRewriter<Element> implements
		ElementVisitor {

	/**
	 * Constructor
	 * @param values The values to rewrite with.
	 */
	public ElementRewriter(Map<Var, Node> values) {
		super(values);
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		ElementTriplesBlock newBlock = new ElementTriplesBlock();
		Iterator<Triple> tIter = el.patternElts();
		while (tIter.hasNext()) {
			newBlock.addTriple(rewrite(tIter.next()));
		}
		push(newBlock);
	}

	@Override
	public void visit(ElementPathBlock el) {
		ElementPathBlock newBlock = new ElementPathBlock();
		Iterator<TriplePath> tIter = el.patternElts();
		while (tIter.hasNext()) {
			newBlock.addTriplePath(rewrite(tIter.next()));
		}
		push(newBlock);
	}

	@Override
	public void visit(ElementFilter el) {
		ExprRewriter exprRewriter = new ExprRewriter(values);
		el.getExpr().visit(exprRewriter);
		push(new ElementFilter(exprRewriter.getResult()));
	}

	@Override
	public void visit(ElementAssign el) {
		Node n = changeNode(el.getVar());
		if (n.equals(el.getVar())) {
			ExprRewriter exprRewriter = new ExprRewriter(values);
			el.getExpr().visit(exprRewriter);
			push(new ElementAssign(el.getVar(), exprRewriter.getResult()));
		} else {
			// push( new ElementAssign( el.getVar(), NodeValue.makeNode( n )) );
			// no op
			push(new ElementTriplesBlock());
		}

	}

	@Override
	public void visit(ElementBind el) {
		Node n = changeNode(el.getVar());
		if (n.equals(el.getVar())) {
			ExprRewriter exprRewriter = new ExprRewriter(values);
			el.getExpr().visit(exprRewriter);
			push(new ElementBind(el.getVar(), exprRewriter.getResult()));
		} else {
			// push( new ElementBind( el.getVar(), NodeValue.makeNode( n )) );
			// no op
			push(new ElementTriplesBlock());
		}
	}

	@Override
	public void visit(ElementData el) {
		ElementData retval = new ElementData();
		for (Var v : el.getVars()) {
			retval.add(v);
		}
		for (Binding binding : el.getRows()) {
			retval.add( binding );
		}		
		push(retval);

	}

	@Override
	public void visit(ElementUnion el) {
		ElementUnion retval = new ElementUnion();
		for (Element e : el.getElements()) {
			e.visit(this);
			retval.addElement(getResult());
		}
		push(retval);
	}

	@Override
	public void visit(ElementOptional el) {
		el.getOptionalElement().visit(this);
		push(new ElementOptional(getResult()));
	}

	@Override
	public void visit(ElementGroup el) {
		ElementGroup retval = new ElementGroup();
		for (Element e : el.getElements()) {
			e.visit(this);
			retval.addElement(getResult());
		}
		push(retval);
	}

	@Override
	public void visit(ElementDataset el) {
		Element pattern = null;
		if (el.getElement() != null) {
			el.getElement().visit(this);
			pattern = getResult();
		}
		push(new ElementDataset(el.getDataset(), pattern));
	}

	@Override
	public void visit(ElementNamedGraph el) {
		Node n = el.getGraphNameNode();
		if (n != null) {
			n = changeNode(n);
		}
		el.getElement().visit(this);
		push(new ElementNamedGraph(n, getResult()));
	}

	@Override
	public void visit(ElementExists el) {
		el.getElement().visit(this);
		push(new ElementExists(getResult()));
	}

	@Override
	public void visit(ElementNotExists el) {
		el.getElement().visit(this);
		push(new ElementNotExists(getResult()));
	}

	@Override
	public void visit(ElementMinus el) {
		el.getMinusElement().visit(this);
		push(new ElementMinus(getResult()));
	}

	@Override
	public void visit(ElementService el) {
		el.getElement().visit(this);
		push(new ElementService(changeNode(el.getServiceNode()), getResult(),
				el.getSilent()));
	}

	@Override
	public void visit(ElementSubQuery el) {
		Query q = AbstractQueryBuilder.clone(el.getQuery());
		push(new ElementSubQuery(AbstractQueryBuilder.rewrite(
				q, values)));
	}

}