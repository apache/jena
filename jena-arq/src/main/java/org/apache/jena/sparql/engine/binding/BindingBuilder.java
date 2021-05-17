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

import static java.lang.String.format;

import java.util.*;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.itr.Itr;
import org.apache.jena.sparql.util.FmtUtils;

/**
 * Build Bindings.
 * <p>
 * {@linkplain Binding} are immutable and can not be changed once constructed. A
 * {@code BindingBuilder} accumulates some var/node pairs and, at the build step,
 * chooses the most efficient {@linkplain Binding} implementation.
 *
 * <pre>
 *   Binding binding = Binding.builder()
 *                        .add(var, value)
 *                        ...
 *                        .build();
 * </pre>
 *
 * The {@link #build()} method can only be called once per binding constructed. A
 * {@link BindingBuilder} can be reused but is not thread safe.
 * <p>Example:
 * <pre>
 *   BindingBuilder builder = Binding.builder(parent);
 *   for .... {
 *
 *      builder.reset()
 *      for ( ... each variable ... ) {
 *          builder.add(var, node)
 *      }
 *      Binding binding = builder.build();
 *      ... accumulate binding ...
 *   }
 * </pre>
 * <p>Example:
 * <pre>
 *   BindingBuilder builder = Binding.builder(parent);
 *   for .... {
 *      Binding binding =
 *          builder.reset()
 *             .add(var1, node1)
 *             .add(var2, node2)
 *             .add(var3, node3)
 *             .build();
 *      }
 *      ... accumulate binding ...
 *   }
 * </pre>
 *
 * See also {@link BindingFactory}. When the number of var/node pairs is fixed and
 * known, operations in {@link BindingFactory} directly create a binding without
 * needing a builder.
 */
public class BindingBuilder
{
    // Do not implement .hashCode or .equals as value-based operations.
    // Do not implement Binding or else it makes the coding accident of returning a
    // BindingBuilder as a Binding result rather easy.

    public static final Binding noParent = null;
    /*package*/ static boolean CHECKING = true;
    /*package*/ static final boolean UNIQUE_NAMES_CHECK = true;
    // Additional checking for development. Set false in production.
    /*package*/ static final boolean UNIQUE_NAMES_CHECK_PARENT = false;

    private boolean haveBuilt = false;
    protected final Binding parent;

    // Optimise up to 4 slots
    private Var  var1  = null;
    private Node node1 = null;

    private Var  var2  = null;
    private Node node2 = null;

    private Var  var3  = null;
    private Node node3 = null;

    private Var  var4  = null;
    private Node node4 = null;

    // More than 4
    private Map<Var,Node> map = null;

    private int countSlots() {
        if ( map != null ) return map.size();
        if ( var1 == null ) return 0;
        if ( var2 == null ) return 1;
        if ( var3 == null ) return 2;
        if ( var4 == null ) return 3;
        if ( map == null ) return 4;
        throw new InternalErrorException("Inconsistent internal state");
    }

    public static BindingBuilder create() { return new BindingBuilder(); }
    public static BindingBuilder create(Binding parent) { return new BindingBuilder(parent); }

    /** Create via {@link #create()} */
    /*package*/ BindingBuilder() { this(noParent); }

    /** Create via {@link #create(Binding)} */
    /*package*/ BindingBuilder(Binding parent) {
        //super(parent);
        this.parent = parent;
    }

    /**
     * Accumulate (var,value) pairs.
     * Allow binding in this level to be replaced (i.e. not in parent)
     */
    public BindingBuilder set(Var var, Node node) {
        checkAdd(var, node, true);
        set$(var, node);
        return this;
    }

    // Accumulate (var,value) pairs.
    public BindingBuilder add(Var var, Node node) {
        checkAdd(var, node, false);
        add$(var, node);
        return this;
    }

    /** Add all the (var, value) pairs from another binding */
    public BindingBuilder addAll(Binding other) {
        Iterator<Var> vIter = other.vars();
        for (; vIter.hasNext(); ) {
            Var v = vIter.next();
            Node n = other.get(v);
            add(v,n);
        }
        return this;
    }

    private void add$(Var var, Node node)  {
        if ( haveBuilt )
            throw new IllegalStateException("Attempt to add a binding pair after build() without reset()");

        if ( map != null ) {
            map.put(var, node);
            return;
        }
        if ( var1 == null ) {
            var1 = var;
            node1 = node;
            return;
        }
        if ( var2 == null ) {
            var2 = var;
            node2 = node;
            return;
        }
        if ( var3 == null ) {
            var3 = var;
            node3 = node;
            return;
        }
        if ( var4 == null ) {
            var4 = var;
            node4 = node;
            return;
        }
        // Spill from 4 to N. map is null.
        map = new HashMap<>();
        map.put(var1, node1);
        map.put(var2, node2);
        map.put(var3, node3);
        map.put(var4, node4);
        map.put(var, node);
        var1 = null;
        node1 = null;
        var2 = null;
        node2 = null;
        var3 = null;
        node3 = null;
        var4 = null;
        node4 = null;
    }

    private void set$(Var var, Node node)  {
        if ( haveBuilt )
            throw new IllegalStateException("Attempt to set a binding pair after build() without reset()");

        if ( map != null ) {
            map.put(var, node);
            return;
        }
        if ( var1 == null || var.equals(var1) ) {
            var1 = var;
            node1 = node;
            return;
        }
        if ( var2 == null || var.equals(var2) ) {
            var2 = var;
            node2 = node;
            return;
        }
        if ( var3 == null || var.equals(var3) ) {
            var3 = var;
            node3 = node;
            return;
        }
        if ( var4 == null || var.equals(var4) ) {
            var4 = var;
            node4 = node;
            return;
        }
        // Spill from 4 to N.
        // map is null.
        map = new HashMap<>();
        map.put(var1, node1);
        map.put(var2, node2);
        map.put(var3, node3);
        map.put(var4, node4);
        var1 = null;
        node1 = null;
        var2 = null;
        node2 = null;
        var3 = null;
        node3 = null;
        var4 = null;
        node4 = null;
        map.put(var, node);
    }

//    public void setParent(Binding parent) {
//        if ( this.parent != null )
//            throw new IllegalStateException("Parent already set");
//        this.parent = parent;
//    }

    /** Get or return the variable. */
    public Node getOrSame(Var var) {
        Node x = get1(var);
        if ( x == null )
            x = parent.get(var);
        return x == null ? var : x;
    }

    private void requireBeingBuilt(String method) {
        if ( haveBuilt )
            throw new IllegalStateException("Method "+method+" called after .build(), before a .reset()");
    }


    public Node get(Var var) {
        Node x = get1(var);
        if ( x == null && parent != null )
            x = parent.get(var);
        return x;
    }

    public boolean contains(Var var) {
        boolean x = contains1(var);
        if ( x )
            return true;
        if ( parent == null )
            return false;
        return parent.contains(var);
    }

    private Node get1(Var var) {
        requireBeingBuilt("get1");
        if ( var == null )
            return null;
        if ( map != null )
            return map.get(var);

        if ( var1 == null )
            return null;
        if ( var.equals(var1) )
            return node1;

        if ( var2 == null )
            return null;
        if ( var.equals(var2) )
            return node2;

        if ( var3 == null )
            return null;
        if ( var.equals(var3) )
            return node3;

        if ( var4 == null )
            return null;
        if ( var.equals(var4) )
            return node4;

        return null;
    }

    private boolean contains1(Var var) {
        Objects.requireNonNull(var);
        requireBeingBuilt("contains1");
        if ( map != null )
            return map.containsKey(var);

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

        if ( var4 == null )
            return false;
        if ( var.equals(var4) )
            return true;

        return false;
    }

    // This class does not provide binding-like functions to include the parent.Unused but available for adding Binding-like functionality.
    private int size1() {
        requireBeingBuilt("size1");
        return countSlots();
    }

    public boolean isEmpty() {
        if ( !isEmpty1() )
            return false;
        if ( parent == null )
            return true;
        return parent.isEmpty();
    }

    private boolean isEmpty1() {
        requireBeingBuilt("isEmpty1");
        return var1 == null && map == null;
    }

    /** Variables in the builder - does not include the parent. */
    public Iterator<Var> vars1() {
        requireBeingBuilt("vars1");
        if ( map != null )
            // Copy - to allow modification of the builder.
            return new ArrayList<>(map.keySet()).iterator();
        if ( var4 != null ) return Itr.iter4(var1, var2, var3, var4);
        if ( var3 != null ) return Itr.iter3(var1, var2, var3);
        if ( var2 != null ) return Itr.iter2(var1, var2);
        if ( var1 != null ) return Itr.iter1(var1);
        return Itr.iter0();
    }

    private void forEach1(BiConsumer<Var, Node> action) {
        requireBeingBuilt("forEach1");
        vars1().forEachRemaining(v -> {
            Node n = get1(v);
            action.accept(v, n);
        });
    }

    private void checkAdd( Var var, Node node, boolean parentOnly ) {
        if ( ! CHECKING )
            return;
        if ( var == null )
            throw new NullPointerException("check("+var+", "+node+"): null var" );
        if ( node == null )
            throw new NullPointerException("check("+var+", "+node+"): null node value" );

        if ( parent != null && UNIQUE_NAMES_CHECK_PARENT && parent.contains(var) ) {
            throw new IllegalArgumentException("Attempt to reassign parent variable '"+var+
                                               "' from '"+FmtUtils.stringForNode(parent.get(var))+
                                               "' to '"+FmtUtils.stringForNode(node)+"'");
        }

        if ( parentOnly )
            return;

        if ( UNIQUE_NAMES_CHECK && contains1(var) )
            throw new IllegalArgumentException("Attempt to reassign '"+var+
                                               "' from '"+FmtUtils.stringForNode(get(var))+
                                               "' to '"+FmtUtils.stringForNode(node)+"'");
    }

    /** Reset the builder state, while keeping the parent */
    public BindingBuilder reset() {
        clear();
        haveBuilt = false;
        return this;
    }

    private void clear() {
        var1 = null;
        node1 = null;
        var2 = null;
        node2 = null;
        var3 = null;
        node3 = null;
        var4 = null;
        node4 = null;
        map = null;
    }


    /**
     * Construct the binding. This can be called only once for a binding but the
     * builder can be reused after a call to {@link #reset()}.
     */
    public Binding build() {
        if ( haveBuilt )
            throw new IllegalStateException("Already built (need to call .reset()?");
        Binding b = construct(true);
        haveBuilt = true;
        return b;
    }

    /**
     * Build a view of the current state.
     * <p>
     * Do not modify the builder while this binding is in use.
     * Changes to the builder make this view unstable.
     * To create a finished binding, call {@link #build}.
     */
    public Binding snapshot() {
        return construct(false);
    }

    private Binding construct(boolean isFinal) {
        if ( map != null ) {
            Map<Var, Node> m = map;
            // If pure isolation. The contract in the javadoc makes this unnecessary.
//            if ( !isFinal )
//                // Snapshot - need to be a safe copy.
//                m = new HashMap<>(map);
            return new BindingOverMap(parent, m);
        }
        if ( var4 != null )
            return new Binding4(parent, var1, node1, var2, node2, var3, node3, var4, node4);
        if ( var3 != null )
            return new Binding3(parent, var1, node1, var2, node2, var3, node3);
        if ( var2 != null )
            return new Binding2(parent, var1, node1, var2, node2);
        if ( var1 != null )
            return new Binding1(parent, var1, node1);
        return new Binding0(parent);
    }

    @Override
    public String toString() {
        if ( isEmpty1() )
            return "<empty>"+parentStr(parent);
        StringJoiner sj = new StringJoiner(" ", "( ", " )");
        forEach1((v,n)->sj.add(format("%s=>%s", v,n)));
        return sj.toString()+parentStr(parent);
    }

    private static String parentStr(Binding parent) {
        if ( parent == noParent )
            return "";
        return " -> "+parent.toString();
    }
}
