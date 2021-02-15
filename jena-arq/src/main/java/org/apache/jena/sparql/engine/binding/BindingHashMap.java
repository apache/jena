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

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.util.FmtUtils ;

/** A mutable mapping from a name to a value such that we can create a tree of levels
 *  with higher (earlier levels) being shared.
 *  Looking up a name is done by looking in the current level,
 *  then trying the parent is not found.
 *  @deprecated Use {@link BindingBuilder}; avoid mutable bindings.
 */
@Deprecated
public class BindingHashMap extends BindingBase implements BindingMap
{
    // Bindings are often small.  Is this overkill?
    private Map<Var, Node> map = new HashMap<>() ;

    /** Using BindingFactory.create is better */
    public BindingHashMap(Binding parent) { super(parent) ; }
    /** Using BindingFactory.create is better */
    public BindingHashMap() { super(BindingRoot.create()) ; }

    /** Add a (name,value) */

    protected void add1(Var var, Node node) {
        if ( !Var.isAnonVar(var) )
            map.put(var, node);
    }

    @Override
    protected int size1() {
        return map.size();
    }

    @Override
    protected boolean isEmpty1() {
        return map.isEmpty();
    }

    /**
     * Iterate over all the names of variables.
     */
    @Override
    public Iterator<Var> vars1() {
        // Assumes that varnames are NOT duplicated.
        Iterator<Var> iter = map.keySet().iterator();
        return iter;
    }

    @Override
    public boolean contains1(Var var) {
        return map.containsKey(var);
    }

    @Override
    public Node get1(Var var) {
        return map.get(var);
    }

    /** Add a (var,value) - the node value is never null */
    @Override
    final public void add(Var var, Node node) {
        checkAdd(var, node);
        add1(var, node);
    }

    @Override
    final public void addAll(Binding other) {
        BindingUtils.addAll(this, other);
    }

    private void checkAdd(Var var, Node node) {
        checkPair(var, node);
        if ( BindingBuilder.UNIQUE_NAMES_CHECK && contains(var) )
            throw new ARQInternalErrorException("Attempt to reassign '" + var + "' from '" + FmtUtils.stringForNode(get(var)) + "' to '"
                                                + FmtUtils.stringForNode(node) + "'");
        // Let the implementation do a check as well.
        checkAdd1(var, node);
    }

    protected static void checkPair(Var var, Node node) {
        if ( !BindingBuilder.CHECKING )
            return;
        if ( var == null )
            throw new ARQInternalErrorException("check(" + var + ", " + node + "): null var");
        if ( node == null )
            throw new ARQInternalErrorException("check(" + var + ", " + node + "): null node value");
    }


    protected void checkAdd1(Var v, Node node) {}
}
