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

package com.hp.hpl.jena.sparql.engine;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase ;

public abstract class PlanBase extends PrintSerializableBase implements Plan
{
    private Op op = null ;
    protected Closeable closeable = null ;
    protected boolean closed = false ;
    private boolean iteratorProduced = false ;
    
    protected abstract QueryIterator iteratorOnce() ;

    protected PlanBase(Op op, Closeable closeable)  { this.op = op ; this.closeable = closeable ; } 
    
    @Override
    public Op getOp()       { return op ; }
    
    @Override
    final
    public QueryIterator iterator() 
    {
        if ( iteratorProduced )
        {
            throw new ARQInternalErrorException("Attempt to use the iterator twice") ;
        }
        iteratorProduced = true ;
        return iteratorOnce() ;
    }

    @Override
    public void output(IndentedWriter out)
    {
        SerializationContext sCxt = new SerializationContext(ARQConstants.getGlobalPrefixMap()) ;
        output(out, sCxt) ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        op.output(out, sCxt) ;
    }
    
    @Override
    public void close()
    { 
        if ( closed )
            return ;
        if ( closeable != null )
            // Called once
            // Two routes - explicit QueryExecution.close
            // or natural end of QueryIterator (see PlanOp)
            closeable.close() ;
        closed = true ;
    }
}
