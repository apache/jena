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

package org.apache.jena.sparql.pfunction;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.IterLib ;

abstract
class PFuncAssignBase extends PFuncSimple
{
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt)
    {
        try {
            if ( subject.isVariable() )
            {
                Log.warn(this, "Variable found: expected a value: "+subject) ;
                return IterLib.noResults(execCxt) ;
            }
            
            Node r = calc(subject) ;
            
            // Variable bound? 
            if ( Var.isVar(object) ) //object.isVariable() )
                return IterLib.oneResult(binding, Var.alloc(object), r, execCxt) ;
            
            // Variable already bound - test same value.
            if ( r.equals(object) )
                return IterLib.result(binding, execCxt) ;
            
            return IterLib.noResults(execCxt) ;
        }  catch (Exception ex)
        { 
            Log.warn(this, "Exception: "+ex.getMessage(), ex);
            return null ;
        }
    }

    public abstract Node calc(Node node) ;
}
