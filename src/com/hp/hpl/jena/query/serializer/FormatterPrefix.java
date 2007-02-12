/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.serializer;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.expr.Expr ;
import com.hp.hpl.jena.query.expr.ExprVisitor ;
import com.hp.hpl.jena.query.syntax.*;
import com.hp.hpl.jena.query.util.IndentedLineBuffer;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.FmtUtils;

/** com.hp.hpl.jena.query.core.FormatterARQ
 * 
 * @author Andy Seaborne
 * @version $Id: FormatterPrefix.java,v 1.18 2007/02/05 17:11:29 andy_seaborne Exp $
 */

public class FormatterPrefix extends FormatterBase
    implements FormatterElement, FormatterTemplate 
{
    static int INDENT = FormatterElement.INDENT ;

    static final boolean closingBracketOnSameLine = QuerySerializerPrefix.closingBracketOnSameLine ; 
    static final boolean allowDoubles = true ;
    
    protected String openMarker = "(" ;
    protected String closeMarker = ")" ;

    public FormatterPrefix(IndentedWriter out, SerializationContext context)
    {
        super(out, context) ;
    }
    
    public static void format(IndentedWriter out, SerializationContext cxt, Element el)
    {
        FormatterPrefix fmt = new FormatterPrefix(out, cxt) ;
        fmt.startVisit() ;
        el.visit(fmt) ;
        fmt.finishVisit() ;
    }
    
    
    public static String asString(Element el)
    {
        SerializationContext cxt = new SerializationContext() ;
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        FormatterPrefix.format(b.getIndentedWriter(), cxt, el) ;
        return b.toString() ;
    }

    public boolean topMustBeGroup() { return false ; }
    
    private void format(Triple t)
    {
        printOpen() ;
        out.print("triplepattern") ;
        out.print(" ") ;
        formatTriple(t) ;
        printClose() ;
    }
    
    public void visit(ElementTriplesBlock el)
    {
        printOpen() ;
        out.print("basicgraphpattern") ;
        out.incIndent(INDENT) ;
        for ( Iterator iter = el.getTriples().iterator() ; iter.hasNext(); )
        {
            out.newline() ;
            Triple t = (Triple)iter.next();
            format(t) ;
        }
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    // This format is primary to represent the abstract SPARQL tree the parser builds.
    // See the "plain" format for a slightly different appearnance that is a more literal
    // representation of the ARQ abstract parse tree.
    
    static final boolean hideBlocks = true ;
    
    public void visit(ElementDataset el)
    {
        if ( hideBlocks )
        {
            el.getPatternElement().visit(this) ;
            return ;
        }
        
        String label = "block" ;
        if ( el.getDataset() != null )
            label = "block/dataset" ;
        singleElement(label, el.getPatternElement()) ;
    }

    public void visit(ElementFilter el)
    {
        printOpen() ;
        out.print("filter") ;
        out.incIndent(INDENT) ;
        out.print(" ") ;
        FmtExprPrefix.format(out, context, el.getExpr()) ;
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    public void visit(ElementUnion el)
    {
        multiElement("union", el.getElements().iterator()) ;
    }
    public void visit(ElementGroup el)
    {
        multiElement("group", el.getElements().iterator()) ;
    }

    public void visit(ElementOptional el)
    {
        singleElement("optional", el.getOptionalElement()) ;
    }
    
    public void visit(ElementNamedGraph el)
    {
        printOpen() ;
        out.print("graph") ;
        out.print(" ") ;
        out.print(slotToString(el.getGraphNameNode())) ;
        out.incIndent(INDENT) ;
        out.newline() ;
        el.getElement().visit(this) ;
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    public void visit(ElementUnsaid el)
    {
        singleElement("unsaid", el.getElement()) ;
    }
    
    public void visit(ElementExtension el)
    {
        printOpen() ;
        out.print("ext") ;
        out.print(" ") ;
        String uri = el.getURI() ;
        String tmp = FmtUtils.stringForURI(uri, context.getPrefixMapping()) ;
        out.print(tmp) ;
        out.print("(") ;
        
        for ( int i = 1 ; ; i++ )  
        {
            Expr expr = el.getArg(i) ;
            if ( expr == null )
                break ; 
            if ( i != 1 )
                out.print(", ") ;
            ExprVisitor v = new FmtExprARQ(out, context) ;
            expr.visit(v) ;
        }
        out.print(")") ;
        out.print(" )") ;
        //printClose() ;
    }
    
    // ---- Worker functions
    
    private void singleElement(String name, Element element)
    {
        printOpen() ;
        out.print(name) ;
        out.incIndent(INDENT) ;
        out.newline() ;
        element.visit(this) ;
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    private void doubleElement(String name, Element el_1, Element el_2)
    {
        printOpen() ;
        out.print(name) ;
        out.println() ;
        out.incIndent(INDENT) ;
        el_1.visit(this) ;
        out.newline() ;
        el_2.visit(this) ;
        out.decIndent(INDENT) ;
        printClose() ;

    }
    
    private void multiElement(String name, Iterator iter)
    {
        printOpen() ;
        out.print(name) ;
        out.incIndent(INDENT) ;
        for ( ; iter.hasNext() ; )
        {
            out.newline() ;
            Element element = (Element)iter.next() ;
            element.visit(this) ;
        }
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    public void visit(TemplateTriple template)
    {
        printOpen() ;
        out.print("triplepattern") ;
        out.print(" ") ;
        formatTriple(template.getTriple()) ;
        printClose() ;
    }

    public void visit(TemplateGroup tGroup)
    {
        printOpen() ;
        out.print("templategroup") ;
        out.incIndent(INDENT) ;
        for ( Iterator iter = tGroup.templates() ; iter.hasNext() ;)
        {
            out.newline() ;
            Template sub = (Template)iter.next() ;
            sub.visit(this) ;
        }
        out.decIndent(INDENT) ;
        printClose() ;
    }
    
    private void printOpen() { out.print(openMarker) ; }
    
    private void printClose()
    {
        if ( ! closingBracketOnSameLine )
            out.println() ;
        out.print(closeMarker) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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