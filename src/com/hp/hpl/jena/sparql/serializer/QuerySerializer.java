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
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

/** Serialize a query into SPARQL or ARQ formats */

public class QuerySerializer implements QueryVisitor
{
    static final int BLOCK_INDENT = 2 ;
    protected FormatterTemplate fmtTemplate ;
    protected FormatterElement fmtElement ;
    protected FmtExpr fmtExpr ;
    protected IndentedWriter out = null ;

//    protected QuerySerializer(IndentedWriter _out, SerializationContext context)
//    {
//        out = _out ;
//    
//        SerializationContext cxtElt = new SerializationContext(context) ;
//        cxtElt.getBNodeMap().setBNodesAsFakeURIs(ARQ.enableConstantBNodeLabels) ;
//        fmtElement = new FmtElementARQ(out, cxtElt) ;
//
//        
//        SerializationContext cxtTmp = new SerializationContext(context) ;
//        cxtTmp.getBNodeMap().setBNodesAsFakeURIs(false) ;
//        fmtTemplate = new FmtTemplateARQ(out, cxtTmp) ;
//        
//        fmtExpr = new FmtExprARQ(out, cxtElt) ;
//    }

    
    QuerySerializer(OutputStream _out,
                    FormatterElement   formatterElement, 
                    FmtExpr            formatterExpr,
                    FormatterTemplate  formatterTemplate)
    {
        this(new IndentedWriter(_out),
             formatterElement, formatterExpr, formatterTemplate) ;
    }

    QuerySerializer(IndentedWriter iwriter,
                    FormatterElement   formatterElement, 
                    FmtExpr            formatterExpr,
                    FormatterTemplate  formatterTemplate)
    {
        out = iwriter ;
        fmtTemplate = formatterTemplate ;
        fmtElement = formatterElement ;
        fmtExpr = formatterExpr ;
    }
    
    
    
    public void startVisit(Query query)  {}
    
    public void visitResultForm(Query query)  {}

    public void visitBase(Query query)
    {
        if ( query.getBaseURI() != null && query.explicitlySetBaseURI() )
        {
            out.print("BASE    ") ;
            out.print("<"+query.getBaseURI()+">") ;
            out.newline() ;
        }
    }
    
    public void visitPrefixes(Query query)
    {
        Map pmap = null ;
        
        if ( query.getPrefixMapping() instanceof PrefixMapping2 )
        {
            PrefixMapping2 pm2 = (PrefixMapping2)query.getPrefixMapping() ;
            pmap = pm2.getNsPrefixMap(false) ;
        }
        else
            pmap = query.getPrefixMapping().getNsPrefixMap() ;
        
        if ( pmap.size() > 0 )
        {
            //boolean first = true ;
            for ( Iterator iter = pmap.keySet().iterator() ; iter.hasNext() ; )
            {
                String k = (String)iter.next() ;
                String v = (String)pmap.get(k) ;
                out.print("PREFIX  ") ;
                out.print(k) ;
                out.print(':') ;
                out.print(' ', -k.length()) ;
                // Include at least one space 
                out.print(" <"+v+">") ;
                out.newline() ;
            }
            out.newline() ;
        }
    }
    
    public void visitSelectResultForm(Query query)
    {
        out.print("SELECT ") ;
        if ( query.isDistinct() )
            out.print("DISTINCT ") ;
        out.print(" ") ; //Padding
        
        if ( query.isQueryResultStar() )
            out.print("*") ;
        else
        {
            appendVarList(query, out, query.getResultVars()) ;
            //appendURIList(query, sb, resultURIs) ;
        }
        out.newline() ;
    }
    
    public void visitConstructResultForm(Query query)
    {
        out.print("CONSTRUCT ") ;
        if ( query.isQueryResultStar() )
        {
            out.print("*") ;
            out.newline() ;
        }
        else
        {
            out.incIndent(BLOCK_INDENT) ;
            out.newline() ;
            Template t = query.getConstructTemplate() ;
            t.visit(fmtTemplate) ;
            out.decIndent(BLOCK_INDENT) ;
        }
    }
    
    public void visitDescribeResultForm(Query query)
    {
        out.print("DESCRIBE ") ;
        
        if ( query.isQueryResultStar() )
            out.print("*") ;
        else
        {
            appendVarList(query, out, query.getResultVars()) ;
            if ( query.getResultVars().size() > 0 &&
                 query.getResultURIs().size() > 0 )
                out.print(" ") ;
            appendURIList(query, out, query.getResultURIs()) ;
        }
        out.newline() ;
    }
    
    public void visitAskResultForm(Query query)
    {
        out.print("ASK") ;
        out.newline() ;
    }
    
    public void visitDatasetDecl(Query query)
    {
        if ( query.getGraphURIs() != null && query.getGraphURIs().size() != 0 )
        {
            for ( Iterator iter = query.getGraphURIs().iterator() ; iter.hasNext() ; )
            {
                String uri = (String)iter.next() ;
                out.print("FROM ") ;
                out.print(FmtUtils.stringForURI(uri, query.getPrefixMapping())) ;
                out.newline() ;
            }
        }
        if ( query.getNamedGraphURIs() != null  && query.getNamedGraphURIs().size() != 0 )
        {
            for ( Iterator iter = query.getNamedGraphURIs().iterator() ; iter.hasNext() ; )
            {
                // One per line
                String uri = (String)iter.next() ;
                out.print("FROM NAMED ") ;
                out.print(FmtUtils.stringForURI(uri, query.getPrefixMapping())) ;
                out.newline() ;
            }
        }
    }
    
    public void visitQueryPattern(Query query)
    {
        if ( query.getQueryPattern() != null )
        {
            out.print("WHERE") ;
            out.incIndent(BLOCK_INDENT) ;
            out.newline() ;
            
            Element el = query.getQueryPattern() ;

            if ( fmtElement.topMustBeGroup() )
            {
                if ( ! ( el instanceof ElementGroup ) )
                {
                    ElementGroup elg = new ElementGroup() ;
                    elg.addElement(el) ;
                    el = elg ;
                }
            }
            el.visit(fmtElement) ;
            out.decIndent(BLOCK_INDENT) ;
            out.newline() ;
        }
    }
    
    public void visitOrderBy(Query query)
    {
        if ( query.hasOrderBy() )
        {
            out.print("ORDER BY ") ;
            boolean first = true ;
            for (Iterator iter = query.getOrderBy().iterator() ; iter.hasNext() ; )
            {
                if ( ! first )
                    out.print(" ") ;
                SortCondition sc = (SortCondition)iter.next() ;
                sc.format(fmtExpr, out) ;
                first = false ;
            }
            out.println() ;
        }
    }
    
    
    public void visitLimit(Query query)
    {
        if ( query.hasLimit() )
        {
            out.print("LIMIT   "+query.getLimit()) ;
            out.newline() ; 
        }
    }
    
    public void visitOffset(Query query)
    {
        if ( query.hasOffset() )
        {
            out.print("OFFSET  "+query.getOffset()) ;
            out.newline() ; 
        }
    }
    
    public void finishVisit(Query query)
    {
        out.flush() ;
    }
    
    // ----
    
    static void appendVarList(Query query, IndentedWriter sb, List vars)
    {
        boolean first = true ;
        for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
        {
            String var = (String)iter.next() ;
            if ( ! first )
                sb.print(" ") ;
            sb.print("?") ;
            sb.print(var) ;
            first = false ;
        }
    }
        
    static void appendURIList(Query query, IndentedWriter sb, List vars)
    {
        SerializationContext cxt = new SerializationContext(query) ;
        boolean first = true ;
        for ( Iterator iter = vars.iterator() ; iter.hasNext() ; )
        {
            Node node = (Node)iter.next() ;
            if ( ! first )
                sb.print(" ") ;
            sb.print(FmtUtils.stringForNode(node, cxt)) ;
            first = false ;
        }
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