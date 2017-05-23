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
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingRoot ;
import org.apache.jena.sparql.serializer.SerializationContext ;

/** The root binding is one-row, no columns making it the join identity.
 *  It is useful to be able to spot it before having to activate a {@link QueryIterator}.
 *  Executing with a pre-set binding does not use QueryIterRoot.
 */
public class QueryIterRoot extends QueryIterSingleton
{
    public static QueryIterator createRoot(ExecutionContext execCxt)
    { return new QueryIterRoot(BindingRoot.create(), execCxt) ; }
    
    private QueryIterRoot(Binding binding, ExecutionContext execCxt) {
        super(binding, execCxt) ;
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        if ( binding instanceof BindingRoot )
            out.print("QueryIterRoot");
        else
            // Not used
            out.print("QueryIterRoot: "+binding);
    }
    
    @Override
    public boolean isJoinIdentity() { return true; }
}
