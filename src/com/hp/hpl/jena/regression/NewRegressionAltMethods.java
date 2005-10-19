/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionAltMethods.java,v 1.1 2005-10-19 15:27:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionAltMethods extends NewRegressionContainerMethods
    {
    public NewRegressionAltMethods( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionAltMethods.class ); }

    protected Container createContainer()
        { return m.createAlt(); }

    protected Resource getContainerType()
        { return RDF.Alt; }
//  /** test Alt
//  * @param m the model implementation under test
//  */
// public void test15(Model m) {
//     String  test = "Test15";
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
//         Literal    tvLiteral = m.createLiteral("test 12 string 2");
//         Resource   tvResource = m.createResource();
//         Resource   tvResObj = m.createResource(new ResTestObjF());
//         Object     tvLitObj = new LitTestObj(1234);
//         Bag        tvBag    = m.createBag();
//         Alt        tvAlt    = m.createAlt();
//         Seq        tvSeq    = m.createSeq();
//         int        num=10;
//         Statement stmt;
//
//         {
//             n=300;
//             n++; alt = m.createAlt();
//                  for (int i=0; i<num; i++) {
//                     alt.add(i);
//                 }
//             n++; if (! (alt.size()==num)) error(test,n);
//             n++; nIter = alt.iterator();
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
//             n++; nIter=alt.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=450;
//             n++; nIter = alt.iterator();
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
//             n++; alt = m.createAlt();
//                  for (int i=0; i<num; i++) {
//                     alt.add(i);
//                 }
//             n++; nIter=alt.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=550;
//             n++; nIter = alt.iterator();
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
//             n++; alt = m.createAlt();
//                  for (int i=0; i<num; i++) {
//                     alt.add(i);
//                 }
//             n++; nIter=alt.iterator();
//                  for (int i=0; i<num; i++) {
//             n++;    nIter.nextNode();
//             n++;    if (! pattern[i]) nIter.remove();
//                     found[i] = false;
//                  }
//             n++; nIter.close();
//             n=650;
//             n++; nIter = alt.iterator();
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
//         {
//             n=700;
//             n++; alt = m.createAlt();
//             n++; if (! (alt.setDefault(tvLiteral)
//                            .getDefault().equals(tvLiteral)))
//                    error(test,n);
//             n++; if (! (alt.setDefault(tvLiteral)
//                            .getDefaultLiteral().equals(tvLiteral)))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvResource)
//                            .getDefaultResource().equals(tvResource))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvByte)
//                            .getDefaultByte()== tvByte))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvShort)
//                            .getDefaultShort()==tvShort))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvInt)
//                            .getDefaultInt()==tvInt))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvLong)
//                            .getDefaultLong()==tvLong))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvChar)
//                            .getDefaultChar()==tvChar))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvFloat)
//                            .getDefaultFloat()==tvFloat))
//                    error(test,n);
//             n++; if (!  (alt.setDefault(tvDouble)
//                            .getDefaultDouble()==tvDouble))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvString)
//                            .getDefaultString().equals(tvString))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvResObj)
//                            .getDefaultResource(new ResTestObjF())
//                            .equals(tvResObj))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvLitObj)
//                            .getDefaultObject(new LitTestObjF())
//                            .equals(tvLitObj))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvAlt)
//                            .getDefaultAlt()
//                            .equals(tvAlt))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvBag)
//                            .getDefaultBag()
//                            .equals(tvBag))
//                    error(test,n);
//             n++; if (!  alt.setDefault(tvSeq)
//                            .getDefaultSeq()
//                            .equals(tvSeq))
//                    error(test,n);
//         }
//
//     } catch (Exception e) {
//         logger.error( "test " + test + "[" + n + "]", e );
//         errors = true;
//     }
////     System.out.println("End of " + test);
// }


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