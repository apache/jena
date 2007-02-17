/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.main;

import java.util.*;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.pfunction.PropFuncArg;
import com.hp.hpl.jena.query.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.GNode;
import com.hp.hpl.jena.query.util.GraphList;
import com.hp.hpl.jena.query.util.Utils;

public class StageGenPropertyFunction implements StageGenerator
{
    private StageGenerator other ;

    /** A StageGenerator that splits out triples in
     *  those that are property functions and those that are not.
     *  Calls <code>other</code> on each block of non-property function triples.
     *  Merges the results with StagePropertyFunctions to produce an overall
     *  StageList   
     *  */
    public StageGenPropertyFunction(StageGenerator other)
    {
        this.other = other ;
    }
    
    public StageList compile(BasicPattern pattern,
                             ExecutionContext execCxt)
    {
        if ( pattern.isEmpty() )
            return new StageList() ;
        return process(execCxt.getContext(), pattern) ;
    }
    
    // From StageProcessorFunc which can be removed later.
    // ---------------------------------------------------------------------------------
    // Split into Stages of triples and property functions.
    
    private static StageList process(Context context, BasicPattern pattern)
    {
        boolean doingMagicProperties = context.isTrue(ARQ.enablePropertyFunctions) ;
        PropertyFunctionRegistry registry = chooseRegistry(context) ;
    
        // 1/ Find property functions.
        //   Property functions may involve other triples (for list arguments)
        // 2/ Make the property function elements, remove associated triples
        //   (but leave the proprty function triple in-place as a marker)
        // 3/ For remaining triples, put into blocks.
        
        StageList stages = new StageList() ;                // The elements of the BGP execution
        List propertyFunctionTriples = new ArrayList() ;    // Property functions seen
        BasicPattern triples = new BasicPattern() ;         // All triples (mutated)
        
        findPropetryFunctions(context, pattern, 
                              doingMagicProperties, registry,
                              triples, propertyFunctionTriples) ;
        
        Map pfStages = new HashMap() ;
        makePropetryFunctions(context, registry, pfStages, triples, propertyFunctionTriples) ;
        
        makeStages(context, stages, pfStages, triples, propertyFunctionTriples) ;
        return stages ;
    }

    private static void findPropetryFunctions(Context context, BasicPattern pattern,
                                              boolean doingMagicProperties,
                                              PropertyFunctionRegistry registry,
                                              BasicPattern triples, List propertyFunctionTriples)
    {
        // Stage 1 : find property functions (if any); collect triples.
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next() ;
            
            if ( ! ( obj instanceof Triple ) )
            {
                LogFactory.getLog(StageGenPropertyFunction.class).warn("Don't recognize: ["+Utils.className(obj)+"]") ;
                throw new ARQInternalErrorException("Not a triple pattern: "+obj.toString() ) ;
            }
                
            Triple t = (Triple)obj ;
            triples.add(t) ;
            if ( doingMagicProperties && isMagicProperty(registry, t) )
                propertyFunctionTriples.add(t) ;
        }
    }

    private static void makePropetryFunctions(Context context, 
                                              PropertyFunctionRegistry registry, Map pfStages, 
                                              BasicPattern triples, List propertyFunctionTriples)
    {
        // Stage 2 : for each property function, make element and remove associated triples
        for ( Iterator iter = propertyFunctionTriples.iterator() ; iter.hasNext(); )
        {
            Triple pf = (Triple)iter.next();
            
            // Removes associated triples. 
            Stage stage = magicProperty(context, registry, pf, triples) ;
            if ( stage == null )
            {
                LogFactory.getLog(StageGenPropertyFunction.class).warn("Lost a Stage for a property function") ;
                continue ;
            }
            pfStages.put(pf, stage) ;
        }
    }

    private static void makeStages(Context context,
                                   StageList stages, 
                                   Map pfStages, 
                                   BasicPattern triples,
                                   List propertyFunctionTriples)
    {
        // Stage 3 : make line up the stages.
        //   For each property function, insert the implementation 
        //   For each block of non-property function triples, make a Stage
        
        BasicPattern pattern = null ;
        for ( Iterator iter = triples.iterator() ; iter.hasNext(); )
        {
            Triple t = (Triple)iter.next() ;
            
            if ( propertyFunctionTriples.contains(t) )
            {
                // It's a property function stage.
                Stage stage = (Stage)pfStages.get(t) ;
                stages.add(stage) ;
                pattern = null ;       // Unset any current BasicPattern
                continue ;
            }                
                
            // Regular triples - make sure there is a basic pattern in progress. 
            if ( pattern == null )
            {
                pattern = new BasicPattern() ;
                Stage basicStage = new StageBasic(pattern) ;
                stages.add(basicStage) ;
            }
            pattern.add(t) ;         // Plain triple
        }
    }
    
    private static PropertyFunctionRegistry chooseRegistry(Context context)
    {
        PropertyFunctionRegistry registry = PropertyFunctionRegistry.get(context) ;
        // Else global
        if ( registry == null )
            registry = PropertyFunctionRegistry.get() ;
        return registry ;
    }
    
    private static boolean isMagicProperty(PropertyFunctionRegistry registry, Triple pfTriple)
    {
        if ( ! pfTriple.getPredicate().isURI() ) 
            return false ;

        if ( registry.manages(pfTriple.getPredicate().getURI()) )
            return true ;
        
        return false ;
    }
    
    // Remove all triples associated with this magic property,
    // and make the stage. Null means it's not really a magic property
    private static Stage magicProperty(Context context, PropertyFunctionRegistry registry, Triple pfTriple, BasicPattern triples)
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
        
        PropFuncArg subjArgs = new PropFuncArg(sList, pfTriple.getSubject()) ;
        PropFuncArg objArgs =  new PropFuncArg(oList, pfTriple.getObject()) ;

        Stage propFuncStage = StagePropertyFunction.make(context, subjArgs, pfTriple.getPredicate(), objArgs) ;
        if ( propFuncStage != null )
            triples.getList().removeAll(listTriples) ;
        return propFuncStage ;
    }
   
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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