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

import java.util.Map;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.index.IndexFactory ;
import com.hp.hpl.jena.sparql.engine.index.IndexTable ;

/** Minus by materializing the RHS - this is not streamed on the right */
public class QueryIterMinus extends QueryIter2
{
	private IndexTable tableRight;
	private Map<Var, Integer> varColumns ;
	private Set<Node[]> rightTable;
    Binding slot = null ;

	public QueryIterMinus(QueryIterator left, QueryIterator right, Set<Var> commonVars, ExecutionContext qCxt)
    {
        super(left, right, qCxt) ;
        tableRight = IndexFactory.createIndex(commonVars, right) ;
    }

    protected Binding getNextSlot(Binding bindingLeft)
    {
        if ( tableRight.containsCompatibleWithSharedDomain(bindingLeft) )
        	return null ;
        
        return bindingLeft;
    }

    @Override
    protected final void closeSubIterator() { }
    
    @Override
    protected void requestSubCancel() { }
   
    @Override
    protected final boolean hasNextBinding()
    {
        if ( slot != null )
            return true ;
        
        while ( getLeft().hasNext() )
        {
            Binding bindingLeft = getLeft().nextBinding() ;
            slot = getNextSlot(bindingLeft) ;
            if ( slot != null )
            {
                slot = bindingLeft ; 
                return true ;
            }
        }
        getLeft().close() ;
        return false ;
    }

    @Override
    protected final Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            return null ;
        Binding x = slot ;
        slot = null ;
        return x ;
    }
}
