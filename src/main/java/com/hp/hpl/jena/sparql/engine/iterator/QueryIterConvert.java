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

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;


/** Iterator over another QueryIterator, applying a converter function
 *  to each object that is returned by .next() */

public class QueryIterConvert extends QueryIter1
{
    public interface Converter
    {
        public Binding convert(Binding obj) ;
    }
    
    Converter converter ; 
    
    public QueryIterConvert(QueryIterator iter, Converter c, ExecutionContext context)
    { 
        super(iter, context) ;
        converter = c ;
    }
    
    @Override
    protected void 
    closeSubIterator() {}
    
    @Override
    protected void 
    requestSubCancel() {}

    @Override
    public boolean hasNextBinding()
    {
        return getInput().hasNext() ;
    }

    @Override
    public Binding moveToNextBinding()
    {
        return converter.convert(getInput().nextBinding()) ;
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext cxt)
    { 
        out.println(Utils.className(this)) ;
    }
}
