/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Serialize a query into SPARQL (or ARQ formats) into an abstract syntax */

public class QuerySerializerPrefix implements QueryVisitor
{
    protected FormatterTemplate fmtTemplate ;
    protected FormatterElement fmtElement ;
    protected FmtExpr fmtExpr ;
    protected IndentedWriter out = null ;

    static final int INDENT = 2 ;
    
    static final boolean closingBracketOnSameLine = true ; 
    
    QuerySerializerPrefix(OutputStream outStream, SerializationContext context)
    {
        this(new IndentedWriter(outStream), context) ;
    }

    QuerySerializerPrefix(IndentedWriter iwriter, SerializationContext context)
    {
        FormatterPrefix fmt = new FormatterPrefix(iwriter, context) ;
        fmtElement = fmt ;
        fmtTemplate = fmt ;
        fmtExpr = new FmtExprPrefix(iwriter, context) ;
        out = iwriter ;
    }
    
    public void startVisit(Query query)
    {
        out.println("(query ") ;
        out.incIndent(INDENT) ;
    }
    
    public void finishVisit(Query query)
    {
        out.decIndent(INDENT) ;
        out.println(")") ;
        out.flush() ;
    }

    public void visitResultForm(Query query)  {}

    
    public void visitPrologue(Prologue prologue)
    {
        printBase(prologue) ;
        printPrefixes(prologue) ;
    }
    
    private void printBase(Prologue prologue)
    {
        if ( prologue.explicitlySetBaseURI() && prologue.getBaseURI() != null )
            out.println("(base <"+prologue.getBaseURI()+">)") ;
    }
    
    private void printPrefixes(Prologue prologue)
    {
        if ( prologue.getPrefixMapping() == null )
            return ;
        Map pmap = prologue.getPrefixMapping().getNsPrefixMap() ;
        for ( Iterator iter = pmap.keySet().iterator() ; iter.hasNext() ; )
        {
            String k = (String)iter.next() ;
            String v = (String)pmap.get(k) ;
            out.println("(prefix "+k+": <"+v+">)") ;
        }
    }
    
    public void visitSelectResultForm(Query query)
    {
        
        out.print("(select") ;
        if ( query.isQueryResultStar() )
            out.print(" *") ;
        else
            writeVarList(query.getResultVars()) ;
        out.println(")") ;
        resultFormModifiers(query) ;
    }
    
    public void visitConstructResultForm(Query query)
    {
        out.print("(construct") ;
        out.println() ;
        out.incIndent(INDENT) ;
        
        query.getConstructTemplate().visit(fmtTemplate) ;
        out.decIndent(INDENT) ;
        out.println(")") ;
        resultFormModifiers(query) ;
    }
    
    public void visitDescribeResultForm(Query query)
    {
        out.print("(describe") ;
        writeVarList(query.getResultVars()) ;
        writeNodeList(query.getResultURIs()) ;
        out.println(")") ;
        resultFormModifiers(query) ;
    }
    
    public void visitAskResultForm(Query query)
    {
        out.print("(ask") ;
        out.incIndent(INDENT) ;
        out.decIndent(INDENT) ;
        out.println(")") ;
        resultFormModifiers(query) ;
    }
    
    public void visitDatasetDecl(Query query)
    {
        for ( Iterator iter = query.getGraphURIs().iterator() ; iter.hasNext() ; )
        {
            String graphIRI = (String)iter.next();
            String $ = FmtUtils.stringForNode(Node.createURI(graphIRI), query.getPrefixMapping()) ;
            out.println("(from "+$+")") ;
        }
        for ( Iterator iter = query.getNamedGraphURIs().iterator() ; iter.hasNext() ; )
        {
            String graphIRI = (String)iter.next();
            String $ = FmtUtils.stringForNode(Node.createURI(graphIRI), query.getPrefixMapping()) ;
            out.println("(from named "+$+")") ;
        }
    }

    
    public void visitQueryPattern(Query query)
    {
        if ( query.getQueryPattern() != null )
        {
            query.getQueryPattern().visit(fmtElement) ;
            out.println() ;
        }
    }
    
    // Done in resultFormModifiers
    public void visitOrderBy(Query query)
    { }
    
    // Done in resultFormModifiers
    public void visitOffset(Query query) { }

    // Done in resultFormModifiers
    public void visitLimit(Query query) { }
    
//    private void printClose()
//    {
//        if ( closingBracketOnSameLine )
//            out.print(")") ;
//        else
//        {
//            out.println("") ;
//            out.print(")") ;
//        }
//    }
    
    private void writeVarList(List vars)
    {
        for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
        {
            String var = (String)iter.next() ;
            out.print(" ?"+var) ;
            //out.println("(var ?"+var+")") ;
        }
    }
        
    private void writeNodeList(List nodes)
    {
        for ( Iterator iter = nodes.iterator() ; iter.hasNext() ; )
        {
            Node n = (Node)iter.next();
            out.print(" ") ;
            out.print(FmtUtils.stringForNode(n)) ;
        }
    }
    
    private void resultFormModifiers(Query query)
    {
        // Order, projection, distinct, offset, limit
        // Exception projection is done at the end so it looks natural.
        
        //out.incIndent(INDENT) ;
        
        if ( query.hasOrderBy() )
        {
            for (Iterator iter = query.getOrderBy().iterator() ; iter.hasNext() ; )
            {
                out.print("(order by ") ;
                SortCondition sc = (SortCondition)iter.next() ;
                sc.formatPrefix(fmtExpr.getVisitor(), out) ;
                out.println(")") ;
            }
        }

        if ( query.isDistinct() )
            out.println("(distinct true)") ;
        
        if ( query.isReduced() )
            out.println("(reduced true)") ;

        if ( query.hasOffset() )
            out.println("(offset "+query.getOffset()+")") ;

        if ( query.hasLimit() )
            out.println("(limit "+query.getLimit()+")") ;
        
        //out.decIndent(INDENT) ;
    }
}


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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