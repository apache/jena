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

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class QueryIteratorResultSet extends QueryIteratorBase
{
    private ResultSet resultSet ; 
    public QueryIteratorResultSet(ResultSet rs) { resultSet = rs ; }
    
    @Override
    protected void closeIterator()          { resultSet = null ; }
    @Override
    protected void requestCancel()          { }
    @Override
    protected boolean hasNextBinding()      { return resultSet.hasNext() ; }
    @Override
    protected Binding moveToNextBinding()   { return resultSet.nextBinding() ; }

    @Override
    public void output(IndentedWriter out, SerializationContext cxt)
    {
        out.print(Utils.className(this)) ;
    }
}
