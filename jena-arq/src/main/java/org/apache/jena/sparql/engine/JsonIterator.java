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

package org.apache.jena.sparql.engine;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.lib.RDFTerm2Json;

/** A JSON iterator for JsonObject's, that wraps a QueryIterator, and a list
 * of result variables.
 */
public class JsonIterator implements Iterator<JsonObject>
{

    private final QueryIterator queryIterator ;
    private final List<String> resultVars ;

    public JsonIterator(QueryIterator queryIterator, List<String> resultVars)
    {
        this.queryIterator = queryIterator ;
        this.resultVars = Collections.unmodifiableList(resultVars) ;
    }

    @Override
    public boolean hasNext()
    {
        if (queryIterator == null)
            return false;
        boolean r = queryIterator.hasNext() ;
        if (!r)
            close() ;
        return r ;
    }

    @Override
    public JsonObject next()
    {
        if (queryIterator == null)
            throw new NoSuchElementException(this.getClass() + ".next") ;
        try
        {
            Binding binding = queryIterator.next() ;
            JsonObject jsonObject = new JsonObject() ;
            for (String resultVar : resultVars)
            {
                Node n = binding.get(Var.alloc(resultVar)) ;
                JsonValue value = RDFTerm2Json.fromNode(n) ;
                jsonObject.put(resultVar, value);
            }
            return jsonObject ;
        }
        catch (NoSuchElementException ex)
        {
            close() ;
            throw ex ;
        }
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException(this.getClass().getName() + ".remove") ;
    }

    /**
     * Closes the QueryIterator, if it is an instance of Closeable.
     */
    private void close()
    {
        queryIterator.close();
    }
}
