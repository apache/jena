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

package com.hp.hpl.jena.sparql.engine.main.iterator;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.http.Service ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterCommonParent ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.engine.main.QC ;


public class QueryIterService extends QueryIterRepeatApply
{
    OpService opService ;
    
    public QueryIterService(QueryIterator input, OpService opService, ExecutionContext context)
    {
        super(input, context) ;
        this.opService = opService ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding outerBinding)
    {
        Op op = QC.substitute(opService, outerBinding) ;
        boolean silent = opService.getSilent() ;
        QueryIterator qIter ;
        try {
            qIter = Service.exec((OpService)op, getExecContext().getContext()) ;
        } catch (RuntimeException ex)
        {
            if ( silent )
            {
                Log.warn(this, "SERVICE: "+ex.getMessage()) ;
                return new QueryIterNullIterator(getExecContext()) ; 
            }
            throw ex ;
        }
            
        // Need to put the outerBinding as parent to every binding of the service call.
        // There should be no variables in common because of the OpSubstitute.substitute 
        QueryIterator qIter2 = new QueryIterCommonParent(qIter, outerBinding, getExecContext()) ;

        // Materialise, otherwise we may have outstanding incoming data.
        // Allows the server to fulfil the request as soon as possible.
        // In extremis, can cause a deadlock when SERVICE loops back to this server.
        return QueryIter.materialize(qIter2, getExecContext()) ;
    }
}
