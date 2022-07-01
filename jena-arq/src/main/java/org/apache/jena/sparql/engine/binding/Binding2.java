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

import java.util.*;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.itr.Itr;

/**
 * Special purpose binding for two name/value slots.
 */
public class Binding2 extends BindingBase
{
    private final Var  var1;
    private final Node value1;

    private final Var  var2;
    private final Node value2;

    /* package */ Binding2(Binding parent, Var var1, Node value1, Var var2, Node value2) {
        super(parent);
        this.var1 = Objects.requireNonNull(var1);
        this.value1 = Objects.requireNonNull(value1);
        this.var2 = Objects.requireNonNull(var2);
        this.value2 = Objects.requireNonNull(value2);
    }

    @Override
    protected int size1() { return 2; }

    @Override
    protected boolean isEmpty1() { return false; }

    @Override
    protected Iterator<Var> vars1() {
        return Itr.iter2(var1, var2);
    }

    @Override
    protected void forEach1(BiConsumer<Var, Node> action) {
        action.accept(var1, value1);
        action.accept(var2, value2);
    }

    @Override
    protected boolean contains1(Var n) {
        if ( var1.equals(n) || var2.equals(n) )
            return true;
        return false;
    }

    @Override
    protected Node get1(Var v)
    {
        if ( v.equals(var1) )
            return value1;
        if ( v.equals(var2) )
            return value2;
        return null;
    }
}
