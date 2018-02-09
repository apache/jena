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

import java.util.Iterator ;
import java.util.Objects;

import org.apache.jena.atlas.iterator.IteratorConcat ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.util.FmtUtils ;


/** Machinary encapsulating a mapping from a name to a value.
 *  The "parent" is a shared, immutable, common set of bindings.
 *  An association of var/node must not override a setting in the parent.
 *  
 *  @see BindingFactory
 *  @see BindingMap for mutable bindings.
 */


abstract public class BindingBase implements Binding
{
    static final boolean CHECKING = true ;
    /*package*/ static final boolean UNIQUE_NAMES_CHECK = false ;
    
    // This is a set of bindings, each binding being one pair (var, value).
    protected final Binding parent ;

    protected BindingBase(Binding _parent) {
        parent = _parent;
    }

    public Binding getParent() {
        return parent;
    }
    
    /** Iterate over all the names of variables. */
    @Override
    final public Iterator<Var> vars() {
        // Hidesight - replace with accumulator style vars1(accumulator)
        Iterator<Var> iter = vars1();
        if ( parent != null )
            iter = IteratorConcat.concat(parent.vars(), iter);
        return iter;
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
    public boolean contains(Var var) {
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

    protected static void checkPair(Var var, Node node) {
        if ( !BindingBase.CHECKING )
            return;
        if ( var == null )
            throw new ARQInternalErrorException("check(" + var + ", " + node + "): null var");
        if ( node == null )
            throw new ARQInternalErrorException("check(" + var + ", " + node + "): null node value");
    }

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
        String sep = "";
        for ( Iterator<Var> iter = vars1() ; iter.hasNext() ; ) {
            Object obj = iter.next();
            Var var = (Var)obj;

            sbuff.append(sep);
            sep = " ";
            format(sbuff, var);
        }
    }

    protected void format(StringBuffer sbuff, Var var) {
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
        return equals(this, binding);
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

    public static boolean equals(Binding bind1, Binding bind2) {
        if ( bind1 == bind2 )
            return true;

        // Same variables?

        if ( bind1.size() != bind2.size() )
            return false;

        for ( Iterator<Var> iter1 = bind1.vars() ; iter1.hasNext() ; ) {
            Var var = iter1.next();
            Node node1 = bind1.get(var);
            Node node2 = bind2.get(var);
            if ( !Objects.equals(node1, node2) )
                return false;
        }

        // No need to check the other way round as the sizes matched.
        return true;
    }
}
