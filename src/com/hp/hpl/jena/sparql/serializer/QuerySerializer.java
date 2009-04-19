/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.io.OutputStream;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Serialize a query into SPARQL or ARQ formats */

public class QuerySerializer implements QueryVisitor
{
    static final int BLOCK_INDENT = 2 ;
    protected FormatterTemplate fmtTemplate ;
    protected FormatterElement fmtElement ;
    protected FmtExpr fmtExpr ;
    protected IndentedWriter out = null ;

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

    public void visitPrologue(Prologue prologue)
    { 
        int row1 = out.getRow() ;
        PrologueSerializer.output(out, prologue) ;
        int row2 = out.getRow() ;
        if ( row1 != row2 )
            out.newline() ;
    }
    
    public void visitSelectResultForm(Query query)
    {
        out.print("SELECT ") ;
        if ( query.isDistinct() )
            out.print("DISTINCT ") ;
        if ( query.isReduced() )
            out.print("REDUCED ") ;
        out.print(" ") ; //Padding
        
        if ( query.isQueryResultStar() )
            out.print("*") ;
        else
            appendNamedExprList(query, out, query.getProject()) ;
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
            for ( String uri : query.getGraphURIs() )
            {
                out.print("FROM ") ;
                out.print(FmtUtils.stringForURI(uri, query)) ;
                out.newline() ;
            }
        }
        if ( query.getNamedGraphURIs() != null  && query.getNamedGraphURIs().size() != 0 )
        {
            for ( String uri : query.getNamedGraphURIs() )
            {
                // One per line
                out.print("FROM NAMED ") ;
                out.print(FmtUtils.stringForURI(uri, query)) ;
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

            fmtElement.visitAsGroup(el) ;
            //el.visit(fmtElement) ;
            out.decIndent(BLOCK_INDENT) ;
            out.newline() ;
        }
    }
    
    public void visitGroupBy(Query query)
    {
        if ( query.hasGroupBy() )
        {
            out.print("GROUP BY ") ;
            appendNamedExprList(query, out, query.getGroupBy()) ;
            out.println();
        }
    }

    public void visitHaving(Query query)
    {
        if ( query.hasHaving() )
        {
            out.print("HAVING") ;
            for (Expr expr : query.getHavingExprs())
            {
                out.print(" ") ;
                fmtExpr.format(expr) ;
            }
            out.println() ;
        }
    }

    public void visitOrderBy(Query query)
    {
        if ( query.hasOrderBy() )
        {
            out.print("ORDER BY ") ;
            boolean first = true ;
            for (SortCondition sc : query.getOrderBy())
            {
                if ( ! first )
                    out.print(" ") ;
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
    
    void appendVarList(Query query, IndentedWriter sb, List<String> vars)
    {
        boolean first = true ;
        for ( String varName : vars )
        {
            Var var = Var.alloc(varName) ;
            if ( ! first )
                sb.print(" ") ;
            sb.print(var.toString()) ;
            first = false ;
        }

    }
        
    void appendNamedExprList(Query query, IndentedWriter sb, VarExprList namedExprs)
    {
        boolean first = true ;
        for ( Var var : namedExprs.getVars() )
        {
            Expr expr = namedExprs.getExpr(var) ;
            if ( ! first )
                sb.print(" ") ;
            
            if ( expr != null ) 
            {
                // The following are safe to write without () 
                // Compare/merge with fmtExpr.format
                boolean needParens = true ; 
                
                if ( expr.isFunction() )
                    needParens = false ;
//                else if ( expr instanceof E_Aggregator )
//                    // Aggregators are variables (the function maps to an internal variable 
//                    // that is accesses by the E_Aggregator
//                    needParens = false ;
                else if ( expr.isVariable() )
                    needParens = false ;
                
                if ( ! Var.isAllocVar(var) )
                    // AS ==> need parens
                    needParens = true  ;
                
                if ( needParens ) 
                    out.print("(") ;
                fmtExpr.format(expr) ;
                if ( ! Var.isAllocVar(var) )
                {
                    sb.print(" AS ") ;
                    sb.print(var.toString()) ;
                }
                if ( needParens ) 
                    out.print(")") ;
            }
            else
            {
                sb.print(var.toString()) ;
            }
            first = false ;
        }
    }
    
    static void appendURIList(Query query, IndentedWriter sb, List<Node> vars)
    {
        SerializationContext cxt = new SerializationContext(query) ;
        boolean first = true ;
        for ( Node node : vars )
        {
            if ( ! first )
                sb.print(" ") ;
            sb.print(FmtUtils.stringForNode(node, cxt)) ;
            first = false ;
        }
    }
    
}


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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