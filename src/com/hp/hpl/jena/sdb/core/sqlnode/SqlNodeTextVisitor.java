/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import static com.hp.hpl.jena.query.util.FmtUtils.stringForNode;

import java.util.Collection;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.IndentedWriter;

import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.util.Pair;


public class SqlNodeTextVisitor implements SqlNodeVisitor
{
    // TODO Use .ensureNewLine and .atStartOfLine more
    private static final String DelimOpen = "" ;
    private static final String DelimClose = "" ;
    
    final static boolean closeOnSameLine = true ;
    private IndentedWriter out ;
    private static final int annotationColumn = 40 ; 

    public SqlNodeTextVisitor(IndentedWriter out) { this.out = out ; }
    
    public void visit(SqlProject sqlNode)
    {
        start(sqlNode, "Project", null) ;
        if ( sqlNode.getCols().size() == 0 )
            out.println("<no cols>") ;
        else
        {
            boolean first = true ; 
            String currentPrefix = null ; 
            out.incIndent() ;
            for ( Pair<Var, SqlColumn> c : sqlNode.getCols() )
            {
                if ( ! first ) out.print(" ") ;
                first = false ;
                
                String a = null ;
                String b = "<null>" ;
                if ( c.car() != null )
                    a = stringForNode(c.car().asNode()) ;
                if ( c.cdr() != null )
                    b = c.cdr().asString() ;
                
                if ( a == null )
                {
                    out.print(b) ;
                    currentPrefix = null ;
                }
                else
                {
                    // Var name formatting. 
                    String x[] = a.split("\\"+SQLUtils.getSQLmark()) ;
                    if ( currentPrefix != null && ! x[0].equals(currentPrefix) )
                        out.println() ;
                    currentPrefix = x[0] ;
                    out.print(a+"/"+b) ;
                }
            }
            out.decIndent() ;
            out.println() ; 
        }
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    public void visit(SqlRestrict sqlNode)
    {
        start(sqlNode, "Restrict", null) ;
        out.incIndent() ;
        for ( SqlExpr c : sqlNode.getConditions() )
        {
            out.println(c.toString()) ;
        }
        out.decIndent() ;
        
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    public void visit(SqlTable sqlNode)
    {
//        if ( ! sqlNode.hasOneNote() )
//            addAnnotations(sqlNode) ;
        out.print(DelimOpen) ;
        out.print("Table ") ;
        out.print(sqlNode.getAliasName()) ;
        out.print(DelimClose) ;
//        if ( sqlNode.hasOneNote() )
            addAnnotations(sqlNode) ;
    }

    public void visit(SqlJoinInner sqlJoin)
    { visitJoin(sqlJoin) ; }
 
    public void visit(SqlJoinLeftOuter sqlJoin)
    { visitJoin(sqlJoin) ; }
    
    public void visit(SqlCoalesce sqlNode)
    {
        start(sqlNode, "Coalesce", sqlNode.getAliasName()) ;

        boolean first = true ;
        for ( Var v : sqlNode.getCoalesceVars()  )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            out.print(v.toString()) ;
            SqlColumn leftCol = sqlNode.getLeft().getIdScope().getColumnForVar(v) ;
            SqlColumn rightCol = sqlNode.getRight().getIdScope().getColumnForVar(v) ;
            out.print("["+leftCol+"/"+rightCol+"]") ;
        }
        
        sqlNode.getLeft().visit(this) ;
        out.println() ;
        sqlNode.getRight().visit(this) ;
        outputConditionList(sqlNode.getConditions()) ;
        finish() ;
    }

    int depth = 0 ;
    
    private void visitJoin(SqlJoin sqlJoin)
    {
        depth ++ ;
        out.ensureStartOfLine() ;
        start(sqlJoin, sqlJoin.getJoinType().printName(), sqlJoin.getAliasName()) ;
        sqlJoin.getLeft().visit(this) ;
        out.println() ;
        sqlJoin.getRight().visit(this) ;
        outputConditionList(sqlJoin.getConditions()) ;
        finish() ;
        depth -- ;
    }
    
    
    private void addAnnotations(Annotations n)
    {
        if ( n == null || !n.hasNotes() ) return ;
        
        boolean first = true ;
        
//        if ( n.getNotes().size() == 1 )
//        {
//            out.pad(annotationColumn) ;
//            out.print(" -- ") ;
//            out.print(n.getNotes().get(0)) ;
//            return ;
//        }
        
        for ( String s : n.getNotes() )
        {
            if ( !first )
                out.ensureStartOfLine() ;
            first = false ;
            out.pad(annotationColumn, true ) ;
            out.print(" -- ") ;
            out.print(s) ;
        }
    }
    
    private void outputConditionList(Collection<SqlExpr>cond)
    {   
        boolean first = true ;
        for ( SqlExpr c : cond )
        {
            out.println() ;
            first = false ;
            out.print(DelimOpen) ;
            out.print("Condition ") ;
            out.print(c.toString()) ;
            addAnnotations(c) ;
            out.print(DelimClose) ;
        }
    }

    
    private void start(SqlNode sqlNode, String label, String alias)
    {
        out.print(DelimOpen) ;
        out.print(label) ;
        if ( alias != null )
        {
            out.print("/") ;
            out.print(alias) ;
        }
        addAnnotations(sqlNode) ;
        out.incIndent() ;
        out.println() ;
    }
    
    private void finish()
    {
        if ( ! closeOnSameLine )
            out.println() ;
        out.print(DelimClose) ;
        out.decIndent() ;
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