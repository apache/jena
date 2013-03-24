/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.syntax;


/** <p> Visitor pattern helper that walks the entire tree calling operations
 * are various points in the walking process.  It is a depth first traversal.</p>
 * 
 * <p> Every visit operation is bracketted by a start/end pair makeing the
 * calling points:
 * <ul>
 * <li>start of element</li>
 * <li>start each sub element</li>
 * <li>end of each sub element</li>
 * <li>end of element</li>
 * </ul>
 * </p>
 * 
 * <p>The calls before and after subElements pass in the containing Element.
 * These calls are in addition to the start/end call on elements as
 * part of the recursive walk.</p>   
 * 
 * <p>Usage: inherit from this class and implement  startElement/endElement as needed.
 * The ElementWalker is like implementing endElement.</p> */

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

    public void startElement(ElementBind el) {} 
    public void endElement  (ElementBind el) {} 

    public void startElement(ElementData el) {} 
    public void endElement  (ElementData el) {} 

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
    
    @Override
    public final void visit(ElementTriplesBlock el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    
    @Override
    public final void visit(ElementDataset el)
    {
        startElement(el) ;
        el.getPatternElement().visit(this) ;
        endElement(el) ;
    }

    @Override
    public final void visit(ElementFilter el)
    {
        startElement(el) ;
        endElement(el) ;
    }

    @Override
    public void visit(ElementAssign el)
    {
        startElement(el) ;
        endElement(el) ;
    }

    @Override
    public void visit(ElementBind el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    
    @Override
    public void visit(ElementData el)
    {
        startElement(el) ;
        endElement(el) ;
    }
    
    @Override
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
    
    @Override
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

    @Override
    public final void visit(ElementOptional el)
    {
        startElement(el) ;
        el.getOptionalElement().visit(this) ;
        endElement(el) ;
    }


    @Override
    public final void visit(ElementNamedGraph el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    @Override
    public final void visit(ElementService el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }
    
    @Override
    public final void visit(ElementExists el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    @Override
    public final void visit(ElementNotExists el)
    {
        startElement(el) ;
        el.getElement().visit(this) ;
        endElement(el) ;
    }

    @Override
    public final void visit(ElementMinus el)
    {
        startElement(el) ;
        el.getMinusElement().visit(this) ;
        endElement(el) ;
    }


    
    @Override
    public void visit(ElementSubQuery el)
    { 
        startElement(el) ;
        endElement(el) ;
    }
    @Override
    public void visit(ElementPathBlock el)
    {
        startElement(el) ;
        endElement(el) ;
    }
}
