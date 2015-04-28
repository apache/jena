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
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.path.PathLib ;
import org.apache.jena.sparql.serializer.SerializationContext ;

public class QueryIterPath extends QueryIterRepeatApply
{
    private TriplePath triplePath ;
    private Var varSubject = null ;
    private Var varObject = null ;
    

    public QueryIterPath(TriplePath triplePath, QueryIterator input, ExecutionContext context)
    {
        super(input, context) ;
        this.triplePath = triplePath ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        QueryIterator qIter = PathLib.execTriplePath(binding, triplePath, getExecContext()) ;
        return qIter ; 
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Lib.className(this)) ;
        out.println() ;
        out.incIndent() ;
        out.print(triplePath.toString()) ; 
        out.decIndent() ;
    }
}
