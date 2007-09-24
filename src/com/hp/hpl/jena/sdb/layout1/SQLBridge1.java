/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.SQLBridgeBase;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

public class SQLBridge1 extends SQLBridgeBase
{
    private EncoderDecoder codec ;
    
    public SQLBridge1(SDBRequest request, SqlNode sqlNode, List<Var> projectVars, EncoderDecoder codec)
    { 
        super(request, sqlNode, projectVars) ;
        this.codec = codec ;
    }
    
    @Override
    protected void buildValues()
    { }

    @Override
    protected void buildProject()
    {
        for ( Var v : getProject() )
        {
            if ( ! v.isNamedVar() )
                continue ;
            // Value scope == IdScope for layout1
            // CHECK
            ScopeEntry e = getSqlExprNode().getIdScope().findScopeForVar(v) ; 
            if ( e == null )
                continue ;
            SqlColumn c = e.getColumn() ;
            String sqlVarName = allocSqlName(v) ;
            addProject(sqlVarName, c) ;
            addAnnotation(sqlVarName+"="+v.toString()) ;
        }
        setAnnotation() ;
    }
    
    @Override
    protected Binding assembleBinding(ResultSetJDBC rs, Binding parent)
    {
        Binding b = new BindingMap(parent) ;
        for ( Var v : getProject() )
        {
            String sqlVarName = getSqlName(v) ;
            
            if ( sqlVarName == null )
                // Not mentioned in query.
                continue ;
            try {
                // because of encoding into SPARQL terms, this is never the empty string.
                String s = rs.get().getString(sqlVarName) ;
                // Same as rs.wasNull() for things that can return Java nulls.
                if ( s == null )
                    continue ;
                // TupleLoaderSimple used SqlConstant which made the string SQL-safe
                // so it could be embedded in a non-prepared statement.  
                s = SQLUtils.unescapeStr(s) ;
                Node n = codec.decode(s) ;
                b.add(v, n) ;
                // Ignore any access error (variable requested not in results)
            } catch (SQLException ex) {}
        }
        return b ;
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