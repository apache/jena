/*
 *  (c) Copyright Hewlett-Packard Company 2001,2002
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
 *
 * $Id: testModelEquals.java,v 1.3 2003-02-01 14:35:14 bwm Exp $
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;
import com.hp.hpl.jena.mem.ModelMem;

/**
 *
 * @author  bwm
 */
public class testModelEquals extends Object {
   
    
    protected static void doTest(Model m1, Model m2) {
        (new testModelEquals()).test(m1, m2);
    }

    void test(Model m1, Model m2) {

        String  test = "testModelEquals";
        String  filebase = "modules/rdf/regression/" + test + "/";
        boolean results[] = { 
            false, true, true, true, true, false, false, true, false };
    //    System.out.println("Beginning " + test);
        int n = 0;
        try {
            for (n=1; n<7; n++) {
                empty(m1); empty(m2);
                m1.read(
                    ResourceReader.getInputStream(filebase + Integer.toString(n) + "-1.rdf"),
                    "");
                m2.read(
                    ResourceReader.getInputStream(filebase + Integer.toString(n) + "-2.rdf"),
                    "");
                if (! (m1.isIsomorphicWith(m2) == results[n])) {
                    error(test, n);
                    System.out.println("m1:");
                    m1.write(System.out, "N-TRIPLE");
                    System.out.println("m2:");
                    m2.write(System.out, "N-TRIPLE");
                }
            }
            for (n=7; n<9; n++) {
                empty(m1); empty(m2);
                m1.read(
                    ResourceReader.getInputStream(filebase + Integer.toString(n) + "-1.nt"),
                                   "", "N-TRIPLE");
                m2.read(
                    ResourceReader.getInputStream(filebase + Integer.toString(n) + "-2.nt"),
                                   "", "N-TRIPLE");
                if (! (m1.isIsomorphicWith(m2) == results[n])) {
                    error(test, n);
                    System.out.println("m1:");
                    m1.write(System.out, "N-TRIPLE");
                    System.out.println("m2:");
                    m2.write(System.out, "N-TRIPLE");
                }
            }
        } catch (Exception e) {
            inError = true;
            ErrorHelper.logInternalError(" test " + test, n, e);
        }
     //   System.out.println("End of " + test);        
    }
    
    static protected void empty(Model m) throws RDFException {
        StmtIterator iter = m.listStatements();
        while (iter.hasNext()) {
            iter.nextStatement();
            iter.remove();
        }
    }
    
    private boolean inError = false;
       
    protected void error(String test, int n) {
        System.out.println(test + ": failed test " + Integer.toString(n));
        inError = true;
    }
    
    public boolean getErrors() {
        return inError;
    }
    
    // RUN THIS TEST ONLY
    static public void main(String args[]) throws RDFException
    {
        doTest(new ModelMem(), new ModelMem());
    }
}
