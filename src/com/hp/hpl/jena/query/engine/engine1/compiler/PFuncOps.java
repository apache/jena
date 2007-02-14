/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.compiler;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanPropertyFunction;
import com.hp.hpl.jena.query.pfunction.PropFuncArg;
import com.hp.hpl.jena.query.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.GNode;
import com.hp.hpl.jena.query.util.GraphList;

/** Manipulation functions for property functions
 * Auxilliary to PlaBasicGraphPattern
 * @author Andy Seaborne
 * @version $Id: PFuncOps.java,v 1.7 2007/02/01 18:05:30 andy_seaborne Exp $
 */

public class PFuncOps
{
   
//    public static PlanElement magicProperty(Context context, Triple t, List triples)
//    { return magicProperty(context, chooseRegistry(context), t, triples) ; }
    
    public static boolean isMagicProperty(PropertyFunctionRegistry registry, Triple pfTriple)
    {
        if ( ! pfTriple.getPredicate().isURI() ) 
            return false ;

        if ( registry.manages(pfTriple.getPredicate().getURI()) )
            return true ;
        
        return false ;
    }
    
    public static PlanElement magicProperty(Context context, PropertyFunctionRegistry registry, Triple pfTriple, List triples)
    {
        if ( ! isMagicProperty(registry, pfTriple) )
            throw new ARQInternalErrorException("Not a property function: "+pfTriple.getMatchPredicate()) ;
        
        List listTriples = new ArrayList() ;
        GNode sGNode = new GNode(triples, pfTriple.getSubject()) ;
        GNode oGNode = new GNode(triples, pfTriple.getObject()) ;
        List sList = null ;
        List oList = null ;
        
        if ( GraphList.isListNode(sGNode) )
        {
            sList = GraphList.members(sGNode) ;
            GraphList.allTriples(sGNode, listTriples) ;
        }
        if ( GraphList.isListNode(oGNode) )
        {
            oList = GraphList.members(oGNode) ;
            GraphList.allTriples(oGNode, listTriples) ;
        }
        
        triples.removeAll(listTriples) ;
        
        PropFuncArg subjArgs = new PropFuncArg(sList, pfTriple.getSubject()) ;
        PropFuncArg objArgs =  new PropFuncArg(oList, pfTriple.getObject()) ;

        PlanElement propFuncPlan = PlanPropertyFunction.make(context, subjArgs, pfTriple.getPredicate(), objArgs) ;
        return propFuncPlan ;
    }
    
    
    
    static public PropertyFunctionRegistry chooseRegistry(Context context)
    {
        // Get from the Plan context 
        PropertyFunctionRegistry registry =
            (PropertyFunctionRegistry)context.get(ARQConstants.registryPropertyFunctions) ;
        // Else global
        if ( registry == null )
            registry = PropertyFunctionRegistry.get() ;
        return registry ;
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