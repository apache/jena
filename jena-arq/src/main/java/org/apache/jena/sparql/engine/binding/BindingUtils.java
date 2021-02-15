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

package org.apache.jena.sparql.engine.binding;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;

public class BindingUtils {
    /** Convert a query solution to a binding
     * @deprecated Use {@link BindingLib#asBinding(QuerySolution)} instead*/
    public static Binding asBinding(QuerySolution qSolution) {
        return BindingLib.asBinding(qSolution);
    }

    /**
     * @deprecated Use {@link BindingLib#toBinding(QuerySolution)} instead
     */
    public static Binding toBinding(QuerySolution qSolution) {
        return BindingLib.toBinding(qSolution);
    }

    /** @deprecated Switch from {@link BindingMap} to using {@link BindingBuilder}. */
    @Deprecated
    public static void addAll(BindingMap dest, Binding src) {
        Iterator<Var> iter = src.vars();
        for ( ; iter.hasNext() ; ) {
            Var v = iter.next();
            Node n = src.get(v);
            dest.add(v, n);
        }
    }
}
