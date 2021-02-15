/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.sparql.engine.binding;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.itr.Itr;

/**
 * A binding implementation that supports 3 binding pairs.
 */
public class Binding3 extends BindingBase {
    private final Var  var1;
    private final Node value1;

    private final Var  var2;
    private final Node value2;

    private final Var  var3;
    private final Node value3;

    /*package*/ Binding3(Binding _parent, Var var1, Node value1, Var var2, Node value2, Var var3, Node value3) {
        super(_parent);
        this.var1 = Objects.requireNonNull(var1);
        this.value1 = Objects.requireNonNull(value1);
        this.var2 = Objects.requireNonNull(var2);
        this.value2 = Objects.requireNonNull(value2);
        this.var3 = Objects.requireNonNull(var3);
        this.value3 = Objects.requireNonNull(value3);
    }

    @Override
    protected Iterator<Var> vars1() {
        return Itr.iter3(var1, var2, var3);
    }

    @Override
    protected void forEach1(BiConsumer<Var, Node> action) {
        action.accept(var1, value1);
        action.accept(var2, value2);
        action.accept(var3, value3);
    }

    protected Var getVar1(int idx) {
        if ( idx == 0 )
            return var1;
        if ( idx == 1 )
            return var2;
        if ( idx == 2 )
            return var3;
        return null;
    }

    @Override
    protected int size1() {
        if ( var1 == null )
            return 0;
        if ( var2 == null )
            return 1;
        if ( var3 == null )
            return 2;
        return 3;
    }

    @Override
    protected boolean isEmpty1() {
        return var1 == null;
    }

    @Override
    protected boolean contains1(Var var) {
        if ( var == null )
            return false;

        if ( var1 == null )
            return false;
        if ( var.equals(var1) )
            return true;

        if ( var2 == null )
            return false;
        if ( var.equals(var2) )
            return true;

        if ( var3 == null )
            return false;
        if ( var.equals(var3) )
            return true;

        return false;
    }

    @Override
    protected Node get1(Var var) {
        if ( var == null )
            return null;

        if ( var1 == null )
            return null;
        if ( var.equals(var1) )
            return value1;

        if ( var2 == null )
            return null;
        if ( var.equals(var2) )
            return value2;

        if ( var3 == null )
            return null;
        if ( var.equals(var3) )
            return value3;

        return null;
    }
}
