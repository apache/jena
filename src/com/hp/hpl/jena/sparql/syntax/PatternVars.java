/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.LinkedHashSet ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.core.Var ;

/** Get the variables potentially bound by an element.
 *  All mentioned variables except those in MINUS and FILTER (and hence NOT EXISTS)
 *  The work is done by PatternVarsVisitor.  
 */
public class PatternVars
{
    public static Set<Var> vars(Element element) { return vars(new LinkedHashSet<Var>(), element) ; }

    public static Set<Var> vars(Set<Var> s, Element element)
    {
        PatternVarsVisitor v = new PatternVarsVisitor(s) ;
        vars(element, v) ;
        return s ;
    }
    
    public static void vars(Element element, PatternVarsVisitor visitor)
    {
        ElementWalker.Walker walker = new WalkerSkipMinus(visitor) ;
        ElementWalker.walk(element, walker) ;
    }
    
    public static class WalkerSkipMinus extends ElementWalker.Walker
    {
        protected WalkerSkipMinus(ElementVisitor visitor)
        {
            super(visitor) ;
        }
        
        @Override
        public void visit(ElementMinus el)
        {
//            if ( el.getMinusElement() != null )
//                el.getMinusElement().visit(this) ;
            proc.visit(el) ;
        }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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