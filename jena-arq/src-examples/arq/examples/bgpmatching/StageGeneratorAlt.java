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

package arq.examples.bgpmatching;


import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterTriplePattern ;
import org.apache.jena.sparql.engine.main.StageGenerator ;

/** Example stage generator that compiles a BasicPattern into a sequence of
 *  individual triple matching steps.
 */   

public class StageGeneratorAlt implements StageGenerator
{
    StageGenerator other ;
    
    public StageGeneratorAlt(StageGenerator other)
    {
        this.other = other ;
    }
    
    
    @Override
    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt)
    {
        // Just want to pick out some BGPs (e.g. on a particualr graph)
        // Test ::  execCxt.getActiveGraph() 
        if ( ! ( execCxt.getActiveGraph() instanceof GraphBase ) )
            // Example: pass on up to the original StageGenerator if
            // not based on GraphBase (which most Graph implementations are). 
            return other.execute(pattern, input, execCxt) ;
        
        System.err.println("MyStageGenerator.compile:: triple patterns = "+pattern.size()) ;

        // Stream the triple matches together, one triple matcher at a time. 
        QueryIterator qIter = input ;
        for (Triple triple : pattern.getList())
            qIter = new QueryIterTriplePattern(qIter, triple, execCxt) ;
        return qIter ;
    }
}
