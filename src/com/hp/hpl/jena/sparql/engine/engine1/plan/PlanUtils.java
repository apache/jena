/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;

public class PlanUtils
{
    // -------
    // Build an iterator sequence that feeds the stages one into another.
    // Conjunction, in other words.
    
    public static QueryIterator buildSerial(PlanElementN elt, QueryIterator input, ExecutionContext execCxt)
    {
        if ( elt.numSubElements() == 0 )
            return input ;
        
        // Nesting on one item.
        if ( elt.numSubElements() == 1 )
        {
            PlanElement e = elt.getSubElement(0) ;
            return e.build(input, execCxt) ;
        }

        int count = 0 ;
        
        Iterator elementsIterator = elt.getSubElements().listIterator() ;
        QueryIterator chain = input ;     // Results of previous stage.
        while(elementsIterator.hasNext() )
        {
            count++ ;
            PlanElement element = (PlanElement)elementsIterator.next() ;
            
            // Occurs if some subclass query engine nulls out a slot.
            if ( element == null )
                continue ;
           
            QueryIterator cIter = element.build(chain, execCxt) ;
            chain = cIter ;
        }
        return chain ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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