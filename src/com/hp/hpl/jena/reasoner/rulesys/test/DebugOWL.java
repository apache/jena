/******************************************************************
 * File:        DebugOWL.java
 * Created by:  Dave Reynolds
 * Created on:  12-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DebugOWL.java,v 1.1 2003-06-12 17:10:34 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.*;
//import com.hp.hpl.jena.reasoner.*;

/**
 * Test harnness for investigating OWL reasoner correctness and performance
 * on specific local test files. Unit testing is done using OWLWGTester or simplar,
 * this code is a debugging tools rather than a tester.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-12 17:10:34 $
 */
public class DebugOWL {

    /** The trial file to be loaded and processed */
    public String testFile;
    
    /** The raw tests data as a Graph */
    Graph testdata;
    
    /**
     * Construct a test harness.
     */
    public DebugOWL(String testFile) {
        this.testFile = testFile;
    }
    
    /**
     * Load the named file into the harness.
     */
    void setUp() {
        
    }
    
    public static void main(String[] args) {
        try {
            DebugOWL tester = new DebugOWL("file:testing/ontology/owl/list-syntax/test-with-import.rdf");
            tester.setUp();
            
        } catch (Exception e) {
            System.out.println("Problem: " + e);
            e.printStackTrace();
        }
    }
    
}


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/