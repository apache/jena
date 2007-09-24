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
    private SqlTable vTable ;       // Our column naming space.
    
    // --- Development/debugging.
    
    public static ScopeRename calc(Scope scope)
    {
        SqlTable table = new SqlTable("REN") ;
        Generator gen = Gensym.create("X") ;
        return calcRename(table, scope, gen) ;
    }
    //---

    public static SqlRename view(String alias, SqlNode sqlNode)
    { 
        SqlTable table = new SqlTable(alias) ;
        Generator gen = Gensym.create("X") ;    // Column names.  Not global.
        
        ScopeRename idScope = calcRename(table, sqlNode.getIdScope(), gen) ;
        ScopeRename nodeScope = calcRename(table, sqlNode.getNodeScope(), gen) ;

        SqlRename rename = new SqlRename(table, sqlNode, idScope, nodeScope) ;
        return rename ;
    }
    
    private SqlRename(SqlTable here, SqlNode sqlNode, ScopeRename idScope, ScopeRename nodeScope)
    {
        super(here.getAliasName(), sqlNode) ;
        this.vTable = here ;
        this.idScope = idScope ;
        this.nodeScope = nodeScope ;
        notes(idScope) ;
        notes(nodeScope) ;
    }

    private void notes(ScopeRename scope)
    {
        String x = "" ;
        String sep = "" ;
        for ( Var v : scope.getVars() )
        {
            SqlColumn c = scope.findScopeForVar(v).getColumn() ;
            x = x+sep+c+"="+v ;
            sep = " " ;
        }
        if ( ! x.isEmpty() )
            addNote(x) ;
    }
//    private SqlRename(String aliasName, SqlNode sqlNode, 
//                     Map<Var, String> idRenames,
//                     Map<Var, String> nodeRenames)
//    {
//        this(aliasName, sqlNode) ;
//        setAliases(here, idRenames, sqlNode.getIdScope(), idScope) ;
//        setAliases(here, nodeRenames, sqlNode.getNodeScope(), nodeScope) ;
//    }
    
    // Map all vars in the scope to names in the rename.
    private static ScopeRename calcRename(final SqlTable table, Scope scope, 
                                          final Generator gen)
    {
        ScopeRename renameScope = new ScopeRename(scope) ;
        
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
            SqlColumn col = new SqlColumn(table, colName) ;
            renameScope.setAlias(v, col) ;
        }
        return renameScope ;
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
    
    @Override
    public ScopeRename getIdScope()   { return idScope ; }
    @Override
    public ScopeRename getNodeScope() { return nodeScope ; }
    
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
        return new SqlRename(this.vTable, subNode, this.idScope, this.nodeScope) ;
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