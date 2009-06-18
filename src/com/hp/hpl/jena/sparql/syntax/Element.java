/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.*;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.serializer.FormatterElement;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** Element - abstract class for all pattern elements 
 * 
 * @author Andy Seaborne
 */

public abstract class Element
{
    public Set<Var> varsMentioned()
    {
        // Binding variables -- in patterns, not in filters and not in EXISTS
        LinkedHashSet<Var> s = new LinkedHashSet<Var>() ;
        return PatternVars.vars(s, this) ;
    }
    
    public abstract void visit(ElementVisitor v) ;
    
    @Override
    public abstract int hashCode() ;
    // If the labeMap is null, do .equals() on nodes, else map from
    // bNode varables in one to bNodes variables in the other 
    public abstract boolean equalTo(Element el2, NodeIsomorphismMap isoMap) ;
    
    @Override
    final public boolean equals(Object el2)
    { 
        if ( this == el2 ) return true ;

        if ( ! ( el2 instanceof Element ) )
            return false ;
        return equalTo((Element)el2, null) ;
    }
    
    
    @Override
    public String toString()
    {
        return FormatterElement.asString(this) ;
    }
    
    // Constants used in hashing to stop an element and it's subelement
    // (if just one) having the same hash.
    
    static final int HashBasicGraphPattern    = 0xA1 ;
    static final int HashGroup                = 0xA2 ;
    static final int HashUnion                = 0xA3 ;
    static final int HashOptional             = 0xA4 ;
    // static final int HashGraph                = 0xA5 ; // Not needed
    static final int HashExists               = 0xA6 ;
    static final int HashNotExists            = 0xA7 ;
    static final int HashPath                 = 0xA8 ;
    static final int HashFetch                = 0xA9 ;
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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