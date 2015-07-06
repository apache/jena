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

package org.apache.jena.sparql.pfunction.library;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.pfunction.PFuncSimple ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.IterLib ;

/** Assignment: does not change the value of an existing binding.
 *  Either the subject or object must be a constant or be a bound variable.
 *  If both are bound, it degenerates to a test of RDF term equality. */

public class assign extends PFuncSimple
{
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt)
    {
        if ( subject.isVariable() && object.isVariable() )
            throw new QueryExecException("Both subject and object are unbound variables: "+FmtUtils.stringForNode(predicate)) ;
        if ( subject.isVariable() )
            // Object not a variable or already bound
            return IterLib.oneResult(binding, Var.alloc(subject), object, execCxt) ;
        if ( object.isVariable() )
            // Subjects not a variable or already bound
            return IterLib.oneResult(binding, Var.alloc(object), subject, execCxt) ;
        // Both bound.  Must be the same.
        if ( subject.sameValueAs(object) )
            return IterLib.result(binding, execCxt) ;
        // different
        return IterLib.noResults(execCxt) ;
    }

}
