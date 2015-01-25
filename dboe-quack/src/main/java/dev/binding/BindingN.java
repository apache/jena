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

package dev.binding ;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;

/**
 * A binding implementation that supports 0, 1, 2, or 3 bindings only but is
 * very compact.
 */
public class BindingN extends BindingBase implements BindingMap {
    /*package*/ BindingN() {
        this(null) ;
    }

    /*package*/ BindingN(Binding _parent) {
        super(_parent) ;
    }

    // Cost = object overhead + parent + 3*var + 3*node always.
    // Or dynamically switch to using a pair of [].
    // Or switch to a single binding unit (cost = object overhead + parent + var
    // + node -> not much)

    private Var  var1  = null ;
    private Node node1 = null ;

    private Var  var2  = null ;
    private Node node2 = null ;

    private Var  var3  = null ;
    private Node node3 = null ;

    @Override
    protected Iterator<Var> vars1() {
        return null ;
    }

    public Var getVar(int idx) {
        if ( idx == 0 )
            return var1 ;
        if ( idx == 1 )
            return var2 ;
        if ( idx == 2 )
            return var3 ;
        return null ;
    }

    public Node getNode(int idx) {
        if ( idx == 0 )
            return node1 ;
        if ( idx == 1 )
            return node2 ;
        if ( idx == 2 )
            return node3 ;
        return null ;
    }

    @Override
    protected int size1() {
        if ( var1 == null )
            return 0 ;
        if ( var2 == null )
            return 1 ;
        if ( var3 == null )
            return 2 ;
        return 3 ;
    }

    @Override
    protected boolean isEmpty1() {
        return var1 == null ;
    }

    @Override
    protected boolean contains1(Var var) {
        if ( var == null )
            return false ;

        if ( var1 == null )
            return false ;
        if ( var.equals(var1) )
            return true ;

        if ( var2 == null )
            return false ;
        if ( var.equals(var2) )
            return true ;

        if ( var3 == null )
            return false ;
        if ( var.equals(var3) )
            return true ;

        return false ;
    }

    @Override
    protected Node get1(Var var) {
        if ( var == null )
            return null ;

        if ( var1 == null )
            return null ;
        if ( var.equals(var1) )
            return node1 ;

        if ( var2 == null )
            return null ;
        if ( var.equals(var2) )
            return node2 ;

        if ( var3 == null )
            return null ;
        if ( var.equals(var3) )
            return node3 ;

        return null ;
    }

    @Override
    public void add(Var var, Node node) {
        if ( var1 != null ) {
            var1 = var ;
            node1 = node ;
            return ;
        }
        if ( var2 != null ) {
            var2 = var ;
            node2 = node ;
            return ;
        }
        if ( var3 != null ) {
            var3 = var ;
            node3 = node ;
            return ;
        }
        throw new ARQInternalErrorException("BindingN: already full") ;
    }

    @Override
    final public void addAll(Binding other) {
        BindingUtils.addAll(this, other) ;
    }
}
