/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.iterator.SetUtils;

public class SqlCoalesce extends SqlNodeBase1
{
    /* A COALESCE is an operations that takes 
     * variables from the left and right sides of a join
     * and finds the first (left to right) that is defined (not NULL).
     * That's COALESCE in many databases.
     */
    
    SqlJoin join ;
    Set<Var> coalesceVars ;
    Set<Var> nonCoalesceVars = new HashSet<Var>() ;
    ScopeRename idScope ;
    ScopeRename nodeScope ;
    private static final String AliasBase = AliasesSql.VarCollasce ;
    
    public static SqlCoalesce create(SDBRequest request, String alias, SqlJoin join, Set<Var>coalesceVars) 
    {
        // This is not actually true in general.
        // But at the moment, it is a restriction so we test for it for now 
        // as a snaity check. Remove the test when the new situation arises 
        // as this class then needs to be checked. 
        if ( ! join.isLeftJoin() )
            LogFactory.getLog(SqlCoalesce.class).warn("SqlCoalesce node is not a LeftJoin") ;
        
        return new SqlCoalesce(request, alias, join, coalesceVars) ;
    }
    
    private SqlCoalesce(String alias, SqlJoin join, Set<Var> coalesceVars)
    { 
        super(alias, join) ;
        this.join = join ;
        this.coalesceVars = coalesceVars ;
    }
    
    private SqlCoalesce(SDBRequest request, String alias, SqlJoin join, Set<Var> coalesceVars)
    { 
        this(alias, join, coalesceVars) ;
        Annotation1 annotation = new Annotation1(true) ;
        
        // ScopeCoalesce needed
        // Scope is:
        // new ScopeRename(oldScope, renames) ;
        // And ScopeBase ==> ScopeTable.
        
        idScope = new ScopeRename(join.getIdScope()) ;
        nodeScope = new ScopeRename(join.getNodeScope()) ;
        SqlTable table = new SqlTable(alias) ;
        
        nonCoalesceVars = SetUtils.difference(join.getIdScope().getVars(),
                                              coalesceVars) ;

        // In layout1, NodeScope is the same as IdScope
//        if ( join.getNodeScope().getVars().size() != 0 )
//            LogFactory.getLog(SqlCoalesce.class).warn("NodeScope is not empty") ;
        
        for ( Var v : coalesceVars )
        {
            String sqlColName = request.genId(AliasBase) ;
            SqlColumn col = new SqlColumn(table, sqlColName) ;
            idScope.setAlias(v, col) ;
            annotation.addAnnotation(v+" as "+col) ;
            // TODO Value
        }
        
        // Aliases.
        // Not coalesce variables.
        for ( Var v : nonCoalesceVars )
        {
            if ( coalesceVars.contains(v) )
            {
                LogFactory.getLog(SqlCoalesce.class).warn("Variable in coalesce and non-coalesce sets: "+v) ;
                continue ;
            }
            String sqlColName = request.genId(AliasBase) ;
            SqlColumn col = new SqlColumn(table, sqlColName) ;
            idScope.setAlias(v, col) ;
            annotation.addAnnotation(v+" as "+col) ;
            // TODO Value
        }
        annotation.setAnnotation(this) ;
    }
    
    public Set<Var> getCoalesceVars()       { return coalesceVars ; }
    public Set<Var> getNonCoalesceVars()    { return nonCoalesceVars ; }
    
    @Override
    public boolean      isCoalesce()    { return true ; }
    @Override
    public SqlCoalesce  asCoalesce()    { return this ; }

    @Override
    public Scope getIdScope()           { return idScope ; }
    
    @Override
    public Scope getNodeScope()         { return nodeScope ; }
    
    public SqlJoin getJoinNode()        { return join ; }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode newSubNode)
    {
        return transform.transform(this, newSubNode) ;
    }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        // May need to do a deeper copy.
        SqlCoalesce s = new SqlCoalesce(getAliasName(), subNode.asJoin(), this.coalesceVars) ;
        s.nonCoalesceVars  = this.nonCoalesceVars ;
        s.idScope = this.idScope ;
        s.nodeScope = this.nodeScope ;
        return s ;
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