/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.opt;

import com.hp.hpl.jena.sparql.algebra.ExtBuilder;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier;
import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.iterator.QueryIterOptionalIndex;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.builders.BuilderExpr;
import com.hp.hpl.jena.sparql.sse.builders.BuilderLib;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class TransformIndexJoin extends TransformCopy
{
    // Transformer/TransformCopy that applied many transforms more efficiently.
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    { 
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;

        if ( canDoLinear )
            // Streamed evaluation
            return OpSequence.create(left, right) ;
        return super.transform(opJoin, left, right) ;
    }
    
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)
    { 
        boolean canDoLinear = LeftJoinClassifier.isLinear(opLeftJoin) ;

        if ( canDoLinear )
            // Streamed evaluation - Operator?
            return new OpIndexedLeftJoin(left, right, opLeftJoin.getExprs()) ;
        return super.transform(opLeftJoin, left, right) ;
    }
    
    static final String indexedLeftJoin = "indLJ" ;
    
    static class OpIndLJFactory implements ExtBuilder
    {
        @Override
        public String getSubTab()
        {
            return indexedLeftJoin ;
        }

        @Override
        public OpExt make(ItemList argList)
        {
            
            BuilderLib.checkLength(2, 3, argList, "Wrong length for IndexedLeftJoin") ;
            
            Op left = BuilderOp.build(argList.get(0)) ;
            Op right = BuilderOp.build(argList.get(1)) ;
            ExprList exprs = null ;
            if ( argList.size() == 3 )
            {
                exprs = BuilderExpr.buildExprList(argList.get(2)) ;
            }
            return new  OpIndexedLeftJoin(left, right, exprs);
        }
        
    }
    
    static class OpIndexedLeftJoin extends OpExt
    {

        private Op left ;
        private Op right ;
        private ExprList exprs ;

        OpIndexedLeftJoin(Op left, Op right, ExprList exprs)
        { 
            this.left = left ;
            this.right = right ;
            this.exprs = exprs ;
        }
        
        @Override
        public Op effectiveOp()
        {
            return OpLeftJoin.create(left, right, exprs) ;
        }

        @Override
        public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
        {
            Op opLeft = left ;
            Op opRight = right ;
            if (exprs != null )
                opRight = OpFilter.filter(exprs, opRight) ;
            
            QueryIterator left = QC.execute(opLeft, input, execCxt) ;
            QueryIterator qIter = new QueryIterOptionalIndex(left, opRight, execCxt) ;
            return qIter ;
        }

        @Override
        public String getSubTag()
        {
            return indexedLeftJoin ;
        }

        @Override
        public void outputArgs(IndentedWriter out, SerializationContext sCxt)
        {
            SSE.write(out, left) ;
            out.print(" ") ;
            SSE.write(out, right) ;
            if ( exprs != null )
            {
                out.print(" ") ;
                WriterExpr.output(out, exprs, sCxt) ;
            }
        }

        @Override
        public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
        {
            return false ;
        }

        @Override
        public int hashCode()
        {
            return 0 ;
        }
        
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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