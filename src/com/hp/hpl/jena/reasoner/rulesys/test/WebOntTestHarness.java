/******************************************************************
 * File:        WebOntTestHarness.java
 * Created by:  Dave Reynolds
 * Created on:  12-Sep-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: WebOntTestHarness.java,v 1.2 2003-09-15 14:58:12 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

//import java.io.*;
//import java.util.*;

/**
 * Test harness for running the WebOnt working group tests relevant 
 * to the OWL rule reasoner. See also TestOWLRules which runs the
 * core WG tests as part of the routine unit tests.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-09-15 14:58:12 $
 */
public class WebOntTestHarness {

    /** The base directory for the working group test files to use */
    public static final String baseTestDir = "testing/wg";
    
    /** The list of subdirectories to process (omits the rdf/rdfs dirs) */
    public static String[] testDirs= {"AllDifferent", "AllDistinct", 
            "AnnotationProperty", "DatatypeProperty", "FunctionalProperty",
            "I3.2", "I3.4", "I4.1", "I4.5", "I4.6", "I5.1", "I5.2", "I5.21", "I5.24",
            "I5.26", "I5.3", "I5.5", "I5.8", "InverseFunctionalProperty", "Nothing", 
            "Restriction", "SymmetricProperty", "Thing", "TransitiveProperty", 
            "allValuesFrom", "amp-in-url", "cardinality", "complementOf", "datatypes", 
            "description-logic", "differentFrom", "disjointWith", "distinctMembers", 
            "equivalentClass", "equivalentProperty", "extra-credit", "imports", 
            "intersectionOf", "inverseOf", "localtests", "maxCardinality", "miscellaneous",
            "oneOf", "oneOfDistinct", "sameAs", "sameClassAs", "sameIndividualAs", 
            "samePropertyAs", "someValuesFrom", "statement-entailment", "unionOf", 
            "unrecognised-xml-attributes", "xmlbase"};
            
     

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