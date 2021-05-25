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
package org.apache.jena.permissions.query.rewriter;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class rewrites the query by examining each operation in the algebra
 * returned by the Jena SPARQL parser.
 * <p>
 * This implementation inserts security evaluator checks where necessary.
 * </p>
 */
public class OpRewriter implements OpVisitor {
    private static Logger LOG = LoggerFactory.getLogger(OpRewriter.class);
    private OpSequence result;
    private final Node graphIRI;
    private final SecurityEvaluator securityEvaluator;
    // if true the restricted data are silently ignored.
    // default false
    private final boolean silentFail;

    /**
     * Constructor
     *
     * @param securityEvaluator The security evaluator to use
     * @param graphIRI          The IRI for the default graph.
     */
    public OpRewriter(final SecurityEvaluator securityEvaluator, final Node graphIRI) {
        this.securityEvaluator = securityEvaluator;
        this.graphIRI = graphIRI;
        this.silentFail = false;
        reset();
    }

    /**
     * Constructor
     *
     * @param securityEvaluator The security evaluator to use
     * @param graphIRI          The IRI for the default graph.
     */
    public OpRewriter(final SecurityEvaluator securityEvaluator, final String graphIRI) {
        this(securityEvaluator, NodeFactory.createURI(graphIRI));
    }

    /**
     * Add the operation to the result.
     *
     * @param op the operation to add.
     */
    private void addOp(final Op op) {
        result.add(op);
    }

    /**
     * Get the result of the rewrite.
     *
     * @return the resulting operator
     */
    public Op getResult() {
        if (result.size() == 0) {
            return OpNull.create();
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return result;

    }

    /**
     * Register variables.
     *
     * Registers n as a variable if it is one.
     *
     * @param n         the node to check
     * @param variables the list of variable nodes
     * @Return n for chaining.
     */
    private Node registerVariables(final Node n, final List<Node> variables) {
        if (n.isVariable() && !variables.contains(n)) {
            variables.add(n);
        }
        return n;
    }

    /**
     * Reset the rewriter to the initial state.
     *
     * @return this rewriter for chaining.
     */
    public OpRewriter reset() {
        result = OpSequence.create();
        return this;
    }

    /**
     * Register all the variables in the triple.
     *
     * @param t         the triple to register.
     * @param variables The list of variables.
     * @return t for chaining
     */
    private Triple registerBGPTriple(final Triple t, final List<Node> variables) {
        registerVariables(t.getSubject(), variables);
        registerVariables(t.getPredicate(), variables);
        registerVariables(t.getObject(), variables);
        return t;
    }

    /**
     * Rewrites the subop of op1 and returns the result.
     *
     * @param op1
     * @return the rewritten op.
     */
    private Op rewriteOp1(final Op1 op1) {
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        op1.getSubOp().visit(rewriter);
        return rewriter.getResult();
    }

    /**
     * rewrites the left and right parts of the op2 the left part is returned the
     * right part is placed in the rewriter
     *
     * @param op2
     * @param rewriter
     * @return the rewritten op.
     */
    private Op rewriteOp2(final Op2 op2, final OpRewriter rewriter) {
        op2.getLeft().visit(rewriter.reset());
        final Op left = rewriter.getResult();
        op2.getRight().visit(rewriter.reset());
        return left;
    }

    /**
     * rewrite source to dest and returns dest
     *
     * @param source
     * @param dest
     * @return the rewritten op.
     */
    private OpN rewriteOpN(final OpN source, final OpN dest) {
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        for (final Op o : source.getElements()) {
            o.visit(rewriter.reset());
            dest.add(rewriter.getResult());
        }
        return dest;
    }

    /**
     * rewrites the subop of assign.
     */
    @Override
    public void visit(final OpAssign opAssign) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpAssign");
        }
        addOp(OpAssign.assign(rewriteOp1(opAssign), opAssign.getVarExprList()));
    }

    @Override
    public void visit(final OpBGP opBGP) throws ReadDeniedException, AuthenticationRequiredException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpBGP");
        }
        Object principal = securityEvaluator.getPrincipal();
        if (!securityEvaluator.evaluate(principal, Action.Read, graphIRI)) {
            if (silentFail) {
                return;
            }
            throw new ReadDeniedException(SecuredItem.Util.modelPermissionMsg(graphIRI));
        }

        // if the user can read any triple just add the opBGP
        if (securityEvaluator.evaluate(principal, Action.Read, graphIRI, Triple.ANY)) {
            addOp(opBGP);
        } else {
            // add security filtering to the resulting triples
            final List<Triple> newBGP = new ArrayList<>();
            final List<Node> variables = new ArrayList<>();
            // register all variables
            for (final Triple t : opBGP.getPattern().getList()) {
                newBGP.add(registerBGPTriple(t, variables));
            }
            // create the security function.
            final SecuredFunction secFunc = new SecuredFunction(graphIRI, securityEvaluator, variables, newBGP);
            // create the filter
            Op filter = OpFilter.filter(secFunc, new OpBGP(BasicPattern.wrap(newBGP)));
            // add the filter
            addOp(filter);
        }
    }

    /**
     * Rewrite left and right
     */
    @Override
    public void visit(final OpConditional opCondition) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpConditional");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(new OpConditional(rewriteOp2(opCondition, rewriter), rewriter.getResult()));
    }

    /**
     * returns the dsNames
     */
    @Override
    public void visit(final OpDatasetNames dsNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpDatasetName");
        }
        addOp(dsNames);
    }

    /**
     * Rewrite left and right
     */
    @Override
    public void visit(final OpDiff opDiff) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpDiff");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(OpDiff.create(rewriteOp2(opDiff, rewriter), rewriter.getResult()));
    }

    /**
     * Rewrite sequence elements
     */
    @Override
    public void visit(final OpDisjunction opDisjunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpDisjunction");
        }
        addOp(rewriteOpN(opDisjunction, OpDisjunction.create()));
    }

    /**
     * rewrites the subop of distinct
     */
    @Override
    public void visit(final OpDistinct opDistinct) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpDistinct");
        }
        addOp(new OpDistinct(rewriteOp1(opDistinct)));
    }

    /**
     * Returns the Ext
     */
    @Override
    public void visit(final OpExt opExt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpExt");
        }
        addOp(opExt);
    }

    /**
     * rewrites the subop of extend.
     */
    @Override
    public void visit(final OpExtend opExtend) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpExtend");
        }
        addOp(OpExtend.extend(rewriteOp1(opExtend), opExtend.getVarExprList()));
    }

    /**
     * rewrites the subop of filter.
     */
    @Override
    public void visit(final OpFilter opFilter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpFilter");
        }
        addOp(OpFilter.filterBy(opFilter.getExprs(), rewriteOp1(opFilter)));
    }

    /**
     * rewrites the subop of graph.
     */
    @Override
    public void visit(final OpGraph opGraph) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpGraph");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, opGraph.getNode());
        opGraph.getSubOp().visit(rewriter);
        addOp(new OpGraph(opGraph.getNode(), rewriter.getResult()));
    }

    /**
     * rewrites the subop of group.
     */
    @Override
    public void visit(final OpGroup opGroup) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpGroup");
        }
        addOp(OpGroup.create(rewriteOp1(opGroup), opGroup.getGroupVars(), opGroup.getAggregators()));
    }

    /**
     * Parses the joins and recursively calls the left and right parts
     */
    @Override
    public void visit(final OpJoin opJoin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpJoin");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(OpJoin.create(rewriteOp2(opJoin, rewriter), rewriter.getResult()));
    }

    /**
     * returns the label
     */
    @Override
    public void visit(final OpLabel opLabel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpLabel");
        }
        addOp(opLabel);
    }

    /**
     * Parses the joins and recursively calls the left and right parts
     */
    @Override
    public void visit(final OpLeftJoin opLeftJoin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpLeftJoin");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(OpLeftJoin.create(rewriteOp2(opLeftJoin, rewriter), rewriter.getResult(), opLeftJoin.getExprs()));
    }

    /**
     * rewrites the subop of list.
     */
    @Override
    public void visit(final OpList opList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpList");
        }
        addOp(new OpList(rewriteOp1(opList)));
    }

    /**
     * Rewrite left and right
     */
    @Override
    public void visit(final OpMinus opMinus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpMinus");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(OpMinus.create(rewriteOp2(opMinus, rewriter), rewriter.getResult()));
    }

    /**
     * returns the null
     */
    @Override
    public void visit(final OpNull opNull) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpNull");
        }
        addOp(opNull);
    }

    /**
     * rewrites the subop of order.
     */
    @Override
    public void visit(final OpOrder opOrder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpOrder");
        }
        addOp(new OpOrder(rewriteOp1(opOrder), opOrder.getConditions()));
    }

    /**
     * Returns the path
     */
    @Override
    public void visit(final OpPath opPath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpPath");
        }
        addOp(opPath);
    }

    /**
     * rewrites the subop of proc.
     */
    @Override
    public void visit(final OpProcedure opProc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpProc");
        }
        if (opProc.getProcId() != null) {
            addOp(new OpProcedure(opProc.getProcId(), opProc.getArgs(), rewriteOp1(opProc)));
        } else {
            addOp(new OpProcedure(opProc.getURI(), opProc.getArgs(), rewriteOp1(opProc)));
        }
    }

    /**
     * rewrites the subop of project.
     */
    @Override
    public void visit(final OpProject opProject) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpProject");
        }
        addOp(new OpProject(rewriteOp1(opProject), opProject.getVars()));
    }

    /**
     * rewrites the subop of propFunc.
     */
    @Override
    public void visit(final OpPropFunc opPropFunc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpPropFunc");
        }
        addOp(new OpPropFunc(opPropFunc.getProperty(), opPropFunc.getSubjectArgs(), opPropFunc.getObjectArgs(),
                rewriteOp1(opPropFunc)));
    }

    /**
     * Returns the quad
     */
    @Override
    public void visit(final OpQuad opQuad) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpQuad");
        }
        addOp(opQuad);
    }

    /**
     * Returns the quadpattern
     */
    @Override
    public void visit(final OpQuadPattern quadPattern) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpQuadPattern");
        }
        addOp(quadPattern);
    }

    /**
     * rewrites the subop of reduced.
     */
    @Override
    public void visit(final OpReduced opReduced) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpReduced");
        }
        addOp(OpReduced.create(rewriteOp1(opReduced)));
    }

    /**
     * Rewrite sequence elements
     */
    @Override
    public void visit(final OpSequence opSequence) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpSequence");
        }
        addOp(rewriteOpN(opSequence, OpSequence.create()));
    }

    /**
     * returns the service
     */
    @Override
    public void visit(final OpService opService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting opService");
        }
        addOp(opService);
    }

    /**
     * rewrites the subop of slice
     *
     * This also handles the limit case
     */
    @Override
    public void visit(final OpSlice opSlice) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpSlice");
        }
        addOp(opSlice);
    }

    /**
     * returns the table
     */
    @Override
    public void visit(final OpTable opTable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpTable");
        }
        addOp(opTable);
    }

    /**
     * rewrites the subop of top.
     */
    @Override
    public void visit(final OpTopN opTop) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpTop");
        }
        addOp(new OpTopN(rewriteOp1(opTop), opTop.getLimit(), opTop.getConditions()));
    }

    /**
     * Converts to BGP
     */
    @Override
    public void visit(final OpTriple opTriple) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpTriple");
        }
        visit(opTriple.asBGP());
    }

    /**
     * Rewrite left and right
     */
    @Override
    public void visit(final OpUnion opUnion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpUnion");
        }
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        addOp(OpUnion.create(rewriteOp2(opUnion, rewriter), rewriter.getResult()));
    }

    @Override
    public void visit(OpQuadBlock quadBlock) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting visiting OpQuadBlock");
        }
        addOp(quadBlock);
    }
}
