/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.main.OpExtMain;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpSQL extends OpExtMain
{
    private SqlNode sqlNode ;
    private Op originalOp ;
    private SQLBridge bridge = null ; 
    private SDBRequest request ;
    
    public OpSQL(SqlNode sqlNode, Op original, SDBRequest request)
    {
        // Trouble is, we may have to throw the SqlNode away because of substitution.  What a waste!
        this.request = request ;
        this.sqlNode = sqlNode ;
        this.originalOp = original ;
        // Only set at the top, eventually, when we know the projection variables.
        this.bridge = null ;
    }

    public OpSQL copy() 
    { return this ; }      // We're immutable - return self.
    
    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
    { return new QueryIterSQL(this, input, execCxt) ; }

    public QueryIterator exec(ExecutionContext execCxt)
    { return exec(BindingRoot.create(), execCxt) ; }

    public QueryIterator exec(Binding parent, ExecutionContext execCxt)
    {
        if ( parent == null )
            parent = BindingRoot.create() ;
        QueryIterator qIter = QC.exec(this,
                                      getRequest(),
                                      parent, 
                                      execCxt) ;
        return qIter ;
    }


    public Op getOriginal()     { return originalOp ; }
    public Op effectiveOp()     { return originalOp ; }

    @Override
    public int hashCode()
    {
        return sqlNode.hashCode() ^ 0x1 ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        // SqlNodes don't provide structural equality (yet?).
        if ( ! ( other instanceof OpSQL ) ) return false ;
        OpSQL opSQL = (OpSQL)other ;
        return sqlNode.equals(opSQL.sqlNode) ;
    }

    public SDBRequest getRequest() { return request ; }

    @Override
    public void output(IndentedWriter out)
    {
        out.print(Plan.startMarker) ;
        out.println("OpSQL --------") ;
        out.incIndent() ;
        sqlNode.output(out) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
        out.print("--------") ;
        out.print(Plan.finishMarker) ;
    }

    public String toSQL()
    {
       return QC.toSqlString(this, request) ;
    }

    public SqlNode getSqlNode()
    {
        return sqlNode ;
    }

    public void resetSqlNode(SqlNode sqlNode2)
    { sqlNode = sqlNode2 ; }

    public SQLBridge getBridge()            { return bridge ; }

    public void setBridge(SQLBridge bridge) { this.bridge = bridge ; }

    public String getName()                 { return "OpSQL" ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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