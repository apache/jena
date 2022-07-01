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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.solver.PatternMatchData;
import org.apache.jena.sparql.serializer.SerializationContext;

/**
 * Like {@link QueryIterBlockTriples} except it can process triple term patterns (RDF-star)
 * as well.
 * @deprecated Use {@link PatternMatchData}.
 */
@Deprecated
public class QueryIterBlockTriplesStar extends QueryIter1 {

    public static QueryIterator create(QueryIterator input, BasicPattern pattern, ExecutionContext execCxt) {
        return PatternMatchData.execute(execCxt.getActiveGraph(), pattern, input, null, execCxt);
    }

    private final BasicPattern pattern;
    private QueryIterator output;

    private QueryIterBlockTriplesStar(QueryIterator input, BasicPattern pattern, ExecutionContext execContext) {
        super(input, execContext);
        this.pattern = pattern;

        Graph graph = execContext.getActiveGraph();
        output = PatternMatchData.execute(graph, pattern, input, null, execContext);
    }

    @Override
    protected boolean hasNextBinding() {
        return output.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return output.nextBinding();
    }

    @Override
    protected void closeSubIterator() {
        if ( output != null )
            output.close();
        output = null;
    }

    @Override
    protected void requestSubCancel() {
        if ( output != null )
            output.cancel();
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print(this.getClass().getSimpleName());
        if ( pattern.isEmpty() ) {
            out.println(": Empty pattern");
            return;
        }
        out.println();
        out.incIndent();
        out.print(pattern);
        out.decIndent();
        out.println();
    }
}
