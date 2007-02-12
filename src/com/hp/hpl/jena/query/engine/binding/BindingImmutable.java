/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterConvert;

/** A binding that is fixed - used in calculating DISTINCT result sets.
 *  .hashCode and .equals are overidden for content equality semantics (where
 *  "equality" means Node.equals, not Node.sameValueAs)
 * 
 * @author   Andy Seaborne
 * @version  $Id: BindingImmutable.java,v 1.1 2007/02/06 17:06:05 andy_seaborne Exp $
 */


public class BindingImmutable extends BindingBase
{
    List vars ;
    List values ;
    private int calcHashCode ;    

    /**
     * @param projectVars    The projection variables.
     * @param original       Binding to use
     */
    
    private BindingImmutable(Collection projectVars, Binding original)
    {
        super(null) ; 
        this.vars = new ArrayList(projectVars.size()) ;
        values = new ArrayList(projectVars.size()) ;
        calcHashCode = 0 ; 
        for ( Iterator iter = projectVars.iterator() ; iter.hasNext() ; )
        {
            Var var = (Var)iter.next() ;
            Object n = original.get(var) ;

            vars.add(var) ;
            values.add(n) ; // Includes nulls.
            
            if ( n != null )
                // Independent of variable order.
                calcHashCode = calcHashCode^n.hashCode()^var.hashCode() ; 
        }
    }
        
    protected void add1(Var var, Node node)
    { throw new UnsupportedOperationException("BindingImmutable.add") ; }

    protected Iterator vars1() { return vars.listIterator() ; }
    
    protected boolean contains1(Var var)
    {
        Object tmp = get1(var) ;
        return tmp != null ;
    }

    protected Node get1(Var var)
    {
        int i = vars.indexOf(var) ;
        if (i < 0 )
            return null ;
        return (Node)values.get(i) ;
    }

    public boolean equals(Object obj)
    {
        if ( this == obj ) return true ;
        
        if ( ! ( obj instanceof BindingImmutable) )
            return false ;
        BindingImmutable b = (BindingImmutable)obj ;
        if ( b.hashCode() != this.hashCode())
            return false ;
        
        if ( this.vars.size() != b.vars.size() )
            // Mismatch in the variables.
            return false ;
        
        Iterator iter = this.vars.listIterator() ;
        for ( ; iter.hasNext() ; )
        {
            Var v = (Var)iter.next() ; 
            Node node1 = this.get(v) ;
            Node node2 = b.get(v) ;
            
            if ( node1 == null && node2 == null )
                continue ;
            if (node1 == null )
                return false ;      // obj2 not null
            if (node2 == null )
                return false ;      // obj1 not null

            // Same by graph matching (.eausl or .sameAs)
            if ( !node1.equals(node2) )   // *** .equals - not sameValueAs
                return false ;
        }
        return true ;
    }
    
    public int hashCode()
    {
        return calcHashCode ;
    }
    
    protected void checkAdd1(Var v, Node node) { }
    
    public static QueryIterator create(Collection vars, QueryIterator cIter, ExecutionContext context)
    {
        return new QueryIterConvert(cIter, new Convert(vars), context) ;
    }
    
    public static class Convert implements QueryIterConvert.Converter
    {
        Collection vars  ;
        public Convert(Collection vars) { this.vars = vars ; }
        
        public Binding convert(Binding binding)
        {
            return new BindingImmutable(vars, binding) ;
        }
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
