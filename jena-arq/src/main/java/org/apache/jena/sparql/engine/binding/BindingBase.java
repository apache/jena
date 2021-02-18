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
import java.util.function.BiConsumer;

import org.apache.jena.atlas.iterator.IteratorConcat;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.FmtUtils;


/** Machinery encapsulating a mapping from a name to a value.
 *  The "parent" is a shared, immutable, common set of bindings.
 *  An association of var/node must not override a setting in the parent.
 */

abstract public class BindingBase implements Binding
{
    // This is a set of bindings, each binding being one pair (var, value).
    protected Binding parent;

    protected BindingBase(Binding _parent) {
        parent = _parent;
    }

//    @Override
//    public Binding getParent() { return parent; }

    @Override
    final public Iterator<Var> vars()
    {
        Iterator<Var> iter = vars1();
        if ( parent != null )
            iter = IteratorConcat.concat(parent.vars(), iter);
        return iter;
    }

    /** Operate on each entry. */
    @Override
    public void forEach(BiConsumer<Var, Node> action) {
        forEach1(action);
        if ( parent != null )
            parent.forEach(action);
    }

    protected void forEach1(BiConsumer<Var, Node> action) {
        Iterator<Var> vIter = vars1();
        while(vIter.hasNext() ) {
            Var v = vIter.next();
            Node n = get(v);
            action.accept(v, n);
        }
    }

    protected abstract Iterator<Var> vars1();

    @Override
    final public int size() {
        int x = size1();
        if ( parent != null )
            x = x + parent.size();
        return x;
    }

    protected abstract int size1();

    @Override
    public boolean isEmpty() {
        if ( !isEmpty1() )
            return false;
        if ( parent == null )
            return true;
        return parent.isEmpty();
    }

    protected abstract boolean isEmpty1();

    /** Test whether a name is bound to some object */
    @Override
    final public boolean contains(Var var) {
        if ( contains1(var) )
            return true;
        if ( parent == null )
            return false;
        return parent.contains(var);
    }

    protected abstract boolean contains1(Var var);

    /** Return the object bound to a name, or null */
    @Override
    final public Node get(Var var) {
        Node node = get1(var);

        if ( node != null )
            return node;

        if ( parent == null )
            return null;

        return parent.get(var);

    }

    protected abstract Node get1(Var var);

    @Override
    public String toString() {
        StringBuffer sbuff = new StringBuffer();
        format1(sbuff);

        if ( parent != null ) {
            String tmp = parent.toString();
            if ( tmp != null && (tmp.length() != 0) ) {
                sbuff.append(" -> ");
                sbuff.append(tmp);
            }
        }
        return sbuff.toString();
    }

    // Do one level of binding
    public void format1(StringBuffer sbuff) {
        if ( isEmpty() ) {
            sbuff.append("()");
            return;
        }

        String sep = "";
        for ( Iterator<Var> iter = vars1() ; iter.hasNext() ; ) {
            Object obj = iter.next();
            Var var = (Var)obj;

            sbuff.append(sep);
            sep = " ";
            fmtVar(sbuff, var);
        }
    }

    protected void fmtVar(StringBuffer sbuff, Var var) {
        Node node = get(var);
        String tmp = FmtUtils.stringForObject(node);
        sbuff.append("( ?" + var.getVarName() + " = " + tmp + " )");
    }

    // Do one level of binding
    public String toString1() {
        StringBuffer sbuff = new StringBuffer();
        format1(sbuff);
        return sbuff.toString();
    }

    @Override
    public int hashCode() {
        return hashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        if ( this == other )
            return true;
        if ( !(other instanceof Binding) )
            return false;
        Binding binding = (Binding)other;
        return BindingLib.equals(this, binding);
    }

    // Not everything derives from BindingBase.
    public static int hashCode(Binding bind) {
        int hash = 0xC0;
        for ( Iterator<Var> iter = bind.vars() ; iter.hasNext() ; ) {
            Var var = iter.next();
            Node node = bind.get(var);
            hash ^= var.hashCode();
            hash ^= node.hashCode();
        }
        return hash;
    }
}
