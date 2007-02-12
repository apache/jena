/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.plan;

import java.util.*;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.engine1.*;
import com.hp.hpl.jena.query.engine.engine1.iterator.QueryIterUnion;
import com.hp.hpl.jena.query.util.Context;

public class PlanUnion extends PlanElementN
{
    public static PlanElement make(Context context, List acc)
    {
        if ( acc.size() == 1 )
            return (PlanElement)acc.get(0) ;
        return new PlanUnion(context, acc) ;
    }

    private PlanUnion(Context context, List cElts)
    {
        super(context, cElts) ;
    }
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        if ( numSubElements() == 0 )
            return input ;

        // Nesting on one item.
        if ( numSubElements() == 1 )
        {
            PlanElement e = (PlanElement)getSubElement(1) ;
            return e.build(input, execCxt) ;
        }
        
        return new QueryIterUnion(input, getSubElements(), execCxt) ;
    }
    
    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }

    public PlanElement apply(Transform transform, List x)
    {
        return transform.transform(this, x) ;
    }

    public PlanElement copy(List newSubElements)
    {
        return make(getContext(), newSubElements) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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