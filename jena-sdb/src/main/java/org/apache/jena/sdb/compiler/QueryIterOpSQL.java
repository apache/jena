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

package org.apache.jena.sdb.compiler;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sdb.shared.SDBInternalError ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;

public class QueryIterOpSQL extends QueryIterRepeatApply
{

    private OpSQL opSQL ;
    
    public QueryIterOpSQL(OpSQL op, 
                        QueryIterator input ,
                        ExecutionContext context)
    { 
        super(input, context) ;
        this.opSQL = op ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        OpSQL execSQL = this.opSQL ;

        if ( binding != null && ! isRoot(binding) )
        {
            QueryCompiler qc = opSQL.getRequest().getStore().getQueryCompilerFactory().createQueryCompiler(opSQL.getRequest()) ;
            Op op2 = Substitute.substitute(opSQL.getOriginal(), binding) ;
            Op op = qc.compile(op2) ;
            if ( op instanceof OpSQL )
                execSQL = (OpSQL)op ;
            else
                throw new SDBInternalError("Failed to recompile the OpSQL to an OpSQL") ;
        }

        return execSQL.exec(binding, getExecContext()) ;
    }
    
    private static boolean isRoot(Binding binding)
    {
        return ! binding.vars().hasNext() ; 
    }

    @Override
    public void output(IndentedWriter out)
    {
        opSQL.output(out) ;
    }
}
