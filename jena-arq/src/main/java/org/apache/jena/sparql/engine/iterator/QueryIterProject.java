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

import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingProject;
import org.apache.jena.sparql.serializer.SerializationContext;

public class QueryIterProject extends QueryIterConvert {

    public static QueryIterator create(QueryIterator input, List<Var> vars, ExecutionContext qCxt) {
        return new QueryIterProject(input, vars, qCxt);
    }

    private List<Var> projectionVars;

    private QueryIterProject(QueryIterator input, List<Var> vars, ExecutionContext qCxt) {
        super(input, project(vars, qCxt), qCxt);
        projectionVars = vars;
    }

    static QueryIterConvert.Converter project(List<Var> vars, ExecutionContext qCxt) {
        return new Projection(vars, qCxt);
    }

    public List<Var> getProjectionVars() {
        return projectionVars;
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print(Lib.className(this));
        out.print(" ");
        ListUtils.print(out, projectionVars);
    }

    static class Projection implements QueryIterConvert.Converter {
        List<Var> projectionVars;

        Projection(List<Var> vars, ExecutionContext qCxt) {
            this.projectionVars = vars;
        }

        @Override
        public Binding convert(Binding bind) {
            return new BindingProject(projectionVars, bind);
        }
    }
}
