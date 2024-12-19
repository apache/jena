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

import java.util.Iterator;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.serializer.SerializationContext ;

/** Execute each sub stage against the input.
 *  Streamed SPARQL Union. */

public class QueryIterUnion extends QueryIterRepeatApply
{
    protected List<Op> subOps  ;

    public QueryIterUnion(QueryIterator input,
                          List<Op> subOps,
                          ExecutionContext context)
    {
        super(input, context) ;
        this.subOps = subOps ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        Iterator<Op> subOpIt = subOps.iterator();
        return new QueryIter(getExecContext()) {
            QueryIterator qIter = null;

            @Override
            protected void requestCancel() {
                performRequestCancel(qIter);
            }

            @Override
            protected Binding moveToNextBinding() {
                return qIter.next();
            }

            @Override
            protected boolean hasNextBinding() {
                for (;;) {
                    if (qIter != null) {
                        if (qIter.hasNext()) {
                            return true;
                        } else {
                            qIter.close();
                            qIter = null;
                        }
                    } else {
                        if (subOpIt.hasNext()) {
                            Op subOp = subOpIt.next();
                            qIter = QC.execute(subOp, binding, getExecContext());
                        } else {
                            return false;
                        }
                    }
                }
            }

            @Override
            protected void closeIterator() {
                performClose(qIter);
            }

            @Override
            public void output(IndentedWriter out, SerializationContext sCxt) {
                QueryIterator subIter = qIter;
                out.println(Lib.className(this) + "/" + Lib.className(subIter));
                if (subIter != null) {
                    out.incIndent();
                    subIter.output(out, sCxt);
                    out.decIndent();
                }
            }
        };
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Lib.className(this)) ;
        out.incIndent() ;
        for (Op op : subOps)
            op.output(out, sCxt) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }
}
