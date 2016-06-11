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
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.shared.SDBInternalError ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;

public class QueryIterOpSQL extends QueryIterRepeatApply
{

    private final OpSQL opSQL ;
    // Modifed to remove the query - after substitution, the query is no longer useful
    // information.  This also stops the bridge attempting to project variables -
    // by this stage, we want all variables back.
    private final SDBRequest request ;  
    
    public QueryIterOpSQL(OpSQL op, 
                        QueryIterator input ,
                        ExecutionContext context)
    { 
        super(input, context) ;
        this.opSQL = op ;
        SDBRequest req = op.getRequest() ;
        if ( req == null )
            this.request = null ;
        else {
            PrefixMapping pmap = req.getQuery() == null ? null : req.getQuery().getPrefixMapping() ;
            this.request = 
                op.getRequest() == null 
                ? null
                : new SDBRequest(op.getRequest().getStore(), pmap, context.getContext()) ;
        }
    }
    
    @Override
    protected QueryIterator nextStage(Binding binding) {
        OpSQL execSQL = this.opSQL ;

        if ( binding != null && ! isRoot(binding) ) {
            QueryCompiler qc = opSQL.getRequest().getStore().getQueryCompilerFactory().createQueryCompiler(request) ;
            Op op2 = Substitute.substitute(opSQL.getOriginal(), binding) ;
            Op op = qc.compile(op2) ;
            if ( op instanceof OpSQL )
                execSQL = (OpSQL)op ;
            else
                throw new SDBInternalError("Failed to recompile the OpSQL to an OpSQL") ;
        }

        return execSQL.exec(binding, getExecContext()) ;
//        QueryIterator qIter = execSQL.exec(binding, getExecContext()) ;
//        List<Binding> x = Iter.toList(qIter) ;
//        qIter = new QueryIterPlainWrapper(x.iterator(), getExecContext()) ;
//        System.out.println("SQL Eval:") ;
//        x.forEach(b -> System.out.println("  "+b) );
//        System.out.println() ;
//        return qIter ;
    }

    private static boolean isRoot(Binding binding) {
        return !binding.vars().hasNext() ;
    }

    @Override
    public void output(IndentedWriter out) {
        opSQL.output(out) ;
    }
}
