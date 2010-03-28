/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;



/** An element visitor that walks the graph pattern tree, applying a visitor
 *  at each Element traversed.
 *  Only walks one levele of the query (not subqueries -- sub SELECT, (NOT)EXISTS
 *  these wil need to cakll down themselves if it is meaningful for the visitor.  
 *  Bottom-up walk - apply to subelements before applying to current element.
 * @author Andy Seaborne
 */

public class ElementWalker 
{
    // See also RecursiveElementVisitor
    
    public static void walk(Element el, ElementVisitor visitor)
    {
        walk(el, new Walker(visitor), visitor) ;
    }

    public static void walk(Element el, Walker walker, ElementVisitor visitor)
    {
        el.visit(walker) ;
    }

    
//    public void walk(Element el)
//    {
//        el.visit(new Walker(proc)) ;
//    }
    
    static public class Walker implements ElementVisitor
    {
        protected ElementVisitor proc ;
        protected Walker(ElementVisitor visitor) { proc = visitor ; }
        
        public void visit(ElementTriplesBlock el)
        {
            proc.visit(el) ;
        }
        
        public void visit(ElementFilter el)
        {
            proc.visit(el) ;
        }
        

        public void visit(ElementAssign el)
        {
            proc.visit(el) ;
        }

        
        public void visit(ElementUnion el)
        {
            for ( Element e : el.getElements() )
                e.visit(this) ;
            proc.visit(el) ;
        }
        
        public void visit(ElementGroup el)
        {
            for (Element e : el.getElements())
                e.visit(this) ;
            proc.visit(el) ;
        }
    
        public void visit(ElementOptional el)
        {
            if ( el.getOptionalElement() != null )
                el.getOptionalElement().visit(this) ;
            proc.visit(el) ;
        }
        
        public void visit(ElementDataset el)
        {
            if ( el.getPatternElement() != null )
                el.getPatternElement().visit(this) ;
            proc.visit(el) ;
        }

        public void visit(ElementNamedGraph el)
        {
            if ( el.getElement() != null )
                el.getElement().visit(this) ;
            proc.visit(el) ;
        }
    
        public void visit(ElementService el)
        {
            if ( el.getElement() != null )
                el.getElement().visit(this) ;
            proc.visit(el) ;
        }

        public void visit(ElementFetch el)
        {
            proc.visit(el) ;
        }

        // EXISTs, NOT EXISTs are really subqueries so don't automatically walk down them.
        
        public void visit(ElementExists el)
        {
//            if ( el.getElement() != null )
//                el.getElement().visit(this) ;
            proc.visit(el) ;
        }

        public void visit(ElementNotExists el)
        {
//            if ( el.getElement() != null )
//                el.getElement().visit(this) ;
            proc.visit(el) ;
        }

        public void visit(ElementMinus el)
        {
            if ( el.getMinusElement() != null )
                el.getMinusElement().visit(this) ;
            proc.visit(el) ;
        }
        
        public void visit(ElementSubQuery el)
        {
            proc.visit(el) ;
        }

        public void visit(ElementPathBlock el)
        {
            proc.visit(el) ;
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