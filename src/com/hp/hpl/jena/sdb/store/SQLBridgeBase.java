/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Aliases;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.util.Pair;

/** Convert from whatever results a particular layout returns into
 *  an ARQ QueryIterator of Bindings.  An SQLBridge object
 *  is allocated for each query execution. 
 *  
 * @author Andy Seaborne
 * @version $Id$
 */  

public abstract class SQLBridgeBase implements SQLBridge
{
    private Generator vargen = new Gensym(Aliases.VarBase) ;
    
    // Always need to allocate a label for a column 
    // because can't access by table.column
    private Map<Var, String>varLabels = new HashMap<Var, String>() ;
    
    private Collection<Var> projectVars = null ;
    private SqlNode sqlNodeOriginal = null ;
    private SqlNode sqlNode = null ;                 // Subclass can mutate
    private StringBuilder annotation = new StringBuilder() ;
    
    protected SQLBridgeBase() { }
    
    // Delayed constructor
    public void init(SqlNode sqlNode, Collection<Var> projectVars)
    {
        setProjectVars(projectVars) ;
        setSqlNode(sqlNode) ;
    }
    
    private void setSqlNode(SqlNode sNode)
    { 
        if ( sqlNodeOriginal != null )
            throw new SDBException("SQLBridgeBase: SQL node already set") ;
        this.sqlNodeOriginal = sNode ;
        this.sqlNode = SqlProject.project(sNode) ;
    }
        
    private void setProjectVars(Collection<Var> projectVars)
    {
        if ( this.projectVars != null )
            throw new SDBException("SQLBridgeBase: Project vars already set") ;
        this.projectVars = projectVars ;
    }
    
    protected SqlNode getSqlExprNode() { return sqlNodeOriginal ; }
    protected SqlNode getProjectNode() { return sqlNode ; }
    
    protected void addProject(Var v, SqlColumn col)
    {
        // v is null if there is no renaming going on.
        sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(v,  col)) ;
    }
    
    protected void addProject(SqlColumn col)
    { 
        sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(null,  col)) ;
    }
    
    protected void addAnnotation(String note)
    {
        if ( annotation.length() > 0 )
            annotation.append(" ") ;
        annotation.append(note) ;
    }
    
    protected void setAnnotation()
    {
        if ( annotation.length() > 0 )
            getProjectNode().addNote(annotation.toString()) ;
    }
    
    protected Collection<Var> getProject() { return projectVars ; }
    
    protected String allocSqlName(Var v)
    {
        String sqlVarName = varLabels.get(v) ;
        if ( sqlVarName == null )
        {
            sqlVarName = vargen.next() ;
            varLabels.put(v, sqlVarName) ;
        }
        return sqlVarName ;
    }
    
    protected String getSqlName(Var v) { return varLabels.get(v) ; }
    
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