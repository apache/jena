/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.syntax;

import java.util.*;

import com.hp.hpl.jena.query.core.LabelMap;
import com.hp.hpl.jena.query.core.VarsMentionedVisitor;
import com.hp.hpl.jena.query.serializer.FormatterARQ;

/** Element - abstract class for all pattern elements 
 * 
 * @author Andy Seaborne
 * @version $Id: Element.java,v 1.29 2007/01/08 18:25:41 andy_seaborne Exp $
 */

public abstract class Element
{

    public Set varsMentioned()
    {
        LinkedHashSet s = new LinkedHashSet() ;
        ElementVisitor v = new VarsMentionedVisitor(s) ;
        ElementWalker.walk(this, v) ;
        return s ; 
    }
    
    public abstract void visit(ElementVisitor v) ;
    
    public abstract int hashCode() ;
    public abstract boolean equalTo(Element el2, LabelMap labelMap) ;
    
    final public boolean equals(Object el2)
    { 
        if ( this == el2 ) return true ;

        if ( ! ( el2 instanceof Element ) )
            return false ;
        return equalTo((Element)el2, null) ; }
    
    
    public String toString()
    {
        return FormatterARQ.asString(this) ;
    }
    
    // Constants used in hashing to stop an element and it's subelement
    // (if just one) having the same hash.
    
    static final int HashBasicGraphPattern    = 0xA1 ;
    static final int HashGroup                = 0xA2 ;
    static final int HashUnion                = 0xA3 ;
    static final int HashOptional             = 0xA4 ;
    // static final int HashGraph                = 0xA5 ; // Nor needed
    static final int HashUnsaid               = 0xA6 ;
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