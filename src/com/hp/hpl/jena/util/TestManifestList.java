/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

 
package com.hp.hpl.jena.util;

import java.util.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;
import com.hp.hpl.jena.vocabulary.TestManifest ;
import junit.framework.* ;


/** Wrapper class for handling models containg a test manifest according to the
    vocabulary <code>http://jena.hpl.hp.com/2003/03/test-manifest#</code>.
  
    The test manifest framework takes a description file or model and provides a
    set of convenience operations on the manifest.  Entries are in order they are
    in the lists in the manifest but if the manifest model conatins multiple lists,
    i.e. theer are muitple manifest objects or a manifest object has mutiple lists,
    then the order between the lists is not determined.  
  
    @author      Andy Seaborne
    @version     $Id: TestManifestList.java,v 1.4 2003-08-27 13:07:55 andy_seaborne Exp $
*/
public class TestManifestList
{
    
    Model manifest = null ;
    
    public TestManifestList(String filename)
    {
        manifest = ModelLoader.loadModel(filename) ;
    }
    
    public TestManifestList(Model m)
    {
        manifest = m ;
    }

    public Model getModel() { return manifest ; } 

    public TestIterator iterator() { return new TestIterator() ; }

    /** Iterator over all the manifest entries found in the model. 
     * 
     * @author Andy Seaborne
     */

    public class TestIterator implements Iterator
    {
        // Build the test set in memory.
        // Means we can cleanly close iterators.
        List entries ;
        Iterator iterator ;

        /** Create the iterator object */         
        TestIterator()
        {
            init() ;    
        }

        /** Initialise the datatstructures from the model - only needed
         *  if the model has changed since the internal datastructures were
         *  calculated. 
         */
        public void init()
        {
            entries = new ArrayList();

            StmtIterator manifestStmts =
                manifest.listStatements(null, RDF.type, TestManifest.Manifest);

            for (; manifestStmts.hasNext();)
            {
                Statement manifestItemStmt = manifestStmts.nextStatement();
                Resource manifestRes = manifestItemStmt.getSubject();
                StmtIterator listIter = manifestRes.listProperties(TestManifest.entries);
                for (; listIter.hasNext();)
                {
                    Resource listItem = listIter.nextStatement().getResource();

                    for (; !listItem.equals(RDF.nil);)
                    {

                        Resource entry = listItem.getRequiredProperty(RDF.first).getResource();

                        TestItem item = new TestItem();
                        item.entry = entry ;
                        if ( entry.hasProperty(TestManifest.name))
                            item.name = entry.getRequiredProperty(TestManifest.name).getString() ;
                        else
                            item.name = "No name" ;
                        if ( entry.hasProperty(TestManifest.action))
                            item.action = entry.getRequiredProperty(TestManifest.action).getObject();
                        if (entry.hasProperty(TestManifest.result))
                            item.result = entry.getRequiredProperty(TestManifest.result).getObject();

                        entries.add(item);
                        // Move on
                        listItem = listItem.getRequiredProperty(RDF.rest).getResource();
                    }
                }
                listIter.close();
            }
            manifestStmts.close();
            reset() ;
        }

        /** Start again.  Iterator is reset - model is not processed again.
         */
        public void reset() { iterator = entries.iterator(); }
        
        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        /**
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
            return iterator.next();
        }

        public TestItem nextItem()
        {
            Object obj = iterator.next();
            if ( ! ( obj instanceof TestItem ) )
                System.err.println("obj is "+obj.getClass().getName()) ;
            return (TestItem)obj ;
            //return (TestItem) iterator.next();
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException(this.getClass().getName());
        }

    }
    
    public class TestItem
    {
        Resource entry ;
        String name ;
        RDFNode action ;
        RDFNode result ;
        
        public Resource getEntry()  { return entry ; }
        public String   getName()   { return name ; }
        public RDFNode  getAction() { return action ; }
        public RDFNode  getResult() { return result ; }
    }

    // -------- Support for a "map" programming style 

    /** Action to perform on each entry of the manifest file to create the test */
    public interface ActionProc
    {
        /** Called on each test described.
         * @param testNode  The RDF Resource for the whole test case
         * @param action    RDFNode for the test action
         * @param result    RDFNode for the test result : maybe null if no result specified.
         * @return TestCase JUnit test created or null for none
         */
        public TestCase createTest(Resource testNode, RDFNode action, RDFNode result) ;
    }
    

    /** Apply an ActionProc to every item in the manifest to
     *  create a JUnit test suite.
     * 
     * @param actionProc
     * @return TestSuite
     */

    public TestSuite builtTests(ActionProc actionProc)
    {
        TestSuite suite = new TestSuite() ; 
        TestIterator iter = (TestIterator)iterator() ;
        for ( ; iter.hasNext() ; )
        {
            TestItem item = iter.nextItem() ;
            TestCase t = actionProc.createTest(item.entry, item.action, item.result) ;
            if ( t != null )
                suite.addTest(t) ;
        }
        return suite ;
    }

    /** Simple printer for simple manifests.
     * 
     * @author Andy Seaborne
     */ 

    public static class TestPrinter implements TestManifestList.ActionProc
    {
        public TestCase createTest(Resource r, RDFNode action, RDFNode result)
        {
            String name = "Unknown" ;
            String comment = null ;
            if ( r.hasProperty(TestManifest.name))
                name = r.getRequiredProperty(TestManifest.name).getString() ;
                        
            if ( r.hasProperty(RDFS.comment))
                comment = r.getRequiredProperty(RDFS.comment).getString() ;
                        
            String actionStr = "unset" ;
            if ( action != null )
            {
                if ( action instanceof Resource )
                   actionStr = "<"+action+">" ;
                else
                   actionStr = "\""+action+"\"" ;
            }
                        
            String resultStr = "unset" ;
            if ( result != null )
            {
                if ( result instanceof Resource )
                    resultStr = "<"+result+">" ;
                else
                    resultStr = "\""+result+"\"" ;
            }
                        
            System.out.println("Name: "+name) ;
            if ( comment != null )
                System.out.println("Comment: "+comment) ;
                
            System.out.println("    "+actionStr+" => "+resultStr) ;
            System.out.println() ;
            return null ;
        }
    } 



    //------- TESTING

    public static void main(String[] args)
    {
        TestManifestList testProc = new TestManifestList("manifest.n3") ;
        testProc.builtTests(new TestManifestList.TestPrinter()) ; 
    }
}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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

