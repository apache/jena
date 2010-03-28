/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.sparql.syntax.*;

/** <p> Visitor pattern helper that walks the entire tree calling operations
 * are various points in the walking process.  It is a depth first traversal.</p>
 * 
 * <p> Every visit operation is bracketted by a start/end pair makeing the
 * calling points:
 * <ul>
 * <li>start of element</li>
 * <li>end of element</li>
 * <li>start each sub element</li>
 * <li>end of each sub element</li>
 * </ul>
 * </p>
 * 
 * <p>The calls before and after subElements pass in the containing Element.
 * These calls are in addition to the start/end call on elements as
 * part of the recursive walk.</p>   
 * 
 * <p>Usage: inherit from this class and implement  startElement/endElement as needed.
 * The ElementWalker is like implementing endElement.</p>
 *
 * @author Andy Seaborne
 */

public class RecursiveElementVisitor implements ElementVisitor
{
    
    // ---- Call points.
    // Not abstract, because subclasses don't have to implement them.
    
    public void startElement(ElementTriplesBlock el) {}
    public void endElement  (ElementTriplesBlock el) {}

    public void startElement(ElementDataset el) {}
    public void endElement  (ElementDataset el) {}

    public void startElement(ElementFilter el) {} 
    public void endElement  (ElementFilter el) {} 

    public void startElement(ElementAssign el) {} 
    public void endElement  (ElementAssign el) {} 

    public void startElement(ElementUnion el) {}
    public void endElement  (ElementUnion el) {}
    public void startSubElement(ElementUnion el, Element subElt) {}
    public void endSubElement  (ElementUnion el, Element subElt) {}

    public void startElement(ElementGroup el) {}
    public void endElement  (ElementGroup el) {}
    public void startSubElement(ElementGroup el, Element subElt) {}
    public void endSubElement  (ElementGroup el, Element subElt) {}

    public void startElement(ElementOptional el) {}
    public void endElement  (ElementOptional el) {}

    public void startElement(ElementNamedGraph el) {}
    public void endElement  (ElementNamedGraph el) {}

    public void startElement(ElementService el) {}
    public void endElement  (ElementService el) {}

    public void startElement(ElementFetch el)   {}
    public void endElement  (ElementFetch el)   {}

    public void startElement(ElementExists el)  {}
    public void endElement  (ElementExists el)  {}
    
    public void startElement(ElementNotExists el) {}
    public void endElement  (ElementNotExists el) {}
    
    public void startElement(ElementMinus el) {}
    public void endElement  (ElementMinus el) {}

    public void endElement(ElementSubQuery el)      {}
    public void startElement(ElementSubQuery el)    {}

    public void endElement(ElementPathBlock el)     {}
    public void startElement(ElementPathBlock el)   {}

    protected ElementVisitor visitor = null ;
    
    // ---- 
    
    private RecursiveElementVisitor() { this.visitor = new ElementVisitorBase() ; }
    
    public RecursiveElementVisitor(ElementVisitor visitor) { this.visitor = visitor ; }
    
    // Visitor pattern on Elements
    
    public final void visit(ElementTriplesBlock el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    
    public final void visit(ElementDataset el)
    {
        startElement(el) ;
        el.getPatternElement().visit(this) ;
        endElement(el) ;
    }

    public final void visit(ElementFilter el)
    {
        startElement(el) ;
        endElement(el) ;
    }

    public void visit(ElementAssign el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    public final void visit(ElementUnion el)
    {
        startElement(el) ;
        for ( Element subElement : el.getElements() )
        {
            startSubElement(el, subElement) ;
            subElement.visit(this) ;
            endSubElement(el, subElement) ;
        }
        endElement(el) ;
    }
    
    public final void visit(ElementGroup el)
    {
        startElement(el) ;
        for ( Element subElement : el.getElements() )
        {
            startSubElement(el, subElement) ;
            subElement.visit(this) ;
            endSubElement(el, subElement) ;
        }
        endElement(el) ;
    }

    public final void visit(ElementOptional el)
    {
        startElement(el) ;
        el.getOptionalElement().visit(this) ;
        endElement(el) ;
    }


    public final void visit(ElementNamedGraph el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    public final void visit(ElementService el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }
    
    public final void visit(ElementFetch el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    
    public final void visit(ElementExists el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    public final void visit(ElementNotExists el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    public final void visit(ElementMinus el)
    {
        startElement(el) ;
        el.getMinusElement().visit(this) ;
        endElement(el) ;
    }


    
    public void visit(ElementSubQuery el)
    { 
        startElement(el) ;
        endElement(el) ;
    }
    public void visit(ElementPathBlock el)
    {
        startElement(el) ;
        endElement(el) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
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