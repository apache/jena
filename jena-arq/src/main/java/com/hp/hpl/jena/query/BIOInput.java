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

package com.hp.hpl.jena.query;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingInputStream ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class BIOInput
{
    private static Model m = ModelFactory.createDefaultModel() ;
    
    public static ResultSet fromBIO(InputStream input)
    {
        // Scan the stream for VARS and accumulate the total variables. 
        // Trade off of guessing first line is all the VARS and coping with
        // anything possible.  -> Cope with anything possible.
        BindingInputStream bin = new BindingInputStream(input) ;
        List<Binding> bindings = new ArrayList<>() ;
        List<Var> vars = new ArrayList<>() ;
        while(bin.hasNext())
        {
            Binding b = bin.next();
            bindings.add(b) ;
            for ( Var v : bin.vars() )
            {
                if ( ! vars.contains(v) )
                    vars.add(v) ;
            }
        }
        QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
        return new ResultSetStream(Var.varNames(vars), m, qIter) ;
    }
}
