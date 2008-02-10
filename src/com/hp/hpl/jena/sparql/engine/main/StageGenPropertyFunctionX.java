/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.util.*;

import com.hp.hpl.jena.query.ARQ;


// Not used any more.  PropertyFunctions are done in the algebra with called procedures */ 
public class StageGenPropertyFunctionX implements StageGenerator
{
    private StageGenerator other ;

    /** A StageGenerator that splits out triples in
     *  those that are property functions and those that are not.
     *  Calls <code>other</code> on each block of non-property function triples.
     *  Merges the results with StagePropertyFunctions to produce an overall
     *  StageList.
     *  */
    /*public*/ private StageGenPropertyFunctionX(StageGenerator other)
    {
        this.other = other ;
    }
    
    public StageList compile(BasicPattern pattern,
                             ExecutionContext execCxt)
    {
        if ( pattern.isEmpty() )
            return new StageList() ;
        
        boolean doingMagicProperties = execCxt.getContext().isTrue(ARQ.enablePropertyFunctions) ;
        if ( ! doingMagicProperties )
            return other.compile(pattern, execCxt) ;
        
        return process(pattern, other, execCxt) ;
    }
    
    // ---------------------------------------------------------------------------------
    // Split into Stages of triples and property functions.
    
    private static StageList process(BasicPattern pattern, StageGenerator other, ExecutionContext execCxt)
    {
        Context context = execCxt.getContext() ;
        PropertyFunctionRegistry registry = chooseRegistry(context) ;
    
        // 1/ Find property functions.
        //    Property functions may involve other triples (for list arguments)
        // 2/ Make the property function elements, remove associated triples
        //    (but leave the proprty function triple in-place as a marker)
        // 3/ For remaining triples, put into blocks, call the other generator for each block.
        
        List propertyFunctionTriples = new ArrayList() ;    // Property functions seen
        BasicPattern triples = new BasicPattern() ;         // All triples (mutated)
        
        findPropetryFunctions(context, pattern, registry, triples, propertyFunctionTriples) ;
        
        Map pfStages = new HashMap() ;
        // Removes triples of list arguments.
        makePropetryFunctions(context, registry, pfStages, triples, propertyFunctionTriples) ;
        
        StageList stages = new StageList() ;                // The elements of the BGP execution
        makeStages(context, other, stages, pfStages, triples, propertyFunctionTriples, execCxt) ;
        return stages ;
    }

    private static void findPropetryFunctions(Context context, BasicPattern pattern,
                                              PropertyFunctionRegistry registry,
                                              BasicPattern triples, List propertyFunctionTriples)
    {
        // Stage 1 : find property functions (if any); collect triples.
        // Not list arg triples atthis point.
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next() ;
            
            if ( ! ( obj instanceof Triple ) )
            {
                ALog.warn(StageGenPropertyFunctionX.class, "Don't recognize: ["+Utils.className(obj)+"]") ;
                throw new ARQInternalErrorException("Not a triple pattern: "+obj.toString() ) ;
            }
                
            Triple t = (Triple)obj ;
            triples.add(t) ;
            if ( isMagicProperty(registry, t) )
                propertyFunctionTriples.add(t) ;
        }
    }

    private static void makePropetryFunctions(Context context, 
                                              PropertyFunctionRegistry registry, Map pfStages, 
                                              BasicPattern triples, List propertyFunctionTriples)
    {
        // Stage 2 : for each property function, make element
        // Remove associated triples in list arguments; leave the propertyFunction triple itself.
        // Build map property function triple => stage.
        for ( Iterator iter = propertyFunctionTriples.iterator() ; iter.hasNext(); )
        {
            Triple pf = (Triple)iter.next();
            
            // Removes associated triples. 
            Stage stage = magicProperty(context, registry, pf, triples) ;
            if ( stage == null )
            {
                ALog.warn(StageGenPropertyFunctionX.class, "Lost a Stage for a property function") ;
                continue ;
            }
            pfStages.put(pf, stage) ;
        }
    }

    private static void makeStages(Context context, StageGenerator other,
                                   StageList stages, 
                                   Map pfStages, 
                                   BasicPattern triples,
                                   List propertyFunctionTriples, ExecutionContext execCxt)
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
                flush(pattern, stages, other, execCxt) ;
                pattern = null ;       // Flush any current BasicPattern
                // It's a property function stage.
                Stage stage = (Stage)pfStages.get(t) ;
                stages.add(stage) ;
                continue ;
            }                
                
            // Regular triples - make sure there is a basic pattern in progress. 
            if ( pattern == null )
                pattern = new BasicPattern() ;
            pattern.add(t) ;
        }
        flush(pattern, stages, other, execCxt) ;
    }
    
    private static void flush(BasicPattern pattern, StageList stages, StageGenerator other, ExecutionContext execCxt)
    {
        if ( pattern == null || pattern.isEmpty() )
            return  ;
        StageList sl = other.compile(pattern, execCxt) ;
        stages.addAll(sl) ;
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
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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