/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;

import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

public class SqlCoalesce extends SqlJoin
{
    /* A COALESCE is a special kind of LeftJoin where some
     * variables from the left andf right sides are not equated across
     * the join but instead merged by a test for "first non NULL".  
     * That's COALESCE in many databases.
     */
    
    Set<Var> coalesceVars ;
    ScopeBase idScope ;
    ScopeBase nodeScope ;
    Generator genvar = Gensym.create("VC") ; //new Gensym("VC") ;
    
    public static SqlCoalesce merge(String alias, SqlNode left, SqlNode right, Set<Var>coalesceVars) 
    {
        // This is not actually true!
        // But at the moment, it is a restriction so we test for it for now and 
        // remove the test when the new cde arrices as the rest of the class and 
        // it's usage then needs to be checked. 
        if ( ! left.isLeftJoin() )
            LogFactory.getLog(SqlCoalesce.class).warn("Left side is not a LeftJoin") ;
        
        return new SqlCoalesce(alias, left, right, coalesceVars) ;
    }
    
    private SqlCoalesce(String alias, SqlNode left, SqlNode right, Set<Var> coalesceVars)
    { 
        super(JoinType.LEFT, right, left, alias) ;
        this.coalesceVars = coalesceVars ;
        idScope = new ScopeBase(super.getIdScope()) ;
        nodeScope = new ScopeBase(super.getNodeScope()) ;
        SqlTable table = new SqlTable("Coalesce", alias) ;
        
        for ( Var v : coalesceVars )
        {
            String sqlColName = genvar.next() ;
            SqlColumn col = new SqlColumn(table, sqlColName) ;
            idScope.setColumnForVar(v, col) ;
            // TODO Value
        }
        
    }
    
    public Set<Var> getCoalesceVars()   { return coalesceVars ; }
    
    @Override
    public boolean      isCoalesce()    { return true ; }
    @Override
    public SqlCoalesce  getCoalesce()   { return this ; }

    @Override
    public Scope getIdScope()           { return idScope ; }
    
    @Override
    public Scope getNodeScope()         { return nodeScope ; }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }
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