/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.serializer;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.expr.ExprVisitor ;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.query.util.IndentedWriter;

/** Serialize a query into SPARQL (or ARQ formats) into the XML syntax */

public class QuerySerializerXML implements QueryVisitor
{
    static final int INDENT = 2 ;
    FormatterTemplate fmtTemplate ;
    FormatterElement fmtElement ;
    ExprVisitor fmtExpr ;
    IndentedWriter out = null ;
    
    
    QuerySerializerXML(OutputStream outStream, SerializationContext context)
    {
        this(new IndentedWriter(outStream), context) ;
    }

    QuerySerializerXML(IndentedWriter writer, SerializationContext context)
    {
        this.out = writer ;
        //fmtTemplate = new FormatterXML() ;
        FormatterXML fmt = new FormatterXML(writer, context) ;
        fmtElement = fmt ;
        fmtTemplate = fmt ;
        fmtExpr = new FmtExprXML(writer, context) ;
    }
    
    public void startVisit(Query query)
    {
        out.println("<rdf-query>") ;
        out.incIndent(INDENT) ;
    }
    
    public void visitResultForm(Query query)  {}

    public void visitBase(Query query) {}
    
    public void visitPrefixes(Query query) { }
    
    public void visitSelectResultForm(Query query)
    {
//        if ( query.getQueryResultStar() )
//        {
//            writer.println("<select-all/>") ;
//            return ;
//        }
        
        out.print("<select") ;
        resultFormModifiers(query) ;
        out.println(">") ;
        out.incIndent(INDENT) ;
        writeVarList(query.getResultVars()) ;
        out.decIndent(INDENT) ;
        out.println("</select>") ;
    }
    
    public void visitConstructResultForm(Query query)
    {
        out.print("<construct") ;
        resultFormModifiers(query) ;
        out.println(">") ;
        
        out.incIndent(INDENT) ;
        query.getConstructTemplate().visit(fmtTemplate) ;
        out.decIndent(INDENT) ;
        
        out.println("</construct>") ;
    }
    
    public void visitDescribeResultForm(Query query)
    {
        out.print("<describe") ;
        resultFormModifiers(query) ;
        out.println(">") ;
        
        out.incIndent(INDENT) ;
        writeVarList(query.getResultVars()) ;
        writeURIList(query.getResultURIs()) ;
        out.decIndent(INDENT) ;
        
        out.println("</describe>") ;
    }
    
    public void visitAskResultForm(Query query)
    {
        out.println("<ask/>") ;
    }
    
    public void visitDatasetDecl(Query query) { }

    
    public void visitQueryPattern(Query query)
    {
        if ( query.getQueryPattern() != null )
            query.getQueryPattern().visit(fmtElement) ;
    }
    
    public void visitOrderBy(Query query)
    {
        if ( query.hasOrderBy() )
        {
            out.println("<order>") ;
            out.incIndent(INDENT) ;
            for (Iterator iter = query.getOrderBy().iterator() ; iter.hasNext() ; )
            {
                out.println("<order-by>") ;
                out.incIndent(INDENT) ;
                SortCondition sc = (SortCondition)iter.next() ;
                sc.format(fmtExpr, out) ;
                out.decIndent(INDENT) ;
                out.println("</order-by>") ;
            }
            out.decIndent(INDENT) ;
            out.println("</order>") ;
        }
    }
    
    // Done in result form.
    public void visitLimit(Query query) { }
    
    // Done in result form.
    public void visitOffset(Query query) { }
    
    public void finishVisit(Query query)
    {
        out.decIndent(INDENT) ;
        out.println("</rdf-query>") ;
        out.flush() ;
    }
    
    // ----
    
    void writeVarList(List vars)
    {
        for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
        {
            String var = (String)iter.next() ;
            out.println("<variable name=\""+var+"\"/>") ;
        }
    }
        
    void writeURIList(List uris)
    {
        for ( Iterator iter = uris.iterator() ; iter.hasNext() ; )
        {
            String uri = (String)iter.next() ;
            out.println("<uri uri=\""+uri+"\"/>") ;
        }
    }
    
    void resultFormModifiers(Query query)
    {
        if ( query.isDistinct() )
            out.print(" distinct=\"true\"") ;
        if ( query.hasLimit() )
            out.print(" limit=\""+query.getLimit()+"\"") ;
        if ( query.hasOffset() )
            out.print(" offset=\""+query.getOffset()+"\"") ;
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