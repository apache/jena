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

package com.hp.hpl.jena.sdb.compiler;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode ;
import com.hp.hpl.jena.sdb.store.SQLBridge ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpSQL extends OpExt
{
    private SqlNode sqlNode ;
    private Op originalOp ;
    private SQLBridge bridge = null ; 
    private SDBRequest request ;
    
    public OpSQL(SqlNode sqlNode, Op original, SDBRequest request)
    {
        super("SQL") ;
        // Trouble is, we may have to throw the SqlNode away because of substitution.  What a waste!
        this.request = request ;
        this.sqlNode = sqlNode ;
        this.originalOp = original ;
        // Only set at the top, eventually, when we know the projection variables.
        this.bridge = null ;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
    { return new QueryIterOpSQL(this, input, execCxt) ; }

    public QueryIterator exec(ExecutionContext execCxt)
    { return exec(BindingRoot.create(), execCxt) ; }

    public QueryIterator exec(Binding parent, ExecutionContext execCxt)
    {
        if ( parent == null )
            parent = BindingRoot.create() ;
        QueryIterator qIter = SDB_QC.exec(this,
                                      getRequest(),
                                      parent, 
                                      execCxt) ;
        return qIter ;
    }


    public Op getOriginal()     { return originalOp ; }
    
    @Override
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
       return SDB_QC.toSqlString(this, request) ;
    }

    public SqlNode getSqlNode()
    {
        return sqlNode ;
    }

    public void resetSqlNode(SqlNode sqlNode2)
    { sqlNode = sqlNode2 ; }

    public SQLBridge getBridge()            { return bridge ; }

    public void setBridge(SQLBridge bridge) { this.bridge = bridge ; }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("'''") ;
        sqlNode.output(out) ;
        out.print("'''") ;
    }
}
