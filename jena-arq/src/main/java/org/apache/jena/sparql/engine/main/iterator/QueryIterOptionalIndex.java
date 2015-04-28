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

package org.apache.jena.sparql.engine.main.iterator;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterDefaulting ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.serializer.SerializationContext ;



public class QueryIterOptionalIndex extends QueryIterRepeatApply
{
    private Op op ;

    public QueryIterOptionalIndex(QueryIterator input, Op op, ExecutionContext context)
    {
        super(input, context) ;
        this.op = op ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        Op op2 = QC.substitute(op, binding) ;
        QueryIterator thisStep = QueryIterSingleton.create(binding, getExecContext()) ;
        
        QueryIterator cIter = QC.execute(op2, thisStep, super.getExecContext()) ;
        cIter = new QueryIterDefaulting(cIter, binding, getExecContext()) ;
        return cIter ;
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Lib.className(this)) ;
        out.incIndent() ;
        op.output(out, sCxt) ;
        out.decIndent() ;
    }
}
