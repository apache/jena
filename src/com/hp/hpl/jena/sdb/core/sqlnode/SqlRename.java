/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.ScopeRename;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sparql.core.Var;

/** SQL rename */

public class SqlRename extends SqlNodeBase1
{
    private ScopeRename idScope ;
    private ScopeRename nodeScope ;
    private SqlTable here ;

    public static SqlRename view(String alias, SqlNode sqlNode)
    { 
        SqlRename rename = new SqlRename(alias, sqlNode) ;
        Generator gen = Gensym.create("X") ;

        setAliasesAll(rename.here, sqlNode.getIdScope(), rename.idScope, gen) ;
        setAliasesAll(rename.here, sqlNode.getNodeScope(), rename.nodeScope, gen) ;
        
        return rename ;
    }
    
    private SqlRename(String aliasName, SqlNode sqlNode)
    {
        this(aliasName, sqlNode,
             new SqlTable(aliasName),
             new ScopeRename(sqlNode.getIdScope()),
             new ScopeRename(sqlNode.getNodeScope())) ;
    }
    
    private SqlRename(String aliasName, SqlNode sqlNode, SqlTable here, ScopeRename idScope, ScopeRename nodeScope)
    {
        super(aliasName, sqlNode) ;
        this.here = here ;
        this.idScope = idScope ;
        this.nodeScope = nodeScope ;
    }

    
    public SqlRename(String aliasName, SqlNode sqlNode, 
                     Map<Var, String> idRenames,
                     Map<Var, String> nodeRenames)
    {
        this(aliasName, sqlNode) ;
        setAliases(here, idRenames, sqlNode.getIdScope(), idScope) ;
        setAliases(here, nodeRenames, sqlNode.getNodeScope(), nodeScope) ;
    }
    
    // Map all vars in the scope to names in the rename.
    private static void setAliasesAll(final SqlTable here, Scope scope, 
                                      final ScopeRename renameScope, final Generator gen)
    {
        if ( scope == null ) return ;
        
        // Would be nicer if Java didn't impose such an overhead on writing lambda's/
//        Action<Var> action = new Action<Var>(){
//            public void apply(Var var)
//            {
//                String colName = gen.next() ;
//                SqlColumn col = new SqlColumn(here, colName) ;
//                renameScope.setAlias(var, col) ;
//            }
//        } ;
//        Alg.apply(scope.getVars(), action) ;
            
        Set<Var> vars = scope.getVars() ;
        for ( Var v : vars)
        {
            String colName = gen.next() ;
            SqlColumn col = new SqlColumn(here, colName) ;
            renameScope.setAlias(v, col) ;
        }
    }

    // Map certain vars into the rename
    private static void setAliases(final SqlTable here, final Map<Var, String> renames,
                                   final Scope subScope, final ScopeRename scope)
    {
        if ( renames == null )
            return ;
        
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
    
    public ScopeRename getIdScopeRename()   { return idScope ; }
    
    public ScopeRename getNodeScopeRename() { return nodeScope ; }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    {
        return transform.transform(this, subNode) ;
    }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        // Do any subitems need to be copied?
        return new SqlRename(this.getAliasName(), subNode, this.here, this.idScope, this.nodeScope) ;
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