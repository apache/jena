/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** com.hp.hpl.jena.query.core.FormatterARQ
 * 
 * @author Andy Seaborne
 * @version $Id: FormatterPlain.java,v 1.31 2007/01/31 17:41:17 andy_seaborne Exp $
 */

public class FormatterPlain extends FormatterBase
    implements FormatterElement, FormatterTemplate 
{
    // Consider inheriting from FormatterPrefix and setting open/close markers to ""
    // Just the label case that differs then.
    // Add a map of Element.class => label inFormatterPrefix.
    static int INDENT = FormatterElement.INDENT ;
    
    public FormatterPlain(IndentedWriter out, SerializationContext context)
    {
        super(out, context) ;
    }
    
    public static void format(IndentedWriter out, SerializationContext cxt, Element el)
    {
        FormatterPlain fmt = new FormatterPlain(out, cxt) ;
        fmt.startVisit() ;
        el.visit(fmt) ;
        fmt.finishVisit() ;
    }
    
    
    public static String asString(Element el)
    {
        SerializationContext cxt = new SerializationContext() ;
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        FormatterPlain.format(b.getIndentedWriter(), cxt, el) ;
        return b.toString() ;
    }

    public boolean topMustBeGroup() { return false ; }
    
    private void format(Triple t)
    {
        out.print("TriplePattern") ;
        out.print(" ") ;
        formatTriple(t) ;
        //out.newline() ;
    }
    
    public void visit(ElementTriplesBlock el)
    {
        out.print("BasicGraphPattern") ;
        out.incIndent(INDENT) ;
        for ( Iterator iter = el.getTriples().iterator() ; iter.hasNext(); )
        {
            out.newline() ;
            Triple t = (Triple)iter.next();
            format(t) ;
        }
        out.decIndent(INDENT) ;
    }
    
    public void visit(ElementDataset el)
    {
        out.print("Block") ;
        out.incIndent(INDENT) ;
        out.newline() ;
        if ( el.getDataset() != null )
            out.print("dataset") ;
        el.getPatternElement().visit(this) ;
        out.decIndent(INDENT) ;
    }

    public void visit(ElementFilter el)
    {
        //writer.newline() ;
        out.print("Constraint") ;
        out.incIndent(INDENT) ;
        out.newline() ;
        FmtExprPrefix.format(out, context, el.getExpr()) ;
        out.decIndent(INDENT) ;
    }
    
    public void visit(ElementUnion el)
    {
        multiElement("Union", el.getElements().iterator()) ;
    }
    public void visit(ElementGroup el)
    {
        multiElement("Group", el.getElements().iterator()) ;
    }

    public void visit(ElementOptional el)
    {
        singleElement("Optional", el.getOptionalElement()) ;
    }
    
    public void visit(ElementNamedGraph el)
    {
        singleElement("Graph", el.getElement()) ;
    }
    
    public void visit(ElementUnsaid el)
    {
        singleElement("Unsaid", el.getElement()) ;
    }
    
    // ---- Worker functions
    
    private void singleElement(String name, Element element)
    {
        out.print(name) ;
        out.incIndent(INDENT) ;
        out.newline() ;
        element.visit(this) ;
        out.decIndent(INDENT) ;

    }
    
//    private void doubleElement(String name, Element el_1, Element el_2)
//    {
//        out.print(name) ;
//        out.incIndent(INDENT) ;
//        el_1.visit(this) ;
//        out.newline() ;
//        el_2.visit(this) ;
//        out.decIndent(INDENT) ;
//    }
    
    private void multiElement(String name, Iterator iter)
    {
        out.print(name) ;
        out.incIndent(INDENT) ;
        for ( ; iter.hasNext() ; )
        {
            out.newline() ;
            Element element = (Element)iter.next() ;
            element.visit(this) ;
        }
        out.decIndent(INDENT) ;
    }
    
    public void visit(TemplateTriple template)
    {
        Triple t = template.getTriple() ;
        formatTriple(t) ;
    }

    public void visit(TemplateGroup tGroup)
    {
        out.print("Template Group") ;
        out.incIndent(INDENT) ;
       
        for ( Iterator iter = tGroup.templates() ; iter.hasNext() ;)
        {
            out.newline() ;
            Template sub = (Template)iter.next() ;
            sub.visit(this) ;
        }
        out.decIndent(INDENT) ;
        out.println() ;
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