/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1;

import java.util.Iterator;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.engine1.plan.*;

public class PlanWalker
{
    public static void walk(PlanElement el, PlanVisitor v)
    {
        if ( el == null )
        {
            LogFactory.getLog(PlanWalker.class).warn("Attempt to walk a null PlanElement - ignored") ;
            return ;
        }
        el.visit(new Walker(v)) ;
    }
    
    static private class Walker implements PlanVisitor
    {
        PlanVisitor proc ;
        Walker(PlanVisitor v) { proc = v ; }
        
        public void visit(PlanTriples planElt)      { proc.visit(planElt) ; }
        
        public void visit(PlanTriplesBlock planElt)
        {
            for ( Iterator iter = planElt.iterator() ; iter.hasNext() ; )
            {
                PlanElement e = (PlanElement)iter.next() ;
                e.visit(proc) ;
            }
            // Could be planElt.visit(proc) but we already know the type.
            proc.visit(planElt) ;
        }
        
        // Graph combinations
        public void visit(PlanGroup planElt)
        {
            for ( Iterator iter = planElt.iterator() ; iter.hasNext() ; )
            {
                PlanElement e = (PlanElement)iter.next() ;
                e.visit(this) ;
            }
            proc.visit(planElt) ;
        }
        
        public void visit(PlanUnion planElt)
        {
            for ( Iterator iter = planElt.getSubElements().iterator() ; iter.hasNext() ; )
            {
                PlanElement e = (PlanElement)iter.next() ;
                e.visit(this) ;
            }
            proc.visit(planElt) ;
        }
        
        public void visit(PlanOptional planElt)
        {
            visitOrNull(planElt.getOptional()) ;
            proc.visit(planElt) ;
        }
        
        public void visit(PlanUnsaid planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        public void visit(PlanFilter planElt)
        {
            proc.visit(planElt) ;
        }
        
        public void visit(PlanNamedGraph planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        // Other
        public void visit(PlanPropertyFunction planElt) { proc.visit(planElt) ; }
        public void visit(PlanDataset planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }

        public void visit(PlanElementExternal planElt) { proc.visit(planElt) ; }
        
        // Solution sequence modifiers
        public void visit(PlanProject planElt)
        { 
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        public void visit(PlanDistinct planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        public void visit(PlanOrderBy planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        public void visit(PlanLimitOffset planElt)
        {
            visitOrNull(planElt.getSubElement()) ;
            proc.visit(planElt) ;
        }
        
        private void visitOrNull(PlanElement pElt)
        {
            if ( pElt != null )
                pElt.visit(this) ;
        }
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