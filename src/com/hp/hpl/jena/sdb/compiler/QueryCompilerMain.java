/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.compiler.OpLibSDB.asProject ;
import static com.hp.hpl.jena.sdb.compiler.OpLibSDB.sub ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.compiler.rewrite.QuadBlockRewriteCompiler ;
import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock ;
import com.hp.hpl.jena.sdb.store.SQLBridge ;
import com.hp.hpl.jena.sdb.store.SQLBridgeFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase ;
import com.hp.hpl.jena.sparql.algebra.OpWalker ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;
import com.hp.hpl.jena.sparql.core.Var ;

public abstract class QueryCompilerMain implements QueryCompiler 
{
    protected SDBRequest request ;
    
    public QueryCompilerMain(SDBRequest request)
    { 
        this.request = request ;
    }
    
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
        // done after other chnages so QueryCompiler2 overrides this method
        // and does things in the right order.
        
        // (slice (distinct ....))
        op = rewriteDistinct(op, request) ;
        op = rewriteLimitOffset(op, request) ;
        return op ;
     */
    
    public abstract QuadBlockCompiler createQuadBlockCompiler() ;
    
    public ConditionCompiler getConditionCompiler()
    {
        return null ;
    }
    
    // Add the "bridge" that gets the lecical forms etc for the projected, or all, variables. 
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
                Collection<Var> tmp = OpVars.patternVars(opSQL.getOriginal()) ;
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


}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */