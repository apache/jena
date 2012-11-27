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

package com.hp.hpl.jena.sparql.engine;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCloseable ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class PlanOp extends PlanBase
{
    private QueryIterator qIter ;
    private String label = null ;
    
    public PlanOp(Op op, Closeable closeable, QueryIterator qIter)
    { 
        super(op, closeable) ;
        //this.qIter = qIter ;
        // Catch the close and close the plan. 
        this.qIter = new QueryIteratorCloseable(qIter, this) ;
    }

    public PlanOp(String label, Closeable closeable, Op op, QueryIterator qIter)
    {
        this(op, closeable, qIter) ;
        this.label = label ;
    }
    
    @Override
    protected QueryIterator iteratorOnce()
    { return qIter ; }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        if ( getOp() == null )
        {
            out.println(Utils.className(this)) ;
            return ;
        }

        String str = label ;

        if ( label == null )
            str = "Plan" ;
        out.print(Plan.startMarker) ;
        out.println(str) ;
        out.incIndent() ;
        //getOp().output(out, sCxt) ;
        qIter.output(out, sCxt) ;

        out.print(Plan.finishMarker) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }
}
