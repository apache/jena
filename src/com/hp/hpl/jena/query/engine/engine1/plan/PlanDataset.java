/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.plan;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.engine1.*;
import com.hp.hpl.jena.query.syntax.ElementDataset;
import com.hp.hpl.jena.query.util.Context;

/**  
 * @author Andy Seaborne
 * @version $Id: PlanDataset.java,v 1.5 2007/02/02 16:23:31 andy_seaborne Exp $
 */

public class PlanDataset extends PlanElement1
{
    ElementDataset element ;
    
    public static PlanElement make(Context context, ElementDataset el, PlanElement cElt)
    { return new PlanDataset(context, el, cElt) ; }

    private PlanDataset(Context context, ElementDataset el, PlanElement cElt)
    {
        super(context, cElt) ;
        element = el ;
    }
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        ExecutionContext execCxt2 = execCxt ;
        if ( element.getDataset() != null )
            execCxt2 = new ExecutionContext(execCxt, element.getDataset()) ;
        
        if ( getSubElement() == null )
        {
            // Empty sub expression
            return input ;
        }
        return  getSubElement().build(input, execCxt2) ;
    }

    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }
    
    public PlanElement apply(Transform transform, PlanElement subElt)
    { return transform.transform(this, subElt) ; }

    public PlanElement copy(PlanElement newSubElement)
    {
        return make(getContext(), element, newSubElement) ;
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