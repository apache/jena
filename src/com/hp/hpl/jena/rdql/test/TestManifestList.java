/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

 
package com.hp.hpl.jena.rdql.test;

import java.util.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.TestManifest;
import junit.framework.* ;
import org.apache.commons.logging.*;


/** Wrapper class for handling models containg a test manifest according to the
    vocabulary <code>@link{http://www.w3.org/2001/sw/DataAccess/result-set}#</code>
    (originally <code>@link{http://jena.hpl.hp.com/2003/03/test-manifest}#</code>).
  
    The test manifest framework takes a description file or model and provides a
    set of convenience operations on the manifest.  Entries are in order they are
    in the lists in the manifest but if the manifest model conatins multiple lists,
    i.e. theer are muitple manifest objects or a manifest object has mutiple lists,
    then the order between the lists is not determined.  
  
    @author      Andy Seaborne
    @version     $Id: TestManifestList.java,v 1.2 2004-12-06 13:50:29 andy_seaborne Exp $
*/
class TestManifestList
{
    
    Model manifest = null ;
    
    public TestManifestList(String filename)
    {
        manifest = FileManager.get().loadModel(filename) ;
    }
    
    public TestManifestList(Model m)
    {
        manifest = m ;
    }

    public Model getModel() { return manifest ; } 

    public TestIterator iterator() { return new TestIterator() ; }

    /** Iterator over all the manifest entries found in the model - returns TestManifestItems 
     *  
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
                        TestItem item = new TestItem(entry) ;
                        
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
            {
                LogFactory.getLog(TestManifestList.class)
                    .fatal("obj is "+obj.getClass().getName()) ;
                return null ;
            }
            return (TestItem)obj ;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException(this.getClass().getName());
        }
    }
    
    // -------- Support for a "map" programming style 

    /** Action to perform on each entry of the manifest file to create the test */
    public interface ActionProc
    {
        /** Called on each test described.
         * @param testItem  The RDF Resource for the whole test case
         */
        public void map1(TestItem testItem) ;
    }
    

    /** Apply an ActionProc to every item in the manifest to
     *  create a JUnit test suite.
     * 
     * @param actionProc
     */

    public void apply(ActionProc actionProc)
    {
        TestSuite suite = new TestSuite() ; 
        TestIterator iter = (TestIterator)iterator() ;
        for ( ; iter.hasNext() ; )
        {
            TestItem item = (TestItem)iter.next() ;
            actionProc.map1(item) ;
        }
    }
}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

