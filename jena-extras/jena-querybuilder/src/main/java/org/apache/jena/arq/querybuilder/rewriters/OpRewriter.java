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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.algebra.table.TableN ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.ExprAggregator ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;

/**
 * A rewriter that implements OpVisitor.
 *
 */
class OpRewriter extends AbstractRewriter<Op> implements OpVisitor {

	/**
	 * Constructor.
	 * @param values The values to replace.
	 */
	OpRewriter(Map<Var, Node> values) {
		super(values);
	}

	private Quad rewrite(Quad q) {
		return new Quad(changeNode(q.getGraph()), changeNode(q.getSubject()),
				changeNode(q.getPredicate()), changeNode(q.getObject()));
	}

	private QuadPattern rewrite(QuadPattern pattern) {
		QuadPattern qp = new QuadPattern();
		for (Quad q : pattern.getList()) {
			qp.add(rewrite(q));
		}
		return qp;
	}

	private List<Op> rewriteOpList(List<Op> lst) {
		List<Op> retval = new ArrayList<>();
		for (Op o : lst) {
			o.visit(this);
			retval.add(pop());
		}
		return retval;
	}

	private BasicPattern rewrite(BasicPattern pattern) {
		return BasicPattern.wrap(rewrite(pattern.getList()));
	}

	@Override
	public void visit(OpBGP opBGP) {
		push(new OpBGP(rewrite(opBGP.getPattern())));
	}

	@Override
	public void visit(OpQuadPattern quadPattern) {
		push(new OpQuadPattern(changeNode(quadPattern.getGraphNode()),
				rewrite(quadPattern.getBasicPattern())));
	}

	@Override
	public void visit(OpQuadBlock quadBlock) {
		push(new OpQuadBlock(rewrite(quadBlock.getPattern())));
	}

	@Override
	public void visit(OpTriple opTriple) {
		push(new OpTriple(rewrite(opTriple.getTriple())));
	}

	@Override
	public void visit(OpQuad opQuad) {
		push(new OpQuad(rewrite(opQuad.getQuad())));

	}

	@Override
	public void visit(OpPath opPath) {
		push(new OpPath(rewrite(opPath.getTriplePath())));
	}

    @Override
    public void visit(OpFind opFind) {
        Var var = opFind.getVar();
        Triple triple = opFind.getTriple();
        
        Node n2 = changeNode(var);
        if ( ! Var.isVar(n2) )
            throw new JenaException("OpFind: Write if not a variable");
        
        Var var2 = Var.alloc(n2); 
        Triple triple2 = rewrite(triple); 
        push(new OpFind(triple2, var2));
    }

    @Override
	public void visit(OpTable opTable) {
		Table tbl = opTable.getTable();
		boolean process = false;
		for (Var v : tbl.getVars()) {
			process = process | values.keySet().contains(v);
		}
		if (!process) {
			push(opTable);
		} else {
			TableN retTbl = new TableN(tbl.getVars());
			Iterator<Binding> iter = tbl.rows();
			while (iter.hasNext()) {
				retTbl.addBinding(rewrite(iter.next()));
			}
			push(OpTable.create(retTbl));
		}
	}

	@Override
	public void visit(OpNull opNull) {
		push(opNull);
	}

	@Override
	public void visit(OpProcedure opProc) {
		opProc.getSubOp().visit(this);
		Op op = pop();
		ExprList args = new ExprRewriter(values).rewrite(opProc.getArgs());
		Node procId = changeNode(opProc.getProcId());
		push(new OpProcedure(procId, args, op));
	}

	private PropFuncArg rewrite(PropFuncArg arg) {
		if (arg.isList()) {
			List<Node> lst = changeNodes(arg.getArgList());
			return new PropFuncArg(lst, null);
		}
		return new PropFuncArg(changeNode(arg.getArg()));
	}

	@Override
	public void visit(OpPropFunc opPropFunc) {
		opPropFunc.getSubOp().visit(this);
		Op op = pop();
		Node uri = changeNode(opPropFunc.getProperty());
		PropFuncArg args1 = rewrite(opPropFunc.getSubjectArgs());
		PropFuncArg args2 = rewrite(opPropFunc.getObjectArgs());
		push(new OpPropFunc(uri, args1, args2, op));
	}

	@Override
	public void visit(OpFilter opFilter) {
		opFilter.getSubOp().visit(this);
		push(OpFilter.filterBy(
				new ExprRewriter(values).rewrite(opFilter.getExprs()), pop()));
	}

	@Override
	public void visit(OpGraph opGraph) {
		opGraph.getSubOp().visit(this);
		push(new OpGraph(changeNode(opGraph.getNode()), pop()));
	}

	@Override
	public void visit(OpService opService) {
		opService.getSubOp().visit(this);
		push(new OpService(changeNode(opService.getService()), pop(),
				opService.getSilent()));
	}

	@Override
	public void visit(OpDatasetNames dsNames) {
		push(new OpDatasetNames(changeNode(dsNames.getGraphNode())));
	}

	@Override
	public void visit(OpLabel opLabel) {
		if (opLabel.hasSubOp()) {
			opLabel.getSubOp().visit(this);
			push(OpLabel.create(opLabel.getObject(), pop()));
		} else {
			push(opLabel);
		}
	}

	@Override
	public void visit(OpAssign opAssign) {
		opAssign.getSubOp().visit(this);
		push(OpAssign.assign(pop(), rewrite(opAssign.getVarExprList())));
	}

	@Override
	public void visit(OpExtend opExtend) {
		opExtend.getSubOp().visit(this);
		push(OpExtend.extend(pop(), rewrite(opExtend.getVarExprList())));
	}

	@Override
	public void visit(OpJoin opJoin) {
		opJoin.getRight().visit(this);
		opJoin.getLeft().visit(this);
		push(OpJoin.create(pop(), pop()));
	}

	@Override
	public void visit(OpLeftJoin opLeftJoin) {
		opLeftJoin.getRight().visit(this);
		opLeftJoin.getLeft().visit(this);
		push(OpLeftJoin.create(pop(), pop(),
				new ExprRewriter(values).rewrite(opLeftJoin.getExprs())));
	}

	@Override
	public void visit(OpUnion opUnion) {
		opUnion.getRight().visit(this);
		opUnion.getLeft().visit(this);
		push(OpUnion.create(pop(), pop()));
	}

	@Override
	public void visit(OpDiff opDiff) {
		opDiff.getRight().visit(this);
		opDiff.getLeft().visit(this);
		push(OpDiff.create(pop(), pop()));
	}

	@Override
	public void visit(OpMinus opMinus) {
		opMinus.getRight().visit(this);
		opMinus.getLeft().visit(this);
		push(OpMinus.create(pop(), pop()));
	}

	@Override
	public void visit(OpConditional opCondition) {
		opCondition.getRight().visit(this);
		opCondition.getLeft().visit(this);
		push(new OpConditional(pop(), pop()));
	}

	@Override
	public void visit(OpSequence opSequence) {
		List<Op> lst = rewriteOpList(opSequence.getElements());
		push(opSequence.copy(lst));
	}

	@Override
	public void visit(OpDisjunction opDisjunction) {
		List<Op> lst = rewriteOpList(opDisjunction.getElements());
		push(opDisjunction.copy(lst));
	}

	@Override
	public void visit(OpExt opExt) {
		push(opExt);
	}

	@Override
	public void visit(OpList opList) {
		opList.getSubOp().visit(this);
		push(new OpList(pop()));
	}

	@Override
	public void visit(OpOrder opOrder) {
		List<SortCondition> lst = new ExprRewriter(values)
				.rewriteSortConditionList(opOrder.getConditions());
		opOrder.getSubOp().visit(this);
		push(new OpOrder(pop(), lst));
	}

	@Override
	public void visit(OpProject opProject) {
		opProject.getSubOp().visit(this);
		List<Var> vars = new ArrayList<>();
		for (Var v : opProject.getVars()) {
			Node n = changeNode(v);
			vars.add(Var.alloc(n));
		}
		push(new OpProject(pop(), vars));
	}

	@Override
	public void visit(OpReduced opReduced) {
		opReduced.getSubOp().visit(this);
		push(opReduced.copy(pop()));
	}

	@Override
	public void visit(OpDistinct opDistinct) {
		opDistinct.getSubOp().visit(this);
		push(opDistinct.copy(pop()));
	}

	@Override
	public void visit(OpSlice opSlice) {
		opSlice.getSubOp().visit(this);
		push(opSlice.copy(pop()));
	}

	@Override
	public void visit(OpGroup opGroup) {
		opGroup.getSubOp().visit(this);
		ExprRewriter expRewriter = new ExprRewriter(values);
		VarExprList groupVars = rewrite(opGroup.getGroupVars());
		List<ExprAggregator> aggregators = new ArrayList<>();
		for (ExprAggregator ea : opGroup.getAggregators()) {
			ea.visit(expRewriter);
			aggregators.add((ExprAggregator) expRewriter.pop());
		}
		push(new OpGroup(pop(), groupVars, aggregators));
	}

	@Override
	public void visit(OpTopN opTop) {
		opTop.getSubOp().visit(this);
		ExprRewriter expRewriter = new ExprRewriter(values);
		expRewriter.rewriteSortConditionList(opTop.getConditions());
		push(new OpTopN(pop(), opTop.getLimit(),
				expRewriter.rewriteSortConditionList(opTop.getConditions())));
	}
}