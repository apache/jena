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

package org.apache.jena.sparql.engine.ref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.table.TableEmpty;
import org.apache.jena.sparql.algebra.table.TableUnit;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterDistinguishedVars;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.QC;

// Spit out a few of the longer ops.
public class RefEval {
    public static Table eval(Evaluator evaluator, Op op) {
        EvaluatorDispatch ev = new EvaluatorDispatch(evaluator);
        op.visit(ev);
        Table table = ev.getResult();
        return table;
    }

    static Table evalDS(OpDatasetNames opDSN, Evaluator evaluator) {
        Node graphNode = opDSN.getGraphNode();
        if ( graphNode.isURI() ) {
            if ( evaluator.getExecContext().getDataset().containsGraph(graphNode) ) {
                return new TableUnit();
            } else
            // WRONG
            {
                return new TableEmpty();
            }
        }

        if ( !Var.isVar(graphNode) )
            throw new ARQInternalErrorException("OpDatasetNames: Not a URI or variable: " + graphNode);

        DatasetGraph dsg = evaluator.getExecContext().getDataset();
        Iterator<Node> iter = dsg.listGraphNodes();
        List<Binding> list = new ArrayList<>();
        for ( ; iter.hasNext() ; ) {
            Node gn = iter.next();
            Binding b = BindingFactory.binding(Var.alloc(graphNode), gn);
            list.add(b);
        }

        QueryIterator qIter = QueryIterPlainWrapper.create(list.iterator(), evaluator.getExecContext());
        return TableFactory.create(qIter);

    }

    static Table evalGraph(OpGraph opGraph, Evaluator evaluator) {
        ExecutionContext execCxt = evaluator.getExecContext();

        if ( !Var.isVar(opGraph.getNode()) ) {
            DatasetGraph dsg = execCxt.getDataset();
            Node graphNode = opGraph.getNode();
            if ( !dsg.containsGraph(graphNode) )
                return new TableEmpty();
            Graph graph = execCxt.getDataset().getGraph(opGraph.getNode());
            if ( graph == null ) // But contains was true?!!
                throw new InternalErrorException("Graph was present, now it's not");
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph);
            Evaluator e2 = EvaluatorFactory.create(execCxt2);
            return eval(e2, opGraph.getSubOp());
        }

        // Graph node is a variable.
        Var gVar = Var.alloc(opGraph.getNode());
        Table current = null;
        for ( Iterator<Node> iter = execCxt.getDataset().listGraphNodes() ; iter.hasNext() ; ) {
            Node gn = iter.next();
            Graph graph = execCxt.getDataset().getGraph(gn);
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph);
            Evaluator e2 = EvaluatorFactory.create(execCxt2);

            Table tableVarURI = TableFactory.create(gVar, gn);
            // Evaluate the pattern, join with this graph node possibility.
            // XXX If Var.ANON then no-opt.
            Table patternTable = eval(e2, opGraph.getSubOp());
            Table stepResult = evaluator.join(patternTable, tableVarURI);

            if ( current == null )
                current = stepResult;
            else
                current = evaluator.union(current, stepResult);
        }

        if ( current == null )
            // Nothing to loop over
            return new TableEmpty();
        return current;
    }

    static Table evalQuadPattern(OpQuadPattern opQuad, Evaluator evaluator) {
        if ( opQuad.isEmpty() )
            return TableFactory.createUnit();

        ExecutionContext cxt = evaluator.getExecContext();
        DatasetGraph ds = cxt.getDataset();
        BasicPattern pattern = opQuad.getBasicPattern();

        if ( !opQuad.getGraphNode().isVariable() ) {
            if ( !opQuad.getGraphNode().isURI() ) {
                throw new ARQInternalErrorException("Not a URI or variable: " + opQuad.getGraphNode());
            }
            Graph g = null;

            if ( opQuad.isDefaultGraph() )
                g = ds.getDefaultGraph();
            else
                g = ds.getGraph(opQuad.getGraphNode());
            if ( g == null )
                return new TableEmpty();
            ExecutionContext cxt2 = new ExecutionContext(cxt, g);
            QueryIterator qIter = executeBGP(pattern, QueryIterRoot.create(cxt2), cxt2);
            return TableFactory.create(qIter);
        } else {
            // Variable.
            Var gVar = Var.alloc(opQuad.getGraphNode());
            // Or just just devolve to OpGraph and get OpUnion chain of OpJoin
            QueryIterConcat concat = new QueryIterConcat(cxt);
            for ( Iterator<Node> graphNodes = cxt.getDataset().listGraphNodes() ; graphNodes.hasNext() ; ) {
                Node gn = graphNodes.next();
                // Op tableVarURI = TableFactory.create(gn.getName(),
                // Node.createURI(uri));

                Graph g = cxt.getDataset().getGraph(gn);
                Binding b = BindingFactory.binding(BindingRoot.create(), gVar, gn);
                ExecutionContext cxt2 = new ExecutionContext(cxt, g);

                // Eval the pattern, eval the variable, join.
                // Pattern may be non-linear in the variable - do a pure execution.
                Table t1 = TableFactory.create(gVar, gn);
                QueryIterator qIter = executeBGP(pattern, QueryIterRoot.create(cxt2), cxt2);
                Table t2 = TableFactory.create(qIter);
                Table t3 = evaluator.join(t1, t2);
                concat.add(t3.iterator(cxt2));
            }
            return TableFactory.create(concat);
        }
    }

    private static QueryIterator executeBGP(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        if ( pattern.isEmpty() )
            return input;

        boolean hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables);
        // Execute pattern: no reordering.
        QueryIterator qIter = QC.executeDirect(pattern, input, execCxt);

        // Remove non-distinguished variables here.
        // Project out only named variables.
        if ( hideBNodeVars )
            qIter = new QueryIterDistinguishedVars(qIter, execCxt);
        return qIter;
    }
}
