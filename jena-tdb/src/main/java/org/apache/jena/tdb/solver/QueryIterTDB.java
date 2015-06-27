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

package org.apache.jena.tdb.solver;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class QueryIterTDB extends QueryIterPlainWrapper
{
    final private QueryIterator originalInput ;
    private List<Abortable> killList ;
    
    // The original input needs closing as well.
    public QueryIterTDB(Iterator<Binding> iterBinding, List<Abortable> killList , QueryIterator originalInput, ExecutionContext execCxt)
    {
        super(iterBinding, execCxt) ;
        this.originalInput = originalInput ;
        this.killList = killList ;
    }
    
    @Override
    protected void closeIterator()
    { 
        if ( originalInput != null )
            originalInput.close();
        super.closeIterator() ;
    }

    @Override
    protected void requestCancel()
    { 
        if ( killList != null )
            for ( Abortable it : killList )
                it.abort() ;
        if ( originalInput != null )
            originalInput.cancel(); 
    }
}
