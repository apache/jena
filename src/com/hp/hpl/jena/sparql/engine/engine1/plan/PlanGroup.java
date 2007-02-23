/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.engine1.PlanVisitor;
import com.hp.hpl.jena.sparql.util.Context;

public class PlanGroup extends PlanElementN
{
    private static Log log = LogFactory.getLog(PlanGroup.class) ;
    public static boolean enableOrderWarnings = true ;
    public static boolean removeGroupOfOne = true ; 

    private boolean canReorder = true ;
    
    public static PlanElement make(Context context, List acc)
    {
        if ( removeGroupOfOne && acc.size() == 1 
            //&& ! ( acc.get(0) instanceof PlanTriplePattern ) 
            ) 
            return (PlanElement)acc.get(0) ;
        
        return new PlanGroup(context, acc, true) ;
    }

    public static PlanGroup make(Context context, List acc, boolean reorderable)
    {
        return new PlanGroup(context, acc, reorderable) ;
    }
    
    public boolean canReorder() { return canReorder ; }
    
    protected PlanGroup(Context context, List subElts, boolean reorderable)
    {
        super(context, subElts) ;
        canReorder = reorderable ;

        //varsReport() ;
        
        if ( false )
            planGroup() ;
    }
    
    private void planGroup()
    {
        // Crudely put filters last
        List filters = new ArrayList() ;
        for ( Iterator iter = getSubElements().iterator() ; iter.hasNext(); )
        {
            PlanElement elt = (PlanElement)iter.next();
            if ( elt instanceof PlanFilter )
            {
                filters.add(elt) ;
                iter.remove() ;
            }
        }
        getSubElements().addAll(filters) ;
    }
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        return PlanUtils.buildSerial(this, input, execCxt) ;
    }
    
    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }

    public PlanElement apply(Transform transform, List newSubElements)
    { return transform.transform(this, newSubElements) ; }

    public PlanElement copy(List newSubElements)
    {
        return make(getContext(), newSubElements, false);
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