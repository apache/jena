/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;

public class ResultSetCompare
{
    // See
    //   RSCompare -> Merge rename to ResultSetCompare

    // TestItem has getResultModel->Model.
    // The issue will be some results are models for a reason.
    // Add tests based on value to TestResultSet.
    
    /** compare two result sets for equivalence.  Equivalance means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values, 
     * bNodes must map to a consistent other bNodes.  Value comparisons of nodes.   
     * 
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion. 
     */
    
    
    public static boolean equalsByValue(ResultSet rs1, ResultSet rs2)
    {
        return equivalent(convert(rs1), convert(rs2), new BNodeIso(sameValue)) ;
    }


    /** compare two result sets for equivalence.  Equivalance means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values, 
     * bNodes must map to a consistent other bNodes.  
     * Term comparisons of nodes.   
     * 
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion. 
     */

    public static boolean equalsByTerm(ResultSet rs1, ResultSet rs2)
    {
        return equivalent(convert(rs1), convert(rs2), new BNodeIso(sameTerm)) ;
    }

    
    /** compare two result sets for equivalence.  Equivalance means:
     * Each row in rs1 matchs the same index row in rs2.
     * Rows match if they have the same variables with the same values, 
     * bNodes must map to a consistent other bNodes.  
     * Value comparisons of nodes.   
     * 
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion. 
     */

    public static boolean equalsByValueAndOrder(ResultSet rs1, ResultSet rs2)
    {
        return equivalentByOrder(convert(rs1) , convert(rs2), new BNodeIso(sameValue)) ;
    }

    /** compare two result sets for equivalence.  Equivalance means:
     * Each row in rs1 matchs the same index row in rs2.
     * Rows match if they have the same variables with the same values, 
     * bNodes must map to a consistent other bNodes.  
     * RDF term comparisons of nodes.   
     * 
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion. 
     */
    public static boolean equalsByTermAndOrder(ResultSet rs1, ResultSet rs2)
    {
        return equivalentByOrder(convert(rs1) , convert(rs2), new BNodeIso(sameTerm)) ;
    }

    
    interface EqualityTest { boolean equal(Node n1, Node n2) ; }
    
    static public boolean equal(Binding bind1, Binding bind2, EqualityTest test)
    {
        if ( bind1 == bind2 ) return true ;

        if ( bind1.size() != bind2.size() )
            return false ; 
        
        if ( ! containedIn(bind1, bind2, test) ) return false ;
        //if ( ! contains(b2, b1, test) ) return false ;
        return true ;
    }

    // Is bind1 contained in bind22?  For every (var,value) in bind1, is it in bind2?
    // Maybe more in bind2.
    private static boolean containedIn(Binding bind1, Binding bind2, EqualityTest test)
    {
        // There are about 100 ways to do this! 
        Iterator<Var> iter1 =  bind1.vars() ;
        
        for ( Var v : Iter.iter(iter1) )
        {
            Node n1 = bind1.get(v) ;
            Node n2 = bind2.get(v) ;
            if ( n2 == null )
                // v bound in bind1 and not in bind2.
                return false ;
            if ( ! test.equal(n1, n2) )
                return false ;
        }
        return true ;
    }
    
    // Backtracking with bNode assignment.
    
//    static boolean equivalentByValuex(ResultSet rs1, ResultSet rs2)
//    {
//        return equivalent(convert(rs1), convert(rs2), sameValue) ;
//    }
//    
    static private List<Binding> convert(ResultSet rs)
    {
        return Iter.iter(rs).map(qs2b).toList() ;
    }
    
    
//    static boolean equivalentByTerm(ResultSet rs1, ResultSet rs2)
//    {
//        return equivalent(convert(rs1), convert(rs2), sameTerm) ;
//    }
    
    static boolean equivalent(Collection<Binding> rows1, Collection<Binding> rows2, EqualityTest match)
    {
        if ( rows1.size() != rows2.size() )
            return false ;
        for ( Binding row1 : rows1 )
        {
            // find in rows2.
            Binding matched = null ;
            for ( Binding row2 : rows2 )
            {
                if ( equal(row1, row2, match))
                {
                    matched = row2 ;
                    break ;
                }
            }
            

            if ( matched == null ) return false ;
            // Remove matching.
            rows2.remove(matched) ;
        }
        return true ;
    }
    
    static boolean equivalentByOrder(List<Binding> rows1, List<Binding> rows2, EqualityTest match)
    {
        if ( rows1.size() != rows2.size() )
             return false ;
        
        Iterator<Binding> iter1 = rows1.iterator() ;
        Iterator<Binding> iter2 = rows2.iterator() ;
        
        while ( iter1.hasNext() )
        {
            Binding row1 = iter1.next() ;
            Binding row2 = iter2.next() ;
            if ( !equal(row1, row2, match) )
                return false ;
        }
        return true ;
    }
    
    private static Transform<QuerySolution, Binding> qs2b = new Transform<QuerySolution, Binding> () {

        public Binding convert(QuerySolution item)
        {
            return BindingUtils.asBinding(item) ;
        }
    } ;
    
    // This is term comparison.
    private static EqualityTest sameTerm = new EqualityTest() {
        public boolean equal(Node n1, Node n2)
        {
            return NodeFunctions.sameTerm(n1, n2) ;
        }
    } ; 
        
    // This is value comparison
    private static EqualityTest sameValue = new EqualityTest() {
        public boolean equal(Node n1, Node n2)
        {
            NodeValue nv1 = NodeValue.makeNode(n1) ;
            NodeValue nv2 = NodeValue.makeNode(n2) ;
            try {
                return NodeValue.sameAs(nv1, nv2) ;
            } catch(ExprEvalException ex)
            {
                // Incomparible as values - must be different for our purposes.
                return false ; 
            }
        }
    } ;
        
    private static class BNodeIso implements EqualityTest
    {
        private HashMap<Node, Node> mapping ;
        private EqualityTest literalTest ;

        BNodeIso(EqualityTest literalTest)
        { 
            this.mapping = new HashMap<Node, Node>() ;
            this.literalTest = literalTest ;
        }

        public boolean equal(Node n1, Node n2)
        {
            if ( n1 == null && n2 == null ) return true ;
            if ( n1 == null ) return false ;
            if ( n2 == null ) return false ;
            
            if ( n1.isURI() && n2.isURI() )
                return n1.equals(n2) ;
            
            if ( n1.isLiteral() && n2.isLiteral() )
                return literalTest.equal(n1, n2) ;
            
            if ( n1.isBlank() && n1.isBlank() )
            {
                Node x = mapping.get(n1) ;
                if ( x == null )
                {
                    // Not present: map n1 to n2.
                    mapping.put(n1, n2) ;
                    return true ;
                }
                return x.equals(n2) ;
            }
            
            return false ;
        }
    }
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