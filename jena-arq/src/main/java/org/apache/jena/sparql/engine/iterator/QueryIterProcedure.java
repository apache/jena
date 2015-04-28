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

package org.apache.jena.sparql.engine.iterator;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.procedure.Procedure ;
import org.apache.jena.sparql.serializer.SerializationContext ;

/** QueryIterator for a procedure.  Delays first touch until first call because
 *  first touch may cause work to be done.
 *  
 *  Assumes .build already called. */

public class QueryIterProcedure extends QueryIter1
{
    private Procedure proc ;
    private boolean initialized = false ;
    private QueryIterator procIter = null ;
    
    public QueryIterProcedure(QueryIterator input, Procedure proc, ExecutionContext execCxt)
    {
        super(input, execCxt) ;
        this.proc = proc ;
    }

    private void init()
    {
        if ( ! initialized )
        {
            procIter = proc.proc(getInput(), getExecContext()) ;
            initialized = true ;
        }
    }

    @Override
    protected void closeSubIterator()
    { 
        init() ;    // Ensure initialized even if immediately closed.
        procIter.close(); 
    }

    @Override
    protected void requestSubCancel()
    { 
       if (procIter != null) 
    	   procIter.cancel(); 
    }
    
    @Override
    protected boolean hasNextBinding()
    {
        init() ;
        return procIter.hasNext() ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        init( ) ;
        return procIter.nextBinding() ;
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Lib.className(this)) ;
        out.print(" ") ;
        proc.output(out, sCxt) ;
    }
}
