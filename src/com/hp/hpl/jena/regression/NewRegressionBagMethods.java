/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionBagMethods.java,v 1.1 2005-10-19 13:31:39 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

public class NewRegressionBagMethods extends NewRegressionBase
    {
    public NewRegressionBagMethods( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionBagMethods.class );  }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    protected Resource r;

    public void setUp()
        { 
        m = getModel(); 
        r = m.createResource();
        }
    
    public void testX() 
        { 
        Bag bag = m.createBag();
        assertTrue( m.contains( bag, RDF.type, RDF.Bag ) );
        }
    
//  /** test bag
//  * @param m the model implementation under test
//  */
// public void test14(Model m) {
//     String  test = "Test14";
//     int     n = 0;
//
//     try {
//         NodeIterator nIter;
//         StmtIterator sIter;
////         System.out.println("Beginning " + test);
//         boolean    tvBoolean = true;
//         byte       tvByte = 1;
//         short      tvShort = 2;
//         int        tvInt = -1;
//         long       tvLong = -2;
//         char       tvChar = '!';
//         float      tvFloat = (float) 123.456;
//         double     tvDouble = -123.456;
//         String     tvString = "test 12 string";
//         LitTestObj tvObject = new LitTestObj(12345);
//         Literal tvLiteral = m.createLiteral("test 12 string 2");
//         Resource   tvResObj = m.createResource(new ResTestObjF());
//         Object     tvLitObj = new LitTestObj(1234);
//         Bag        tvBag    = m.createBag();
//         Alt        tvAlt    = m.createAlt();
//         Seq        tvSeq    = m.createSeq();
//         int        num=10;
//         Statement stmt;
//
//         n=100;
//         n++; Bag bag = m.createBag();
//         n++; if (! m.contains(bag, RDF.type, RDF.Bag)) error(test,n);
//         n++; if (! (bag.size() == 0)) error(test,n);
//
//         n=200;
//         n++; bag.add(tvBoolean);
//         n++; if (! bag.contains(tvBoolean)) error(test, n);
//         n++; bag.add(tvByte);
//         n++; if (! bag.contains(tvByte)) error(test, n);
//         n++; bag.add(tvShort);
//         n++; if (! bag.contains(tvShort)) error(test, n);
//         n++; bag.add(tvInt);
//         n++; if (! bag.contains(tvInt)) error(test, n);
//         n++; bag.add(tvLong);
//         n++; if (! bag.contains(tvLong)) error(test, n);
//         n++; bag.add(tvChar);
//         n++; if (! bag.contains(tvChar)) error(test, n);
//         n++; bag.add(tvFloat);
//         n++; if (! bag.contains(tvFloat)) error(test, n);
//         n++; bag.add(tvDouble);
//         n++; if (! bag.contains(tvDouble)) error(test, n);
//         n++; bag.add(tvString);
//         n++; if (! bag.contains(tvString)) error(test, n);
//         n++; bag.add(tvLiteral);
//         n++; if (! bag.contains(tvLiteral)) error(test, n);
//         n++; bag.add(tvResObj);
//         n++; if (! bag.contains(tvResObj)) error(test, n);
//         n++; bag.add(tvLitObj);
//         n++; if (! bag.contains(tvLitObj)) error(test, n);
//         n++; if (! (bag.size()==12)) error(test,n);
//
//         {
//             n=300;
//             n++; bag = m.createBag();
//                  for (int i=0; i<num; i++) {
//                     bag.add(i);
//                 }
//             n++; if (! (bag.size()==num)) error(test,n);
//             n++; nIter = bag.iterator();
//                 for (int i=0; i<num; i++) {
//                     if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                         error(test, 320+i);
//                 }
//                 nIter.close();
//         }
//
//         {
//             boolean[] found = new boolean[num];
//             boolean[] pattern =
//               {true,  true,  true,  false, false,
//                false, false, false, true,  true };
//
//             n=400;
//             n++; nIter=bag.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=450;
//             n++; nIter = bag.iterator();
//                  while (nIter.hasNext()) {
//                     int v = ((Literal) nIter.nextNode()).getInt();
//             n++;    if (  found[v]) error(test,n);
//                     found[v] = true;
//                  }
//             n++; nIter.close();
//             n=480;
//                  for (int i=0; i<num; i++) {
//             n++;    if (! (found[i]==pattern[i])) error(test,n);
//                 }
//         }
//
//         {
//             boolean[] found = new boolean[num];
//             boolean[] pattern =
//               {false,  true,  true,  false, false,
//                false, false, false, true,  false };
//
//             n=500;
//             n++; bag = m.createBag();
//                  for (int i=0; i<num; i++) {
//                     bag.add(i);
//                 }
//             n++; nIter=bag.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=550;
//             n++; nIter = bag.iterator();
//                  while (nIter.hasNext()) {
//                     int v = ((Literal) nIter.nextNode()).getInt();
//             n++;    if (  found[v]) error(test,n);
//                     found[v] = true;
//                  }
//             n++; nIter.close();
//             n=580;
//                  for (int i=0; i<num; i++) {
//             n++;    if (! (found[i]==pattern[i])) error(test,n);
//                 }
//         }
//
//         {
//             boolean[] found = new boolean[num];
//             boolean[] pattern =
//               {false, false, false, false, false,
//                false, false, false, false, false};
//
//             n=600;
//             n++; bag = m.createBag();
//                  for (int i=0; i<num; i++) {
//                     bag.add(i);
//                 }
//             n++; nIter=bag.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=650;
//             n++; nIter = bag.iterator();
//                  while (nIter.hasNext()) {
//                     int v = ((Literal) nIter.nextNode()).getInt();
//             n++;    if (  found[v]) error(test,n);
//                     found[v] = true;
//                  }
//             n++; nIter.close();
//             n=680;
//                  for (int i=0; i<num; i++) {
//             n++;    if (! (found[i]==pattern[i])) error(test,n);
//                 }
//         }
//
//     } catch (Exception e) {
//         logger.error( "test " + test + "[" + n + "]", e );
//         errors = true;
//     }
////     System.out.println("End of " + test);
// }
//
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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