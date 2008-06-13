/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.sleepycat.je.*;

public class Snippets
{

    public static void BDB()
        {
            Environment myDbEnvironment = null;
            Database myDatabase = null ;
    
            try {
                try {
                    EnvironmentConfig envConfig = new EnvironmentConfig();
                    envConfig.setAllowCreate(true);
                    //envConfig.setTransactional(true) ;
                    myDbEnvironment = new Environment(new File("tmp/dbEnv"), envConfig);
    
                    // Open the database. Create it if it does not already exist.
                    DatabaseConfig dbConfig = new DatabaseConfig();
                    //dbConfig.setTransactional(true) ;
    
                    dbConfig.setAllowCreate(true);
                    myDatabase = myDbEnvironment.openDatabase(null, 
                                                              "GRAPH", 
                                                              dbConfig); 
                    String aKey = "key" ;
                    DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
                    DatabaseEntry theData = new DatabaseEntry("DATA".getBytes("UTF-8"));
    
    //                if ( myDatabase.put(null, theKey, theData) !=
    //                    OperationStatus.SUCCESS )
    //                {
    //                    System.out.println("Bad put") ;
    //                    return ;
    //                }
    
                    // Perform the get.
                    if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) ==
                        OperationStatus.SUCCESS) {
                        System.out.println(theData) ;
                        String s = new String(theData.getData(), "UTF-8") ;
                        System.out.println("Get: "+s) ;
                    }
    
    
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace(); 
                    // Exception handling goes here
                } catch (UnsupportedEncodingException ex)
                {
                    ex.printStackTrace();
                } 
                try {
                    if (myDatabase != null) {
                        myDatabase.close();
                    }
                    if (myDbEnvironment != null) {
                        myDbEnvironment.cleanLog();
                        myDbEnvironment.close();
                    } 
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace();
                }
            } finally { 
                System.out.println("Finished/BDB") ;
                System.exit(0) ;
            }
        }

    static void typedNode()
        {
    //      typedNode("'2008-04-27T16:52:17+01:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17-05:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17Z'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17+00:00'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27T16:52:17'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27'^^xsd:date") ;
          System.exit(0) ;
        }

    static void typedNodeOne(String x)
    {
        System.out.println("Input = "+x) ;
        Node n = SSE.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( nodeId == null )
        {
            System.out.println("null nodeid") ;
            return ;
        }
        
        System.out.printf("NodeId : %s\n", nodeId) ;
        Node n2 = NodeId.extract(nodeId) ;
        if ( n2 == null )
        {
            System.out.println("null node") ;
            return ;
        }
        String y = FmtUtils.stringForNode(n2) ;
        System.out.println("Output = "+y) ;
        if ( ! n.equals(n2) )
        {
            System.out.println("Different:") ;
            System.out.println("  "+n) ;
            System.out.println("  "+n2) ;
        }
    }

    static void abbrev()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("z", "http://") ;
        abbrevOne(abbrev, "http://example") ;
        abbrevOne(abbrev, "foo") ;
        abbrevOne(abbrev, ":foo") ;
        abbrevOne(abbrev, "::foo") ;
        abbrevOne(abbrev, ":::foo") ;
    }

    static void abbrevOne(StringAbbrev abbrev, String string)
    {
        String a = abbrev.abbreviate(string) ;
        String a2 = abbrev.expand(a) ;
        System.out.println(string) ;
        System.out.println(a) ;
        System.out.println(a2) ;
        System.out.println() ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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