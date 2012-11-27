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

package com.hp.hpl.jena.sparql.engine.iterator;


import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class QueryIterBlockTriples extends QueryIter1
{
    private BasicPattern pattern ;
    private Graph graph ;
    private QueryIterator output ;
    
    public static QueryIterator create(QueryIterator input,
                                       BasicPattern pattern , 
                                       ExecutionContext execContext)
    {
        return new QueryIterBlockTriples(input, pattern, execContext) ;
    }
    
    private QueryIterBlockTriples(QueryIterator input,
                                    BasicPattern pattern , 
                                    ExecutionContext execContext)
    {
        super(input, execContext) ;
        this.pattern = pattern ;
        graph = execContext.getActiveGraph() ;
        // Create a chain of triple iterators.
        QueryIterator chain = getInput() ;
        for (Triple triple : pattern)
            chain = new QueryIterTriplePattern(chain, triple, execContext) ;
        output = chain ;
    }

    @Override
    protected boolean hasNextBinding()
    {
        return output.hasNext() ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        return output.nextBinding() ;
    }

    @Override
    protected void closeSubIterator()
    {
        if ( output != null )
            output.close() ;
        output = null ;
    }
    
    @Override
    protected void requestSubCancel()
    {
        if ( output != null )
            output.cancel();
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Utils.className(this)) ;
        out.println() ;
        out.incIndent() ;
        FmtUtils.formatPattern(out, pattern, sCxt) ;
        out.decIndent() ;
    }
}
