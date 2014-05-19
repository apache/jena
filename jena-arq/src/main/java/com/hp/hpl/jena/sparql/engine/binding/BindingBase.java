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

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.IteratorConcat ;
import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

import com.hp.hpl.jena.sparql.util.FmtUtils ;


/** Machinary encapsulating a mapping from a name to a value.
 *  The "parent" is a shared, immutable, common set of bindings.
 *  An association of var/node must not override a setting in the parent.
 *  
 *  @see BindingFactory
 *  @see BindingMap for mutable bindings.
 */


abstract public class BindingBase implements Binding
{
    static boolean CHECKING = true ;
    static boolean UNIQUE_NAMES_CHECK = true ;
    
    // This is a set of bindings, each binding being one pair (var, value).
    protected Binding parent ;
    
    // Tracking children is for flexiblity.
    
    // It is not needed for flatten results sets (i.e. with nulls in)
    // but is needed for nested result set that record subqueries.
    // and have nested results.
    
    // But keeping the child reference means that used bindings are not freed
    // to the GC until the parent is freed and hence the root is finished with -
    // which is all results.
    
//    private List children = new ArrayList() ; 
//    protected void addChild(Binding child) {  children.add(child) ; }
//    private Iterator getChildren() { return children.listIterator() ; }
    
    protected BindingBase(Binding _parent)
    {
        parent = _parent ;
        //parent.addChild((BindingBase)this) ;
    }
        
    public Binding getParent() { return parent ; }
    
    /** Iterate over all the names of variables. */
    @Override
    final public Iterator<Var> vars()
    {
        // Hidesight - replace with accumulator style vars1(accumulator) 
        Iterator<Var> iter = vars1() ;
        if ( parent != null )
            iter = IteratorConcat.concat(parent.vars(), iter ) ;
        return iter ;
    }
    protected abstract Iterator<Var> vars1() ;
    
    @Override
    final public int size()
    {
        int x = size1() ;
        if ( parent != null )
            x = x + parent.size() ;
        return x ;
    }
    
    protected abstract int size1() ;
    
    @Override
    public boolean isEmpty() 
    {
        if ( ! isEmpty1() ) 
            return false ;
        if ( parent == null )
            return true ;
        return parent.isEmpty() ;
    }
    
    protected abstract boolean isEmpty1() ;
    
    /** Test whether a name is bound to some object */
    @Override
    public boolean contains(Var var)
    {
        if ( contains1(var) )
            return true ;
        if ( parent == null )
            return false ; 
        return parent.contains(var) ; 
    }
    
    protected abstract boolean contains1(Var var) ;

    /** Return the object bound to a name, or null */
    @Override
    final public Node get(Var var)
    {
        Node node = get1(var) ;
        
        if ( node != null )
            return node ;
        
        if ( parent == null )
            return null ; 
        
        return parent.get(var) ; 

    }
    protected abstract Node get1(Var var) ;
    
    @Override
    public String toString()
    {
        StringBuffer sbuff = new StringBuffer() ;
        format1(sbuff) ;

        if ( parent != null )
        {
            String tmp = parent.toString() ;
            if ( tmp != null && (tmp.length() != 0 ) )
            {
                sbuff.append(" -> ") ;
                sbuff.append(tmp) ;
            }
        }
        return sbuff.toString() ;
    }

    // Do one level of binding 
    public void format1(StringBuffer sbuff)
    {
        String sep = "" ;
        for ( Iterator<Var> iter = vars1() ; iter.hasNext() ; ) 
        {
            Object obj = iter.next() ;
            Var var = (Var)obj ;
            
            sbuff.append(sep) ;
            sep = " " ;
            format(sbuff, var) ;
        }
    }
    
    protected void format(StringBuffer sbuff, Var var)
    {
        Node node = get(var) ;
        String tmp = FmtUtils.stringForObject(node) ;
        sbuff.append("( ?"+var.getVarName()+" = "+tmp+" )") ;
    }
    
    // Do one level of binding 
    public String toString1()
    {
        StringBuffer sbuff = new StringBuffer() ;
        format1(sbuff) ;
        return sbuff.toString() ;
    }

    @Override
    public int hashCode() { return hashCode(this) ; } 
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Binding) ) return false ;
        Binding binding = (Binding)other ;
        return equals(this, binding) ; 
    }

    // Not everything derives from BindingBase.
    public static int hashCode(Binding bind)
    {
        int hash = 0xC0 ;
        for ( Iterator<Var> iter = bind.vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ; 
            Node node = bind.get(var) ;
            hash ^= var.hashCode() ;
            hash ^= node.hashCode() ;
        }
        return hash ;
    }

    public static boolean equals(Binding bind1, Binding bind2)
    {
        if ( bind1 == bind2 ) return true ;

        // Same variables?
        
        if ( bind1.size() != bind2.size() )
            return false ;

        for ( Iterator<Var> iter1 = bind1.vars() ; iter1.hasNext() ; )
        {
            Var var = iter1.next() ; 
            Node node1 = bind1.get(var) ;
            Node node2 = bind2.get(var) ;
            if ( ! Lib.equal(node1, node2) )
                return false ;
        }
        
        // No need to check the other way round as the sizes matched. 
        return true ;
    }
}
