/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDistinguishedVars;
import com.hp.hpl.jena.sparql.util.Context;

/** The stage builder (there is only one) is a library that encapsulates
 * evaluation of a basic graph pattern (BGP).  Matching BGPs is an extension
 * point of SPARQL; different entailment regimes plug in at this point.
 * They are also an extension point in ARQ to connect to any datasource,
 * the most common case being connectinbg to a Jena graph.     
 * 
 * The StageBuilder finds the registered StageGenerator, and calls it to
 * evaluate a basic graph pattern that has any bound variables
 * replaced by their value (in effect, an index join).
 *
 * Extension happens by registering a different StageGenerator in
 * the context object for the execution. Setting the StageGenerator
 * in the global context ({@link ARQ}) makes it available
 * to all query execution created after the point of setting.
 * 
 * Helper static methods for setting the stage generator are provided.  
 * 
 * @see StageGenerator
 */

public class StageBuilder
{
    public static QueryIterator execute(BasicPattern pattern, 
                                        QueryIterator input, 
                                        ExecutionContext execCxt)
    {
        if ( pattern.isEmpty() )
            return input ;
        
        boolean hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables) ;
        
        StageGenerator gen = chooseStageGenerator(execCxt.getContext()) ;
        QueryIterator qIter = gen.execute(pattern, input, execCxt) ;

        // Remove non-distinguished variables here.
        // Project out only named variables.
        if ( hideBNodeVars )
            qIter = new QueryIterDistinguishedVars(qIter, execCxt) ;
        return qIter ;
    }
    
    // -------- Initialize
    
    public static void init()
    {
        StageGenerator gen = getGenerator(ARQ.getContext()) ;
        if ( gen == null )
        {
            gen = standardGenerator() ;
            setGenerator(ARQ.getContext(), gen) ;
        }
    }
    
    // -------- Manage StageGenerator registration
    
    public static void setGenerator(Context context, StageGenerator builder)
    {
        context.set(ARQ.stageGenerator, builder) ;
    }
    
    public static StageGenerator getGenerator(Context context)
    {
        if ( context == null )
            return null ;
        return (StageGenerator)context.get(ARQ.stageGenerator) ;
    }
    
    public static StageGenerator getGenerator()
    {
        return getGenerator(ARQ.getContext()) ;
    }
    
    public static StageGenerator standardGenerator()
    {
        return new StageGeneratorGeneric() ;
    }
    
    private static StageGenerator chooseStageGenerator(Context context)
    {
        StageGenerator gen = getGenerator(context) ;
        if ( gen == null )
            gen = getGenerator() ;
        return gen ; 
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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