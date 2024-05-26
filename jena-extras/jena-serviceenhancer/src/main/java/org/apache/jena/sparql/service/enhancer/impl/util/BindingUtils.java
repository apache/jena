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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class BindingUtils {
    /** Return a new binding that projects all variables having non-null values */
    public static Binding project(Binding binding, Iterator<Var> vars) {
        BindingBuilder builder = BindingBuilder.create();

        while (vars.hasNext()) {
            Var var = vars.next();
            Node node = binding.get(var);
            if (node != null) {
                builder.add(var, node);
            }
        }

        return builder.build();
    }

    /** Return a new binding with all non-null-valued and non-excluded variables projected */
    public static Binding project(Binding binding, Iterator<Var> vars, Var exclusion) {
        return project(binding, Iter.notFilter(vars, v -> Objects.equals(v, exclusion)));
    }

    /** Return a new binding with all non-null-valued and non-excluded variables projected */
    public static Binding project(Binding binding, Iterator<Var> vars, Set<Var> exclusions) {
        return project(binding, Iter.notFilter(vars, exclusions::contains));
    }

    public static <C extends Collection<Var>> C addAll(C acc, Binding binding) {
        Iterator<Var> it = binding.vars();
        while (it.hasNext()) {
            Var v = it.next();
            acc.add(v);
        }
        return acc;
    }

    public static <C extends Collection<Var>> C varsMentioned(C out, Iterator<Binding> it) {
        while (it.hasNext()) {
            Binding b = it.next();
            CollectionUtils.addAll(out, b.vars());
        }
        return out;
    }

    /** Transform the keys of a binding w.r.t. the given varMap */
    public static Binding renameKeys(Binding binding, Map<Var, Var> varMap) {
        return NodeTransformLib.transform(binding, new NodeTransformSubst(varMap));
    }

    /** Return the set of all variables mentioned in a collection of bindings */
    public static Set<Var> varsMentioned(Iterable<Binding> bindings) {
        Set<Var> result = new LinkedHashSet<>();
        return varsMentioned(result, bindings.iterator());
    }

    public static Set<Var> varsMentioned(Binding binding) {
        Set<Var> result = new LinkedHashSet<>();
        binding.vars().forEachRemaining(result::add);
        return result;
    }

    /** Attempt to extract a number from a binding and a variable.
     * If the node value is null then null is returned.
     * If the node value is not a number literal then an {@link ExprEvalException} is raised. */
    public static Number getNumberOrNull(Binding binding, Var var) {
        Node node = binding.get(var);
        Number result = NodeUtilsExtra.getNumberOrNull(node);
        return result;
    }

    /** Get a binding's values for var as a number using {@link #getNumberOrNull(Binding, Var)}.
     * Raises an {@link NullPointerException} if no number can be obtained */
    public static Number getNumber(Binding binding, Var var) {
        return Objects.requireNonNull(getNumberOrNull(binding, var), "Number must not be null");
    }
}
