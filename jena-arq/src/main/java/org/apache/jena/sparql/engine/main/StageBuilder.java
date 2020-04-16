/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.engine.main;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterBlockTriplesStar ;
import org.apache.jena.sparql.util.Context ;

/** The stage builder (there is only one) is a library that encapsulates
 * evaluation of a basic graph pattern (BGP).  Matching BGPs is an extension
 * point of SPARQL; different entailment regimes plug in at this point.
 * They are also an extension point in ARQ to connect to any datasource,
 * the most common case being connecting to a Jena graph.     
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
 */

public class StageBuilder
{
//    public static QueryIterator execute(BasicPattern pattern, 
//                                        QueryIterator input, 
//                                        ExecutionContext execCxt)
//    {
//        if ( pattern.isEmpty() )
//            return input ;
//        
//        boolean hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables) ;
//        
//        StageGenerator gen = chooseStageGenerator(execCxt.getContext()) ;
//        QueryIterator qIter = gen.execute(pattern, input, execCxt) ;
//
//        // Remove non-distinguished variables here.
//        // Project out only named variables.
//        if ( hideBNodeVars )
//            qIter = new QueryIterDistinguishedVars(qIter, execCxt) ;
//        return qIter ;
//    }
    
    // -------- Initialize
    
    private static StageGenerator defaultStageGenerator = new StageGeneratorGenericStar() ; 
    
    /** The plain StageGenerator, no reordering */
    public static StageGenerator executeInline = 
        (BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)->
            QueryIterBlockTriplesStar.create(input, pattern, execCxt) ;
        
    // -------- Manage StageGenerator registration
    
    public static void setGenerator(Context context, StageGenerator stageGenerator) {
        if ( ARQ.stageGenerator == null )
            // Indicator of initialization problems
            Log.warn(StageBuilder.class, "ARQ.stageGenerator = null");
        context.set(ARQ.stageGenerator, stageGenerator);
    }

    public static StageGenerator getGenerator(Context context) {
        if ( context == null )
            return null;
        return (StageGenerator)context.get(ARQ.stageGenerator);
    }

    public static StageGenerator getGenerator() {
        return getGenerator(ARQ.getContext());
    }

    public static StageGenerator standardGenerator() {
        return defaultStageGenerator;
    }

    public static StageGenerator chooseStageGenerator(Context context) {
        StageGenerator gen = getGenerator(context);
        if ( gen == null )
            gen = standardGenerator();
        return gen;
    }
}
