/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.engine2.Table;
import com.hp.hpl.jena.query.engine2.op.*;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.layout2.SQLBridge2;
import com.hp.hpl.jena.sdb.store.SQLBridge;

public class QuadToSDB 
{
    private static Log log = LogFactory.getLog(QuadToSDB.class) ; 

    private CompileContext context ;

    public static SqlNode compile(Op op, CompileContext context)
    {
        return new QuadToSDB(context).compile(op) ;
    }

    
    private QuadToSDB(CompileContext context)
    { this.context = context ; }

    private SqlNode compile(Op op)
    {
        SQLBridge sb = new SQLBridge2() ;
        SqlNode n = new QuadVisitor().process(op) ;
        return n ; 
    }
    

    class QuadVisitor implements OpVisitor
    {
        // The result slot.   
        SqlNode result = null ;

        private SqlNode process(Op op)
        {
            QuadVisitor q = new QuadVisitor() ;
            op.visit(q) ;
            return q.result ;
        }

        public void visit(OpBGP opBGP)
        { broken("OpBGP should not appear") ; }


        public void visit(OpQuadPattern quadPattern)
        {
            QuadBlock qBlk = new QuadBlock(quadPattern) ;
            SqlNode node = QuadPatternCompiler.compile(context, qBlk) ;
            result = node ;
        }

        public void visit(OpJoin opJoin)
        {
            SqlNode left = process(opJoin.getLeft()) ;
            SqlNode right = process(opJoin.getRight()) ;
            result = QC.innerJoin(context, left, right) ;
        }

        public void visit(OpLeftJoin opLeftJoin)
        {
            if ( opLeftJoin.getExpr() != null )
                log.warn("LeftJoin has condition [ignored at the moment]") ;
            SqlNode left = process(opLeftJoin.getLeft()) ;
            SqlNode right = process(opLeftJoin.getRight()) ;
            result = QC.leftJoin(context, left, right) ;
        }

        public void visit(OpUnion opUnion)
        {}

        public void visit(OpFilter opFilter)
        {}

        public void visit(OpGraph opGraph)
        {}

        public void visit(Table table)
        {}

        public void visit(OpPlanElement element)
        {}

        public void visit(OpDatasetNames dsNames)
        {}

        private void broken(String msg)
        { 
            log.fatal(msg) ;
            System.exit(99) ;
        }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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