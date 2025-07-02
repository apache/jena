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

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek ;
import org.apache.jena.sparql.engine.main.solver.PatternMatchData;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderProc ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.sparql.mgt.Explain ;

/**
 * Generic - always works - StageGenerator.
 */
public class StageGeneratorGeneric implements StageGenerator {
    private static final ReorderTransformation reorderFixed = ReorderLib.fixed() ;

    public StageGeneratorGeneric() {}

    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        if ( input == null )
            Log.error(this, "Null input to " + Lib.classShortName(this.getClass())) ;

        // Choose reorder transformation and execution strategy.
        ReorderTransformation reorder = reorderFixed ;
        return execute(pattern, reorder, input, execCxt) ;
    }

    /**
     * Attempts to construct an iterator that executes the input against the pattern.
     * If the construction fails, such as due to {@link QueryCancelledException}, then the exception is passed on
     * and the input iterator will be closed.
     */
    protected QueryIterator execute(BasicPattern pattern, ReorderTransformation reorder,
                                    QueryIterator input, ExecutionContext execCxt) {
        Explain.explain(pattern, execCxt.getContext()) ;

        if ( ! input.hasNext() )
            return input ;

        if ( reorder != null && pattern.size() >= 2 ) {
            // If pattern size is 0 or 1, nothing to do.
            BasicPattern bgp2 = pattern ;

            // Try to ground the pattern
            if ( ! input.isJoinIdentity() ) {
                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
                // And now use this one
                input = peek ;
                Binding b ;
                try {
                    b = peek.peek() ;
                } catch (Exception e) {
                    // Close peek iterator on failure e.g. due to cancellation.
                    peek.close() ;
                    e.addSuppressed(new RuntimeException("Error during peek().")) ;
                    throw e ;
                }
                bgp2 = Substitute.substitute(pattern, b) ;
            }
            ReorderProc reorderProc = reorder.reorderIndexes(bgp2) ;
            pattern = reorderProc.reorder(pattern) ;
        }
        Explain.explain("Reorder/generic", pattern, execCxt.getContext()) ;
        return PatternMatchData.execute(execCxt.getActiveGraph(), pattern, input, null, execCxt) ;
    }
}
