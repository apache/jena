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
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/**
 * Interface encapsulating a mapping from a name to a value.
 * <p>
 * A Binding is a map from {@link Var} to {@link Node}.
 * It can have a parent, meaning this binding extends (adds new var/node pairs) another.
 * The operation {@link #get} looks in this binding, then looks in the parent recursively.
 *
 * Bindings are immutable.
 * Bindings provide value-based equality and hash code.
 *
 * @see BindingBuilder
 * @see BindingFactory
 */

public interface Binding
{
    /** Create a {@link BindingBuilder} */
    public static BindingBuilder builder() {
        return new BindingBuilder();
    }

    /** Create a {@link BindingBuilder} */
    public static BindingBuilder builder(Binding parent) {
        return new BindingBuilder(parent);
    }

    public static Binding noParent = null;
//    public Binding getParent();

    /** Iterate over all variables of this binding. */
    public Iterator<Var> vars();

    /** Operate on each entry. */
    public void forEach(BiConsumer<Var, Node> action);

    /** Test whether a variable is bound to some object */
    public boolean contains(Var var);

    /** Return the object bound to a variable, or null */
    public default Node get(String varName) {
        return get(Var.alloc(varName));
    }

    /** Test whether a variable is bound to some object */
    public default boolean contains(String varName) {
        return contains(Var.alloc(varName));
    }

    /** Return the object bound to a variable, or null */
    public Node get(Var var);

    /** Number of (var, value) pairs. */
    public int size();

    /** Is this an empty binding?  No variables. */
    public boolean isEmpty();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object other);
}
