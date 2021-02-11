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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.Element ;

/**
 * A rewriter that implements an ExprVisitor
 *
 */
public class ExprRewriter extends AbstractRewriter<Expr> implements ExprVisitor {

	/**
	 * Constructor.
	 * @param values the values to replace.
	 */
	public ExprRewriter(Map<Var, Node> values) {
		super(values);
	}

	@Override
	public void visit(ExprFunction0 func) {
		push(func);
	}

	@Override
	public void visit(ExprFunction1 func) {
		func.getArg().visit(this);
		push(func.copy(pop()));

	}

	@Override
	public void visit(ExprFunction2 func) {
		// reverse order so they pop in the right order
		func.getArg2().visit(this);
		func.getArg1().visit(this);
		push(func.copy(pop(), pop()));
	}

	@Override
	public void visit(ExprFunction3 func) {
		func.getArg3().visit(this);
		func.getArg2().visit(this);
		func.getArg1().visit(this);
		push(func.copy(pop(), pop(), pop()));
	}

	@Override
	public void visit(ExprFunctionN func) {
		ExprList exprList = rewrite(new ExprList(func.getArgs()));
		ExprFunctionN retval = (ExprFunctionN) func.deepCopy();
		setExprList(retval, exprList);
		push(retval);
	}

	private static void setExprList(ExprFunctionN n, ExprList exprList) {
		try {
			Field f = ExprFunctionN.class.getDeclaredField("args");
			f.setAccessible(true);
			f.set(n, exprList);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void visit(ExprFunctionOp funcOp) {
		ElementRewriter elementRewriter = new ElementRewriter(values);
		funcOp.getElement().visit(elementRewriter);
		OpRewriter opRewriter = new OpRewriter(values);
		funcOp.getGraphPattern().visit(opRewriter);

		try {
			Constructor<? extends ExprFunctionOp> con = funcOp.getClass()
					.getConstructor(Element.class, Op.class);
			push(con.newInstance(elementRewriter.pop(), opRewriter.pop()));
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public void visit(NodeValue nv) {
		NodeValueRewriter rewriter = new NodeValueRewriter(values);
		nv.visit(rewriter);
		push(rewriter.pop());
	}

    @Override
    public void visit(ExprNone none) {
        // This should not occur.
        throw new InternalErrorException("Visit Expr.NONE");
    }

    @Override
	public void visit(ExprVar nv) {
		Node n = changeNode(nv.asVar());
		if (n.isVariable()) {
			push(new ExprVar(n));
		} else {
			push(NodeValue.makeNode(n));
		}
	}

	@Override
    public void visit(ExprTripleTerm tripleTerm) {
	    Triple t1 = tripleTerm.getTriple();
	    Triple t2 = rewrite(t1);
	    Node ntt = NodeFactory.createTripleNode(t2);
	    push(ExprLib.nodeToExpr(ntt));
	}

    @Override
	public void visit(ExprAggregator eAgg) {
		Node n = changeNode(eAgg.getVar());
		if (n.equals(eAgg.getVar())) {
			push(eAgg);
		} else {
			push(NodeValue.makeNode(n));
		}

	}

	public final List<SortCondition> rewriteSortConditionList(
			List<SortCondition> lst) {
		if (lst == null) {
			return null;
		}
		List<SortCondition> retval = new ArrayList<>();
		for (SortCondition sc : lst) {
			retval.add(rewrite(sc));
		}
		return retval;
	}

	public final SortCondition rewrite(SortCondition sortCondition) {
		sortCondition.getExpression().visit(this);
		return new SortCondition(pop(), sortCondition.getDirection());
	}

	public final ExprList rewrite(ExprList lst) {
		if (lst == null) {
			return null;
		}
		ExprList exprList = new ExprList();
		int limit = lst.size();
		for (int i = 0; i < limit; i++) {
			lst.get(i).visit(this);
			exprList.add(pop());
		}
		return exprList;
	}
}