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

package org.apache.jena.sparql.procedure ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.writers.WriterExpr ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.IterLib ;
import org.apache.jena.sparql.util.PrintSerializableBase ;

public abstract class ProcedureBase extends PrintSerializableBase implements Procedure
{
    private Node procId ;
    private ExprList args ;

    @Override
    public void build(Node procId, ExprList args, ExecutionContext execCxt)
    {
        this.procId = procId ;
        this.args = args ;
    }
 
    @Override
    public final QueryIterator proc(QueryIterator input, ExecutionContext execCxt)
    {
        return new RepeatApplyIteratorProc(input, execCxt) ;
    }
    
    public abstract QueryIterator exec(Binding binding, Node name, ExprList args, ExecutionContext execCxt) ;
    
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.print("Procedure ["+FmtUtils.stringForNode(procId, sCxt)+"]") ;
        out.print("[") ;
        out.print(args.toString()) ;
        out.print("]") ;
        out.println() ;
    }
    
    class RepeatApplyIteratorProc extends QueryIterRepeatApply
    {
        private ExecutionContext execCxt ;
        private Node name ;
        
       public RepeatApplyIteratorProc(QueryIterator input, ExecutionContext execCxt)
       { 
           super(input, execCxt) ;
       }

        @Override
        protected QueryIterator nextStage(Binding binding)
        {
            QueryIterator iter = exec(binding, name, args, super.getExecContext()) ;
            if ( iter == null ) 
                iter = IterLib.noResults(execCxt) ;
            return iter ;
        }
        
        @Override
        protected void details(IndentedWriter out, SerializationContext sCxt)
        {
            out.print("Procedure ["+FmtUtils.stringForNode(name, sCxt)+"]") ;
            WriterExpr.output(out, args, sCxt) ;
            out.println() ;
        }
    }

}
