/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.util.List;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine2.Evaluator;
import com.hp.hpl.jena.query.engine2.Table;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpExtBase;
import com.hp.hpl.jena.query.engine2.op.OpWriter;
import com.hp.hpl.jena.query.engine2.table.TableSimple;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;

public class OpSQL extends OpExtBase
{
    private SqlNode sqlNode ;
    private Op originalOp ;
    private List<Var> projectVars = null ;
    private SDBRequest request ;
    
    public OpSQL(SqlNode sqlNode, Op original, SDBRequest request)
    {
        // Only needed for the SqlNode to SQL translation.
        this.request = request ;
        this.sqlNode = sqlNode ;
        this.originalOp = original ;
    }

    public void setProjectVars(List<Var> projectVars) { this.projectVars = projectVars ; }
    
    public Table eval(Evaluator evaluator)
    {
        ExecutionContext execCxt = evaluator.getExecContext() ;
        QueryIterator qIter = QP.exec(this,
                                      request,
                                      //BindingRoot.create(),
                                      null,
                                      execCxt) ;
        return new TableSimple(qIter) ;
    }

    //public Op getOriginal() { return originalOp ; }

    @Override
    public void output(IndentedWriter out)
    {
        out.print(OpWriter.startMarker) ;
        out.println("OpSQL --------") ;
        out.incIndent() ;
        sqlNode.output(out) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
        out.print("--------") ;
        out.print(OpWriter.finishMarker) ;
    }

    public String toSQL()
    {
       return QP.toSqlString(this, request, null, null) ;
    }

    public SqlNode getSqlNode()
    {
        return sqlNode ;
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