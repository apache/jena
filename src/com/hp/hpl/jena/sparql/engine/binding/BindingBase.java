/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.* ;

import org.apache.commons.logging.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

/** Machinary encapsulating a mapping from a name to a value.
 * 
 * @author   Andy Seaborne
 * @version  $Id: BindingBase.java,v 1.1 2007/02/06 17:06:05 andy_seaborne Exp $
 */


abstract public class BindingBase implements Binding
{
    static Log log = LogFactory.getLog(BindingBase.class) ;
    
    static boolean CHECKING = true ;
    static boolean UNIQUE_NAMES_CHECK = true ;
    
    // This is a set of bindings, each binding being one pair (var, value).
    Binding parent ;
    
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
    
    /** Add a (var,value) - the node value is never null */
    final public void add(Var var, Node node)
    { 
        if ( node == null )
        {
            log.warn("Binding.add: null value - ignored") ;
            return ;
        }
        checkAdd(var, node) ;
        add1(var, node) ;
    }

    protected abstract void add1(Var name, Node node) ;

    public void addAll(Binding other)
    {
        Iterator iter = other.vars() ;
        for ( ; iter.hasNext(); )
        {
            Var v = (Var)iter.next();
            Node n = other.get(v) ;
            add(v, n) ;
        }
    }
    
    /** Iterate over all the names of variables. */
    final public Iterator vars()
    {
        Iterator iter = vars1() ;
        if ( parent != null )
            iter = new ConcatenatedIterator(parent.vars(), iter ) ;
        return iter ;
    }
    protected abstract Iterator vars1() ;
    
    final public int size()
    {
        int x = size1() ;
        if ( parent != null )
            x = x + parent.size() ;
        return x ;
    }
    
    protected abstract int size1() ;
    
    public boolean isEmpty() { return size() == 0 ; }
    
    /** Test whether a name is bound to some object */
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
        for ( Iterator iter = vars1() ; iter.hasNext() ; ) 
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

    private void checkAdd(Var var, Node node)
    {
        if ( ! CHECKING )
            return ;
        if ( node == null )
            throw new ARQInternalErrorException("check("+var+", "+node+"): null node value" ) ;
        if ( UNIQUE_NAMES_CHECK && contains(var) )
            throw new ARQInternalErrorException("Attempt to reassign '"+var+
                                                "' from '"+FmtUtils.stringForNode(get(var))+
                                                "' to '"+FmtUtils.stringForNode(node)+"'") ;
        // Let the implementation do a check as well.
        checkAdd1(var, node) ;
    }

    protected abstract void checkAdd1(Var var, Node node) ;
    
    public int hashCode() { return hashCode(this) ; } 
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof Binding) ) return false ;
        Binding binding = (Binding)other ;
        return equals(this, binding) ; 
    }
    
    // Not everything derives from BindingBase.
    public static int hashCode(Binding bind)
    {
        int hash = 0xC0 ;
        for ( Iterator iter = bind.vars() ; iter.hasNext() ; )
        {
            Var var = (Var)iter.next() ; 
            Node node = bind.get(var) ;
            hash ^= var.hashCode() ;
            hash ^= node.hashCode() ;
        }
        return hash ;
    }

    public static boolean equals(Binding bind1, Binding bind2)
    {
        // Same variables?
        
        if ( bind1.size() != bind2.size() )
            return false ;

        for ( Iterator iter1 = bind1.vars() ; iter1.hasNext() ; )
        {
            Var var = (Var)iter1.next() ; 
            Node node1 = bind1.get(var) ;
            Node node2 = bind2.get(var) ;
            
            if ( node1 == null && node2 == null )
                continue ;
            if (node1 == null )
                return false ;      // node2 not null
            if (node2 == null )
                return false ;      // node1 not null
            if ( !node1.equals(node2) )
                return false ;
        }
        
        // No need to check the other way round as the sizes matched. 
        return true ;
    }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
