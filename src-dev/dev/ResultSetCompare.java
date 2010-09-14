/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;

public class ResultSetCompare
{
    interface EqualityTest { boolean equal(Node n1, Node n2) ; }
    
    static public boolean equal(Binding b1, Binding b2, EqualityTest test)
    {
        if ( b1.size() != b2.size() )
            return false ; 
        
        if ( ! containedIn(b1, b2, test) ) return false ;
        //if ( ! contains(b2, b1, test) ) return false ;
        return true ;
    }

    // Is b1 contained in b2?  For every (var,value) in b1, is it in b2? 
    private static boolean containedIn(Binding b1, Binding b2, EqualityTest test)
    {
        // There are about 100 ways to do this! 
        Iterator<Var> iter1 =  b1.vars() ;
        
        for ( Var v : Iter.iter(iter1) )
        {
            Node n1 = b1.get(v) ;
            Node n2 = b2.get(v) ;
            if ( ! test.equal(n1, n2) )
                return false ;
        }
        return true ;
    }
    
    // Backtracking with bNode assignment.
    
    
    
    // This is term comparison.
    static EqualityTest sameTerm = new EqualityTest() {
        public boolean equal(Node n1, Node n2)
        {
            return NodeFunctions.sameTerm(n1, n2) ;
        }} ; 
        
    // This is value comparison
    static EqualityTest sameValue = new EqualityTest() {
        public boolean equal(Node n1, Node n2)
        {
            NodeValue nv1 = NodeValue.makeNode(n1) ;
            NodeValue nv2 = NodeValue.makeNode(n2) ;
            return NodeValue.sameAs(nv1, nv2) ;
        }} ;  
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
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