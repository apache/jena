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

package dev.binding;

import java.util.Iterator ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.util.FmtUtils ;

// Parent vs copy.

/** Build Bindings
 * @see Binding
 */
public class BindingBuilder {
    public static final Binding noParent = null ; 
    // factory like:
    
    /** Create a binding of no pairs */
    public static Binding binding() { return binding(noParent) ; }
    
    /** Create a binding of no pairs */
    public static Binding binding(Binding parent) { return new Binding0(parent)  ; }
    
    public static Binding binding(Var var, Node node) { return binding(noParent, var, node) ; }
    
    /** Create a binding of one (var, value) pair */
    public static Binding binding(Binding parent, Var var, Node node)
    {
        if ( Var.isAnonVar(var) )
            return new Binding0(parent) ;
        return new Binding1(parent, var, node) ;
    }
    

    private static boolean CHECKING = true ;
    private static final boolean UNIQUE_NAMES_CHECK = true ;
    private Binding parent = noParent ;
    
    private int count = 0 ;
    private BindingN   build1 = null ;
    private BindingMap build2 = null ;
    
    public BindingBuilder() { this(null, -1) ; }
    
    public BindingBuilder(Binding parent) {
        this(parent, -1) ;
    }
    
    public BindingBuilder(Binding parent, int size) {
        if ( size < 4 )
            build1 = new BindingN(parent) ;
        else
            build2 = new BindingHashMap(parent) ;
        count = 0 ;
        setParent(parent) ;
    }

    private void setParent(Binding parent) { 
        if ( parent != null )
            throw new IllegalStateException("Parent already set") ;
        this.parent = parent ;
    }
    
    public int size() {
        return count ;
    }
    
    // Accumulate (var,value) pairs.
    public void add(Var var, Node node) {
        // Hmm.
        if ( Var.isAnonVar(var) )
            return ;
        checkAdd(var, node) ;
        
        if ( count < 4 && build1 != null ) {
            build1.add(var, node) ;
            count++ ;
            return ;
        }
        if (count == 4 && build1 != null ) {
            // Switch from BindingN to a general BindingMap
            // "build1 != null" allows for starting with a BindingMap
            build2 = new BindingHashMap(parent) ;
            for ( int i = 0 ; i < 4 ; i++ ) {
                build2.add(build1.getVar(i), build1.getNode(i)) ;
            }
            build1 = null ;
        }
        build2.add(var, node) ;   
        count++ ;
    }

    
    public Node get(Var var) {
        if ( build1 != null )
            return build1.get(var) ;
        if ( build2 != null )
            return build2.get(var) ;
        return null ;
    }
    
    public boolean contains(Var var) {
        if ( build1 != null )
            return build1.contains(var) ;
        else
            return build2.contains(var) ;
    }
    
    private void checkAdd(Var var, Node node)
    {
        if ( ! CHECKING )
            return ;
        if ( var == null )
            throw new ARQInternalErrorException("check("+var+", "+node+"): null var" ) ;
        if ( node == null )
            throw new ARQInternalErrorException("check("+var+", "+node+"): null node value" ) ;
        
//        if ( parent != null && UNIQUE_NAMES_CHECK && parent.contains(var) )
//            throw new ARQInternalErrorException("Attempt to reassign parent variable '"+var+
//                                                "' from '"+FmtUtils.stringForNode(get(var))+
//                                                "' to '"+FmtUtils.stringForNode(node)+"'") ;
        if ( UNIQUE_NAMES_CHECK && contains(var) )
            throw new ARQInternalErrorException("Attempt to reassign '"+var+
                                                "' from '"+FmtUtils.stringForNode(get(var))+
                                                "' to '"+FmtUtils.stringForNode(node)+"'") ;
    }

    /** Add all the (var, value) pairs from another binding */
    public void addAll(Binding other) {
        Iterator<Var> vIter = other.vars() ;
        for ( ; vIter.hasNext() ; ) {
            Var v = vIter.next() ;
            Node n = other.get(v) ;
            add(v,n) ;
        }
    }
    
    public Binding build() {
        Binding b = ( size() < 4 ) ? build1 : build2 ;
        reset() ;
        return b ;
    }
    
    public void reset() {
        build1 = null ;
        build2 = null ;
        count = 0 ; 
    }

}

