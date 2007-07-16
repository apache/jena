/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.ScopeRename;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNodeBase;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNodeVisitor;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.util.StrUtils;

public class SqlRename extends SqlNodeBase
{
    // Alt: an SELECT node with disinct, project, order by
    // Distinct(Project(Order(Having(groupBy(restrict(joins)))))
    // 
    // Table-WHERE-GROUPBY-HAVING-ORDERBY-Project-Distinct
    
    SqlNode sqlNode ;
    ScopeRename idScope ;
    ScopeRename nodeScope ;
    SqlTable here ;

    public static SqlRename view(String alias, SqlNode sqlNode)
    { 
        Generator gen = Gensym.create("X") ;
        Map<Var, String> idRenames = mapping(sqlNode, sqlNode.getIdScope(), gen) ;
        Map<Var, String> nodeRenames = mapping(sqlNode, sqlNode.getNodeScope(), gen) ;
        return new SqlRename(alias, sqlNode, idRenames, nodeRenames) ;
    }
    
    public SqlRename(String aliasName, SqlNode sqlNode, 
                     Map<Var, String> idRenames,
                     Map<Var, String> nodeRenames)
    {
        super(aliasName) ;
        this.sqlNode = sqlNode ;
        idScope = new ScopeRename(sqlNode.getIdScope()) ;
        nodeScope = new ScopeRename(sqlNode.getNodeScope()) ;
        
        here = new SqlTable(aliasName) ;
        
        setAliases(here, idRenames, sqlNode.getIdScope(), idScope) ;
        setAliases(here, nodeRenames, sqlNode.getNodeScope(), nodeScope) ;
    }

    private void setAliases(final SqlTable here, final Map<Var, String> renames, final Scope subScope, final ScopeRename scope)
    {
        if ( renames == null )
            return ;
        
//        MapAction<Var, String> action = new MapAction<Var, String>()
//        {
//            public void apply(Var var, String colName)
//            { 
//                if ( subScope.hasColumnForVar(var) )
//                {
//                    SqlColumn col = new SqlColumn(here, colName) ;
//                    scope.setAlias(var, col) ;
//                }
//            }
//        } ;
//        MapUtils.apply(renames, action) ;
        
        for ( Var v : renames.keySet() )
        {
            String colName = renames.get(v) ;
            if ( subScope.hasColumnForVar(v) )
            {
                SqlColumn col = new SqlColumn(here, colName) ;
                scope.setAlias(v, col) ;
            }
        }
    }
    
    public void visit(SqlNodeVisitor visitor)
    {}

    @Override
    public String toString()
    {
        return StrUtils.strjoinNL(idScope.toString(),
                                  nodeScope.toString(),
                                  sqlNode.toString()) ;
    }
    
    // temp
    
    
    public Scope getIdScope()
    {
        return idScope ;
    }

    public Scope getNodeScope()
    {
        return nodeScope ;
    }
    
    // -------- Workers to build a view mapping.
    
    private static Map<Var, String> mapping(SqlNode sqlNode, Scope scope, Generator gen)
    {
        if ( scope == null )
            return null ;
        Map<Var, String> mapping = new HashMap<Var, String>() ;
        Set<Var> vars = scope.getVars() ;
        for ( Var v : vars)
            mapping.put(v, gen.next()) ;
        return mapping ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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