/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.* ;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.util.ResultSetUtils;
import com.hp.hpl.jena.graph.Node;

/** RSCompare - comparision of result sets
 * 
 * @author Andy Seaborne
 */

public class RSCompare
{
    /** Compare two result sets.  If they are both ordered, do an
     *  order sensitive comparison;  if they are both unordered, do
     *  an order insensitive comparison.  If one is order, one not, they
     *  are considered different.   
     * 
     *  Note, this consumes the result set and so is best used with rewindable
     *  result sets or copies of result sets.
     *  
     * @param rs1 
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean same(ResultSet rs1, ResultSet rs2)
    {
        return sameUnordered(rs1, rs2) ;
    }
    
    /** Compare two result sets, ignoring whether they are ordered or not.
     *  Note, this consumes the result set and so is best used with rewindable
     *  result sets or copies of result sets.
     * 
     * @param rs1 ResultSet
     * @param rs2 ResultSet
     * @return true if they are equivalent
     */
    public static boolean sameUnordered(ResultSet rs1, ResultSet rs2)
    {
        return ResultSetUtils.equals(rs1, rs2) ;
//        Model model1 = ResultSetFormatter.toModel(rs1) ; 
//        Model model2 = ResultSetFormatter.toModel(rs2) ;
//        
//        return model1.isIsomorphicWith(model2) ;
    }
    
    
    /** Compare two result sets, taking order of rows into account.
     *  Note, this consumes the result set and so is best used with rewindable
     *  result sets or copies of result sets.
     * 
     * @param rs1 ResultSet
     * @param rs2 ResultSet
     * @return true if they are equivalent
     */
    public static boolean sameOrdered(ResultSet rs1, ResultSet rs2)
    {
        Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ; 
        
        for ( ; rs1.hasNext() ; )
        {
            if ( ! rs2.hasNext() )
                return false ;
            
            QuerySolution qs1 = rs1.nextSolution() ;
            QuerySolution qs2 = rs2.nextSolution() ;
            
            if ( ! sameQuerySolution(bNodeMap, qs1, qs2) )
                return false ;
        }
        
        if ( rs2.hasNext() )
            return false ;
        
        return true ;
    }
    
    private static boolean sameQuerySolution(Map<Node, Node> bNodeMap, QuerySolution qs1, QuerySolution qs2)
    {
        Iterator<String> names1 = qs1.varNames() ;
        Iterator<String> names2 = qs2.varNames() ;
        
        for ( ; names1.hasNext() ; ) 
        {
            // This is simple counting
            if ( !names2.hasNext() ) return false ;
            names2.next() ;
            
            String vn = names1.next() ;
            RDFNode rn1 = qs1.get(vn) ;
            RDFNode rn2 = qs2.get(vn) ;
            if ( rn2 == null )
                return false ;
            
            Node n1 = rn1.asNode() ;
            Node n2 = rn2.asNode() ;
            
            if ( n1.equals(n2) )
                return true ;
            if ( !n1.isBlank() || !n2.isBlank() )
                return false ;
            // Both blank.
            if ( ! bNodeMap.containsKey(n1) )
            {
                // Create a mapping
                bNodeMap.put(n1,n2) ;
                continue ;
            }
                
            if ( ! bNodeMap.get(n1).equals(n2) )
                return false ;
        }
        
        // Did we count down the whole of names2? 
        if ( names2.hasNext() )
            return false ;
        
        return true ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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