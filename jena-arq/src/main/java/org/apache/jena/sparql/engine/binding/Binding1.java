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
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.itr.Itr;

/**
 * Special purpose binding for just one name/value slot.
 */
public class Binding1 extends BindingBase {
    private final Var  var;
    private final Node value;

    /* package */ Binding1(Binding parent, Var var, Node value) {
        super(parent);
        this.var = Objects.requireNonNull(var);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    protected int size1() {
        return 1;
    }

    @Override
    protected boolean isEmpty1() {
        return false;
    }

    @Override
    protected Iterator<Var> vars1() {
        return Itr.iter1(var);
    }

    @Override
    protected void forEach1(BiConsumer<Var, Node> action) {
        action.accept(var, value);
    }

    @Override
    protected boolean contains1(Var n) {
        return var.equals(n);
    }

    @Override
    protected Node get1(Var v) {
        if ( v.equals(var) )
            return value;
        return null;
    }
}
