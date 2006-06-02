/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import static com.hp.hpl.jena.query.util.FmtUtils.* ;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.util.Pair;


public class SqlNodeTextVisitor implements SqlNodeVisitor
{
    // TODO Use .ensureNewLine and .atStartOfLine more
    private static final String DelimOpen = "[" ;
    private static final String DelimClose = "]" ;
    
    final static boolean closeOnSameLine = true ;
    private IndentedWriter out ;

    public SqlNodeTextVisitor(IndentedWriter out) { this.out = out ; }
    
    public void visit(SqlProject sqlNode)
    {
        start(sqlNode, "Project", null) ;
        if ( sqlNode.getCols().size() == 0 )
            out.println("<no cols>") ;
        else
        {
            boolean first = true ; 
            for ( Pair<Node, Column> c : sqlNode.getCols() )
            {
                if ( ! first ) out.print(" ") ;
                first = false ;
                out.print(stringForNode(c.car())+"/"+c.cdr().asString()) ;
            }
            out.println() ; 
        }
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    public void visit(SqlRestrict sqlNode)
    {
        start(sqlNode, "Restrict", null) ;
        out.incIndent() ;
        for ( Condition c : sqlNode.getConditions() )
        {
            out.println(c.toString()) ;
        }
        out.decIndent() ;
        
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    public void visit(SqlTable sqlNode)
    {
        if ( ! sqlNode.hasOneAnnotation() )
            addAnnotations(sqlNode) ;
        out.print(DelimOpen) ;
        out.print("Table ") ;
        out.print(sqlNode.getAliasName()) ;
        out.print(DelimClose) ;
        if ( sqlNode.hasOneAnnotation() )
            addAnnotations(sqlNode) ;
    }

    public void visit(SqlJoinInner sqlJoin)
    { visitJoin(sqlJoin) ; }
 
    public void visit(SqlJoinLeftOuter sqlJoin)
    { visitJoin(sqlJoin) ; }
    
    private void visitJoin(SqlJoin sqlJoin)
    {
        out.ensureStartOfLine() ;
        start(sqlJoin, sqlJoin.getJoinType().printName(), sqlJoin.getAliasName()) ;
        sqlJoin.getLeft().visit(this) ;
        out.println() ;
        sqlJoin.getRight().visit(this) ;
        out.println() ;
        outputConditionList(sqlJoin.getConditions()) ;
        finish() ;
    }
    
    
    private void addAnnotations(SqlNode n)
    {
        if ( n == null || !n.hasAnnotations() ) return ;
        
        if ( n.getAnnotations().size() == 1 )
        {
            out.print(" -- ") ;
            out.print(n.getAnnotations().get(0)) ;
            return ;
        }
        
        for ( String s : n.getAnnotations() )
        {
            out.ensureStartOfLine() ;
            out.print("-- ") ;
            out.println(s) ;
        }
    }
    
    private void outputConditionList(Collection<Condition>cond)
    {   
        boolean first = true ;
        for ( Condition c : cond )
        {
            if ( ! first ) out.println() ;
            first = false ;
            out.print(DelimOpen) ;
            out.print("Condition ") ;
            out.print(c.toString()) ;
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