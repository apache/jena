/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public abstract class OpN extends OpBase
{ 
    private List<Op> elements = new ArrayList<Op>() ;
    
    protected OpN()         { elements = new ArrayList<Op>() ; }
    protected OpN(List<Op> x)   { elements = x ; }
    
    public void add(Op op) { elements.add(op) ; }
    public Op get(int idx) { return elements.get(idx) ; }
    
    public abstract Op apply(Transform transform, List<Op> elts) ;
    public abstract Op copy(List<Op> elts) ;
    

    // Tests the sub-elements for equalTo.
    protected boolean equalsSubOps(OpN op, NodeIsomorphismMap labelMap)
    {
        Iterator<Op> iter1 = elements.listIterator() ;
        Iterator<Op> iter2 = op.elements.listIterator() ;
        
        for ( ; iter1.hasNext() ; )
        {
            Op op1 = iter1.next();
            Op op2 = iter2.next();
            if ( ! op1.equalTo(op2, labelMap) )
                return false ;
        }
        return true ;
    }

    public int size()                   { return elements.size() ; } 

    
    @Override
    public int hashCode()               { return elements.hashCode() ; } 

    public List<Op> getElements()           { return elements ; }

    public Iterator<Op> iterator()          { return elements.iterator() ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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