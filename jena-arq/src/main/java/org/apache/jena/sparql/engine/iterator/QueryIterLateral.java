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

package org.apache.jena.sparql.engine.iterator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.algebra.table.Table1;
import org.apache.jena.sparql.algebra.table.TableBuilder;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.VarUtils;

public class QueryIterLateral extends QueryIterRepeatApply {

    private final Op subOp;
    private final boolean isUnit;

    public QueryIterLateral(QueryIterator input, Op subOp, ExecutionContext execCxt) {
        super(input, execCxt);
        this.subOp = subOp;
        this.isUnit = isJoinIdentity(subOp);
    }

    private static boolean isJoinIdentity(Op op) {
        if( ! ( op instanceof OpTable table ) )
            return false;
        return table.isJoinIdentity();
    }

    /** Choice of variable setting */
    private static Op assignments(Op op, VarExprList varExprs) {
        // Assign (LET) is safer (it is not an error to assign to variable if already
        // set to the same term) but this operation is not portable.
        //return OpAssign.create(op, varExprs);

        // BIND
        return OpExtend.create(op, varExprs);
    }

    @Override
    protected QueryIterator nextStage(Binding binding) {
        if ( isUnit )
            return QueryIterSingleton.create(binding, super.getExecContext());
        Op op = Substitute.inject(subOp, binding);
        return QC.execute(op, binding, super.getExecContext());
    }

    /*
     * This transform applies variable substitution by injecting a binding for the variable and
     * also substituting the variable in the op.
     * The second step - substitution - is strictly unnecessary it happens anyway.
     */
    public static class TransformInject extends TransformCopy {

        private final Set<Var> injectVars;
        private final Set<Node> varsAsNodes;
        private final Function<Var, Node> replacement;
        private final Binding binding;
        private static final boolean substitute = true;

        public TransformInject(Set<Var> injectVars, Binding binding) {
            this.injectVars = injectVars;
            this.varsAsNodes = Set.copyOf(injectVars);
            this.replacement = binding::get;
            this.binding = binding;
        }

        @Override
        public Op transform(OpBGP opBGP) {
            if ( injectVars.isEmpty())
                return opBGP;
            BasicPattern bp = opBGP.getPattern();
            List<Triple> triples = bp.getList();
            // Alt - build up triple by triple.
                //generateAssignmentTriple(triple, assigns, builder);
            Set<Var> bgpVars = new LinkedHashSet<>();
            VarUtils.addVarsTriples(bgpVars, triples);
            Set<Var> x = SetUtils.intersection(bgpVars, injectVars);
            if ( x.isEmpty())
                return opBGP;
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();
            for ( Var var : x )
                generateAssignmentVar(var, assigns, builder);

            if ( assigns.isEmpty() )
                return super.transform(opBGP);

            // If no substitutions, do less work.
            Binding substitutions = builder.build();
            Op opExec = substitute
                    ? Substitute.substitute(opBGP, substitutions)
                    : opBGP;
            opExec = assignments(opExec, assigns);
            return opExec;
        }

//        @Override
//        public Op transform(OpQuadBlock opQuadBlock) {
//            opQuadBlock.getPattern();
//        }

        @Override
        public Op transform(OpQuadPattern opQuadPattern) {
            // "One node : Commonality with OpGraph, OpService (OpDatasetNames)
            // Function for maker.
            BasicPattern bgp = opQuadPattern.getBasicPattern();
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();
            Node gn = opQuadPattern.getGraphNode();

            generateAssignmentNode(gn, assigns, builder);

            for ( Triple t : bgp ) {
                generateAssignmentTriple(t, assigns, builder);
            }
            if ( assigns.isEmpty() )
                return super.transform(opQuadPattern);
            // If no substitutions, do less work.
            Op opExec = opQuadPattern;
            if ( substitute ) {
                Binding substitutions = builder.build();
                opExec = Substitute.substitute(opQuadPattern, substitutions);
            }
            opExec = assignments(opExec, assigns);
            return opExec;
        }

        @Override
        public Op transform(OpService opService, Op subOp) {
            Node g = opService.getService();
            if ( ! workToDo(g) )
                return super.transform(opService, subOp);
            // Fast small lists?
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();
            Var var = Var.alloc(g);
            generateAssignmentVar(var, assigns, builder);
            if ( assigns.isEmpty() )
                return super.transform(opService, subOp);
            // subOp has already been processed.

            Node g2 = g;
            Op op2;
            if ( substitute ) {
                Binding substitutions = builder.build();
                g2 = substitutions.get(var);
                op2 = new OpService(g2, subOp, opService.getSilent());
            } else
                op2 = new OpService(g, subOp, opService.getSilent());
            Op opExec = assignments(op2, assigns);
            return opExec;
        }

        @Override
        public Op transform(OpGraph opGraph, Op subOp) {
            Node g = opGraph.getNode();
            if ( ! workToDo(g) )
                return super.transform(opGraph, subOp);
            // Fast small lists?
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();
            Var var = Var.alloc(g);
            generateAssignmentVar(var, assigns, builder);
            if ( assigns.isEmpty() )
                return super.transform(opGraph, subOp);
            // subOp has already been processed.

            Node g2 = g;
            Op op2;
            if ( substitute ) {
                Binding substitutions = builder.build();
                g2 = substitutions.get(var);
                op2 = new OpGraph(g2, subOp);
            } else
                op2 = new OpGraph(g, subOp);
            Op opExec = assignments(op2, assigns);
            return opExec;
        }

        @Override
        public Op transform(OpDatasetNames opDatasetNames) {
            Node g = opDatasetNames.getGraphNode();
            if ( ! workToDo(g) )
                return super.transform(opDatasetNames);
            Var var = Var.alloc(g);
            Node g2 = replacement.apply(var);
            Op op2 = new OpGraph(g2, OpTable.unit());
            return op2;
        }

//        Binding for variables occurs in several places in SPARQL:
    //
//            Basic Graph Pattern Matching
//            Property Path Patterns
//            evaluation of algebra form Graph(var,P) involving a variable (from the syntax GRAPH ?variable {&hellip;})
        // and also nested (table unit) inside (extend)

        @Override
        public Op transform(OpPath opPath) {
            // Exactly one of predicate and path is defined.
            TriplePath path = opPath.getTriplePath();
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();

            if ( path.isTriple() ) {
                Triple t1 = path.asTriple();
                generateAssignmentTriple(t1, assigns, builder);
                Triple t2 = applyReplacement(t1, replacement);
                if ( t1.equals(t2) )
                    return opPath;
                TriplePath path2 = new TriplePath(t2);
                return new OpPath(path2);
            }

            // Path, not predicate.
            Node s = path.getSubject();
            Node o = path.getObject();
            if ( ! workToDo(s) && ! workToDo(o) )
                return super.transform(opPath);

            generateAssignmentNode(s, assigns, builder);
            generateAssignmentNode(o, assigns, builder);

            if ( assigns.isEmpty() )
                return super.transform(opPath);

            Node s2 = applyReplacement(s, replacement);
            Node o2 = applyReplacement(o, replacement);
            TriplePath path2 = new TriplePath(s2, path.getPath(), o2);
            Op op2 = new OpPath(path2);
            Op opExec = assignments(op2, assigns);
            return opExec;
        }

        @Override
        public Op transform(OpTriple opTriple) {
            Triple triple = opTriple.getTriple();
            VarExprList assigns = new VarExprList();
            BindingBuilder builder = BindingFactory.builder();
            generateAssignmentTriple(triple, assigns, builder);
            if ( assigns.isEmpty() )
                return super.transform(opTriple);
            Triple t2 = triple;
            if ( substitute )
                t2 = applyReplacement(triple, replacement);
            Op op2 = new OpTriple(t2);
            Op opExec = assignments(op2, assigns);
            return opExec;
        }

        private OpTable tableUnitTransformed = null;

        @Override
        public Op transform(OpTable opTable) {
            // Unit table.
            if ( opTable.isJoinIdentity() ) {
                if ( tableUnitTransformed == null ) {
                    Table table2 = new Table1(binding);
                    // Multiple assignment does not matter!
                    tableUnitTransformed = OpTable.create(table2);
                }
                return tableUnitTransformed;
            }

            // By the assignment restriction, the binding only needs to be added to each row of the table.
            Table table = opTable.getTable();

            TableBuilder tableBuilder = TableFactory.builder();
            tableBuilder.addVars(table.getVars());
            tableBuilder.addVarsFromRow(binding);

            BindingBuilder builder = BindingFactory.builder();
            table.iterator(null).forEachRemaining(row -> {
                builder.reset();
                builder.addAll(row);

                // Forcibly add the input binding - this may reassign variables.
                // The restriction imposed by SyntaxVarScope.checkLATERAL prevents
                // reassignment of a variable to a _different_ value.
                binding.forEach(builder::set);

                tableBuilder.addRow(builder.build());
            });
            Table newTable = tableBuilder.build();
            return OpTable.create(newTable);
        }

        private Triple applyReplacement(Triple triple, Function<Var, Node> replacement) {
            Node s2 = applyReplacement(triple.getSubject(), replacement);
            Node p2 = applyReplacement(triple.getPredicate(), replacement);
            Node o2 = applyReplacement(triple.getObject(), replacement);
            Triple t2 = Triple.create(s2, p2, o2);
            return t2;
        }

        private void generateAssignmentTriple(Triple triple, VarExprList assigns, BindingBuilder builder) {
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();
            if ( ! workToDo(s) && ! workToDo(p) && ! workToDo(o) )
                return;
            generateAssignmentNode(s, assigns, builder);
            generateAssignmentNode(p, assigns, builder);
            generateAssignmentNode(o, assigns, builder);
        }

        private static Node applyReplacement(Node n, Function<Var, Node> replacement) {
            if ( n instanceof Var x ) {
                Node x2 = replacement.apply(x);
                return ( x2 == null ) ? n : x2;
            }
            return n;
        }

        private void generateAssignmentNode(Node n, VarExprList assigns, BindingBuilder builder) {
            if ( n == null )
                return;
            if ( ! Var.isVar(n) )
                return;
            generateAssignmentVar(Var.alloc(n), assigns, builder);
        }

        private void generateAssignmentVar(Var var, VarExprList assigns, BindingBuilder builder) {
            Node value = replacement.apply(var);
            if ( value != null ) {
                if ( ! builder.contains(var) ) {
                    builder.add(var, value);
                    assigns.add(var, NodeValue.makeNode(value));
                }
            }
        }

        // Avoid object allocation.
        private boolean workToDo(Node n) {
            if ( n == null )
                return false;
            if ( ! Var.isVar(n) )
                return false;
            Var v = Var.alloc(n);
            return null != replacement.apply(v);
        }
    }
}
