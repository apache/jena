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

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.http.Service ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterCommonParent ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;
import com.hp.hpl.jena.sparql.engine.main.QC ;


public class QueryIterService extends QueryIterRepeatApply
{
    OpService opService ;
    
    public QueryIterService(QueryIterator input, OpService opService, ExecutionContext context)
    {
        super(input, context) ;
        if ( context.getContext().isFalse(Service.serviceAllowed) )
            throw new QueryExecException("SERVICE not allowed") ; 
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
            // This iterator is materialized already otherwise we may end up
            // not servicing the HTTP connection as needed.
            // In extremis, can cause a deadlock when SERVICE loops back to this server.
            // Add tracking.
            qIter = QueryIter.makeTracked(qIter, getExecContext()) ;
        } catch (RuntimeException ex)
        {
            if ( silent )
            {
                Log.warn(this, "SERVICE <" + opService.getService().toString() + ">: " + ex.getMessage()) ;
                // Return the input
                return QueryIterSingleton.create(outerBinding, getExecContext()) ; 
            }
            throw ex ;
        }
            
        // Need to put the outerBinding as parent to every binding of the service call.
        // There should be no variables in common because of the OpSubstitute.substitute 
        QueryIterator qIter2 = new QueryIterCommonParent(qIter, outerBinding, getExecContext()) ;
        return qIter2 ;
    }
}
