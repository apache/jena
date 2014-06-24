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

/** Code to calculate the transitive-bNode closures */

package com.hp.hpl.jena.sparql.util;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

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
        List<Resource> visited = new ArrayList<>() ;

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
        List<Resource> visited = new ArrayList<>() ;
        
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
        for ( Object value : s )
        {
            tmp = tmp + " " + value.toString();
        }
        return tmp ;
    }

    // Defines the bNode closure
    
    public static class ClosureBNode implements ClosureTest
    {
        @Override
        public boolean traverse(Resource r)
        {
            return r.isAnon() ;
        }

        @Override
        public boolean includeStmt(Statement s)
        {
            return true ;
        }
    }

    // Defines the reachable (on forward arcs) subgraph. 
    
    public static class ClosureReachable implements ClosureTest
    {
        @Override
        public boolean traverse(Resource r)
        {
            return true ;
        }

        @Override
        public boolean includeStmt(Statement s)
        {
            return true ;
        }
    }
    

}
