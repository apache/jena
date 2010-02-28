/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/** Code to calculate the transitive-bNode closures
 * @author     Andy Seaborne
 */

package com.hp.hpl.jena.sparql.util;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class Closure
{
    /** Calculate the bNode closure from a statement .
     *  The Statement itself does not automatically get included.
     * @param stmt
     * @return A model containing statements
     */

    public static Model closure(Statement stmt)
    {
        return closure(stmt, new ClosureBNode()) ;
    }

    /** Calculate the bNode closure from a statement .
      *  The Statement itself does not automatically get included.
      * @param statement  Starting point for the closure.
      * @param test       The test object to be applied
      * @return A model containing statements
      * @see ClosureTest
      */

    public static Model closure(Statement statement, ClosureTest test)
    {
        return closure(statement, test, GraphFactory.makeJenaDefaultModel()) ;
    }

    /** Calculate the bNode closure from a statement .
      *  The Statement itself does not automatically get included.
      * @param statement  Starting point for the closure.
      * @param model      Add the statements to this model
      * @return A model containing statements
      * @see ClosureTest
      */

    public static Model closure(Statement statement, Model model)
    {
        return closure(statement, new ClosureBNode(), model) ;
    }
    
    /** Calculate the bNode closure from a statement .
      *  The Statement itself does not automatically get included.
      * @param statement  Starting point for the closure.
      * @param test       The test object to be applied
      * @param model      Add the statements to this model
      * @return A model containing statements
      * @see ClosureTest
      */

    public static Model closure(Statement statement, ClosureTest test, Model model)
    {
        //Set visited = new HashSet() ;
        List<Resource> visited = new ArrayList<Resource>() ;

        closure(statement, model, visited, test) ;
        return model ;
    }

    /** Calculate the bNode closure from a resource.
     *  The Statement itself does not automatically get included.
     * @param resource       Starting point for the closure.
     * @param testThisNode   Indicate whether to apply the closure test to the Resource argument.
     * @return A model containing statements
     */

    public static Model closure(Resource resource, boolean testThisNode)
    {
        return closure(resource, new ClosureBNode(), testThisNode) ;
    }

    /** Calculate the bNode closure from a resource .
     *  The Statement itself does not automatically get included.
     * @param resource
     * @param test            The test object to be applied
     * @param testThisNode    Indicate whether to apply the closure test to the Resource argument.
     * @return A model containing statements
     */

    public static Model closure(Resource resource, ClosureTest test, boolean testThisNode)
    {
        return closure(resource, test, testThisNode, GraphFactory.makeJenaDefaultModel()) ;
    }


    /** Calculate the bNode closure from a resource .
     *  The Statement itself does not automatically get included.
     * @param resource
     * @param testThisNode Indicate whether to apply the closure test to the Resource argument.
     * @param results      Add the statements to this model
     * @return A model containing statements
     */

    public static Model closure(Resource resource, boolean testThisNode, Model results)
    {
        return closure(resource, new ClosureBNode(), testThisNode, results) ;
    }
    
    
    /** Calculate the bNode closure from a resource .
     *  The Statement itself does not automatically get included.
     * @param resource
     * @param test          The test object to be applied
     * @param testThisNode  Indicate whether to apply the closure test to the Resource argument.
     * @param results       Add the statements to this model
     * @return A model containing statements
     */

    public static Model closure(Resource resource, ClosureTest test,
                                boolean testThisNode, Model results)
    {
        //Set s = new HashSet() ;
        //Set visited = new HashSet() ;
        List<Resource> visited = new ArrayList<Resource>() ;
        
        if ( ! testThisNode )
            closureNoTest(resource, results, visited, test) ;
        else
            closure(resource, results, visited, test) ;
        return results ;
    }



    // --------------------------------------------------------------------------------

    private static void closure(Statement stmt,
                                Model closureBlob, Collection<Resource> visited,
                                ClosureTest test)
    {
        if ( test.includeStmt(stmt) )
                closureBlob.add(stmt) ;
        closure(stmt.getSubject(), closureBlob, visited, test) ;
        closure(stmt.getObject(),  closureBlob, visited, test) ;
    }


    private static void closure(RDFNode n,
                                Model closureBlob, Collection<Resource> visited,
                                ClosureTest test)
    {
        if ( ! ( n instanceof Resource ) )
            return ;

        Resource r = (Resource)n ;

        if ( visited.contains(r) )
            return ;

        if ( ! test.traverse(r) )
            return ;

        closureNoTest(r, closureBlob, visited, test) ;
    }
    
     
    private static void closureNoTest(Resource r,
                                      Model closureBlob, Collection<Resource> visited,
                                      ClosureTest test)
    {
        visited.add(r) ;

        StmtIterator sIter = r.listProperties() ;
        for ( ; sIter.hasNext() ; )
        {
            Statement stmt = sIter.nextStatement() ;
            closure(stmt, closureBlob, visited, test) ;
        }
    }


    private static String dbg_string(Collection<?> s)
    {
        String tmp = "" ;
        for ( Iterator<?> iter = s.iterator() ; iter.hasNext() ; )
        {
            tmp = tmp+" "+iter.next().toString() ;
        }
        return tmp ;
    }

    // Defines the bNode closure
    
    public static class ClosureBNode implements ClosureTest
    {
        public boolean traverse(Resource r)
        {
            return r.isAnon() ;
        }

        public boolean includeStmt(Statement s)
        {
            return true ;
        }
    }

    // Defines the reachable (on forwrd arcs) subgraph. 
    
    public static class ClosureReachable implements ClosureTest
    {
        public boolean traverse(Resource r)
        {
            return true ;
        }

        public boolean includeStmt(Statement s)
        {
            return true ;
        }
    }
    

}


/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

