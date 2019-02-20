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

package org.apache.jena.sdb.compiler;

import static org.apache.jena.sdb.compiler.OpLibSDB.asProject ;
import static org.apache.jena.sdb.compiler.OpLibSDB.sub ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.compiler.rewrite.QuadBlockRewriteCompiler ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.core.sqlnode.SqlSelectBlock ;
import org.apache.jena.sdb.store.SQLBridge ;
import org.apache.jena.sdb.store.SQLBridgeFactory ;
import org.apache.jena.sparql.algebra.* ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprVar;

public abstract class QueryCompilerMain implements QueryCompiler 
{
    protected SDBRequest request ;
    
    public QueryCompilerMain(SDBRequest request)
    { 
        this.request = request ;
    }
    
    @Override
    public Op compile(final Op op)
    {
        QuadBlockCompiler quadCompiler = createQuadBlockCompiler() ;
        if ( request.getContext().isTrue(SDB.useQuadRewrite) )
            quadCompiler = new QuadBlockRewriteCompiler(request, quadCompiler) ;
        
        // XXX Turn off Limit/Offset processing - do later so as to enable filter processing.
        boolean b = request.LimitOffsetTranslation ;
        request.LimitOffsetTranslation = false ;
        
        Transform t = new TransformSDB(request, quadCompiler) ;
        Op op2 = Transformer.transform(t, op) ;
        
        // Modifiers: the structure is:
        //    slice
        //      distinct/reduced
        //        project
        //          order
        //            [toList]
        
        // Find the first non-modifier. WRONG with SqlSelectBlocks.
        Op patternOp = op2 ;
        while ( patternOp instanceof OpModifier )
            patternOp = ((OpModifier)patternOp).getSubOp() ;
        
        boolean patternIsOneSQLStatement = SDB_QC.isOpSQL(patternOp) ;
            
        // Find all OpSQL nodes and put a bridge round them.
        OpWalker.walk(op2, new SqlNodesFinisher(patternIsOneSQLStatement)) ;

        request.LimitOffsetTranslation = b ;
        
        // At this point, we have converted what we can into OpSQL.
        // Some changes can still be made now w have the whole SQL expression.
        
        Op op3 = postProcessSQL(op2) ;
        
        return op3 ;
    }

    protected abstract Op postProcessSQL(Op op) ;
    
    /*
        // This rewrite can be done in all layouts but for layout2, it needs to be
        // done after other changes so QueryCompiler2 overrides this method
        // and does things in the right order.
        
        // (slice (distinct ....))
        op = rewriteDistinct(op, request) ;
        op = rewriteLimitOffset(op, request) ;
        return op ;
     */
    
    public abstract QuadBlockCompiler createQuadBlockCompiler() ;
    
    @Override
    public ConditionCompiler getConditionCompiler()
    {
        return null ;
    }
    
    // Add the "bridge" that gets the lexical forms etc for the projected, or all, variables. 
    private class SqlNodesFinisher extends OpVisitorBase
    {
        private boolean justProjectVars ;
        SqlNodesFinisher(boolean justProjectVars)
        { this.justProjectVars = justProjectVars ; }
        
        @Override
        public void visit(OpExt op)
        {
            if ( ! ( op instanceof OpSQL ) )
            {
                super.visit(op) ;
                return ;
            }
            
            OpSQL opSQL = (OpSQL)op ;

            List<Var> projectVars = null ;
                        
            if ( justProjectVars && request.getQuery() != null )
                // Need project vars and also the ORDER BY (for external sorting)
                projectVars = SDB_QC.queryOutVars(request.getQuery()) ;
            else
            {
                // All variables.
                Collection<Var> tmp = OpVars.visibleVars(opSQL.getOriginal()) ;
                projectVars = new ArrayList<Var>(tmp) ;
            }

            SqlNode sqlNode = opSQL.getSqlNode() ;
            
            SQLBridgeFactory f = request.getStore().getSQLBridgeFactory() ;
            
            SQLBridge bridge = f.create(request, sqlNode, projectVars) ;
            bridge.build();
            sqlNode = bridge.getSqlNode() ;
            
            opSQL.setBridge(bridge) ;
            opSQL.resetSqlNode(sqlNode) ;
        }
    }
    
    
    // -- Library of possible operations that can be applied to all layouts.
    
    protected static Op rewriteLimitOffset(Op op, SDBRequest request)
    {
        Transform t = new LimitOffsetOptimizer(request) ;
        return Transformer.transform(t, op) ;
    }

    
    private static class LimitOffsetOptimizer extends TransformCopy
    {
        private final SDBRequest request ;

        public LimitOffsetOptimizer(SDBRequest request)
        {
            this.request = request ;
        }
        
        // From TransformSDB
        
        @Override
        public Op transform(OpSlice opSlice, Op subOp)
        {
            if ( ! request.LimitOffsetTranslation )
                return super.transform(opSlice, subOp) ;
            
            // Two cases are currently handled:
            // (slice (sql expression))
            // (slice (project ... (sql expression)))
            
            boolean canHandle = false ;
            
            // Relies on the fact that isOpSQL(null) is false.
            if (  SDB_QC.isOpSQL(subOp) )
                canHandle = true ;
            else if ( SDB_QC.isOpSQL(sub(asProject(subOp))) )
            {
                return transformSliceProject(opSlice, (OpProject)subOp) ;
            }

            // Simple slice
            if ( ! SDB_QC.isOpSQL(subOp) )
                return super.transform(opSlice, subOp) ;

            
            
            return transformSlice(opSlice, ((OpSQL)subOp)) ;
        }

        private Op transformSlice(OpSlice opSlice, OpSQL opSQL)
        {
            // (slice X)
            SqlNode sqlSubOp = opSQL.getSqlNode() ;
            SqlNode n = SqlSelectBlock.slice(request, sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
            OpSQL x = new OpSQL(n, opSlice, request) ;
            x.setBridge(opSQL.getBridge()) ;
            return x ;
        }

        public Op transformSliceProject(OpSlice opSlice, OpProject opProject)
        {
            // (slice (project X))
            Op subOp = opProject.getSubOp() ;

            if ( ! SDB_QC.isOpSQL(subOp) )
                // Can't cope - just pass the slice to the general superclass. 
                return super.transform(opSlice, opProject) ;

            OpSQL opSQL = (OpSQL)subOp ;
            SqlNode sqlSubOp = opSQL.getSqlNode() ;
            List<Var> pv = opProject.getVars() ;
            // Do as (slice X)
            SqlNode n = SqlSelectBlock.slice(request, sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
            // Put back project - as an OpProject to leave for the bridge.
            OpSQL x = new OpSQL(n, opProject, request) ;
            x.setBridge(opSQL.getBridge()) ;
            // Bridge will be set later.
            // Is OpProject needed?
            return new OpProject(x, pv) ;
        }
        
    }
    
    protected static Op rewriteDistinct(Op op, SDBRequest request)
    {
        Transform t = new DistinctOptimizer(request) ;
        return Transformer.transform(t, op) ;
    }
    
    private static class DistinctOptimizer extends TransformCopy
    {
        private final SDBRequest request ;

        public DistinctOptimizer(SDBRequest request)
        {
            this.request = request ;
        }

        @Override
        public Op transform(OpDistinct opDistinct, Op subOp)
        { 
            if ( ! request.DistinctTranslation )
                return super.transform(opDistinct, subOp) ;
            
            if ( ! SDB_QC.isOpSQL(subOp) )
                return super.transform(opDistinct, subOp) ;
            SqlNode sqlSubOp = ((OpSQL)subOp).getSqlNode() ;
            SqlNode n = SqlSelectBlock.distinct(request, sqlSubOp) ;
            return new OpSQL(n, opDistinct, request) ; 
        }

    }

    protected static Op rewriteOrder(Op op, SDBRequest request)
    {
        Transform t = new OrderOptimizer(request) ;
        return Transformer.transform(t, op) ;
    }

    private static class OrderOptimizer extends TransformCopy
    {
        private final SDBRequest request ;

        public OrderOptimizer(SDBRequest request)
        {
            this.request = request ;
        }

        @Override
        public Op transform(OpOrder opOrder, Op subOp)
        {
            if ( ! request.OrderTranslation )
                return super.transform(opOrder, subOp) ;

            if ( ! SDB_QC.isOpSQL(subOp) )
                return super.transform(opOrder, subOp) ;

            // Check that all of the SortConditions are simple variable expressions
            boolean orderInSQL = true;
            for (SortCondition sc : opOrder.getConditions()) {
                if (!(sc.getExpression() instanceof ExprVar)) {
                    orderInSQL = false;
                }
            }

            // Simple sorts, so copy them on to the OpSQL and drop the OpOrder
            // (Ordering is then applied in the database, not in memory
            if (orderInSQL) {
                OpSQL opSQL = (OpSQL) subOp;
                SqlNode node = SqlSelectBlock.order(request, opSQL.getSqlNode(), opOrder.getConditions());
                OpSQL x = new OpSQL(node, opSQL, request) ;
                x.setBridge(opSQL.getBridge()) ;
                return x;
            }

            return super.transform(opOrder, subOp) ;
        }

    }
}
