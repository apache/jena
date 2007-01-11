/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.OpVars;
import com.hp.hpl.jena.query.engine2.op.*;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.SQLBridgeFactory;


public abstract class QueryCompilerMain implements QueryCompiler 
{
    // Do we need this as a class?
    protected SDBRequest request ;
    
    
    public QueryCompilerMain(SDBRequest request)
    { 
        this.request = request ;
    }
    
    public Op compile(Op op)
    {
        Transform t = new TransformSDB(request, createQuadBlockCompiler()) ;
        op = Transformer.transform(t, op) ;
        // If it's the whole query, then we only need the  
        
        // Find the first non-modifier.
        Op patternOp = op ;
        while ( patternOp instanceof OpModifier )
            patternOp = ((OpModifier)patternOp).getSubOp() ;
        
        boolean patternIsOneSQLStatement = QC.isOpSQL(patternOp) ;
        
            
        OpWalker.walk(op, new SqlNodesFinisher(patternIsOneSQLStatement)) ;
        return op ;
    }

    protected abstract QuadBlockCompiler createQuadBlockCompiler() ;
    
    public ConditionCompiler getConditionCompiler()
    {
        return null ;
    }
    
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
            
            if ( justProjectVars )
                projectVars = QC.queryOutVars(request.getQuery()) ;
            else
            {
                @SuppressWarnings("unchecked")
                Collection<Var> tmp = (Collection<Var>)OpVars.patternVars(opSQL.getOriginal()) ;
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