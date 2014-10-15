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

package org.apache.jena.iri;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.iri.ViolationCodes ;
import org.apache.jena.iri.impl.Force ;
import org.apache.jena.iri.impl.IRIExamples ;
import org.apache.jena.iri.impl.Specification ;
import org.apache.jena.iri.impl.ViolationCodeInfo ;


public class TestExample extends TestCase
   implements ViolationCodes {
    static {
        new ViolationCodes.Initialize();
    }

    // static {
    // Iterator it = Specification.iris.values().iterator();
    // while (it.hasNext())
    // System.err.println(it.next().getClass().toString());
    // }
    static Specification specs[] = Specification.all
                    .values().toArray(new Specification[0]);

    static IRIFactory all[][] = new IRIFactory[specs.length][Force.SIZE];

    
    static {
        for (int i = 0; i < specs.length; i++)
            for (int j = 0; j < Force.SIZE; j++) {
                all[i][j] = new IRIFactory();
                all[i][j].dnsViolation(false, false);
                all[i][j].mintingViolation(false, false);
                all[i][j].shouldViolation(false, false);
                all[i][j].securityViolation(false, false);
//                all[i][j].schemeViolation(false, false);
                switch (j) {
                case Force.DNS:
                    all[i][j].dnsViolation(false, true);
                    break;
                case Force.MINTING:
                    all[i][j].mintingViolation(false, true);
                    break;
                case Force.MUST:
                    break;
                case Force.SECURITY:
                    all[i][j].securityViolation(false, true);
                    break;
                case Force.SHOULD:
                    all[i][j].shouldViolation(false, true);
                    break;
//                case Force.SCHEME_SPECIFIC:
//                    all[i][j].schemeViolation(false, true);
//                    all[i][j].useSchemeSpecificRules("*");
//                    break;
                }
                if (specs[i].name().equals("IRI")) {
                    all[i][j].useSpecificationIRI(false);
                        
                }
                if (specs[i].name().equals("URI"))
                    all[i][j].useSpecificationURI(false);
                if (specs[i].name().equals("XLink"))
                    all[i][j].useSpecificationXLink(false);
                if (specs[i].name().equals("RDF"))
                    all[i][j].useSpecificationRDF(false);
                if (specs[i].name().equals("XML"))
                    all[i][j].useSpecificationXMLSystemID(false);
                if (specs[i].name().equals("Schema"))
                    all[i][j].useSpecificationXMLSchema(false);

            }
    }

    String uri;

    ViolationCodeInfo violation;

    int specID;
    
    boolean good;

    public TestExample(int spec, String uri, ViolationCodeInfo info, boolean good) {
        super(escapeAndShorten(uri));
        this.uri = uri;
        this.violation = info;
        specID = spec;
        this.good = good;
    }

//    public TestExample(String uri, ViolationCodeInfo info, boolean good) {
//        super(escapeAndShorten(uri));
//        this.uri = uri;
//        this.violation = info;
//        specID = -1;
//    }

    private static String escapeAndShorten(String uri2) {
        StringBuilder rslt = new StringBuilder();
        int ln = uri2.length();
        if (ln > 80)
            ln = 80;
        for (int i = 0; i < ln; i++) {
            int ch = uri2.charAt(i);
            if (ch > 127 || ch < 32) {
                rslt.append("&#");
                rslt.append(ch);
                rslt.append(";");
            } else
                rslt.append((char) ch);
        }
        return rslt.toString();
    }

    @Override
    public void runTest() {
        if (specID == -1)
            runTestErrorCode();
        else if (violation==null)
            runTestJustSpec();
        else
            runTestSpec();
    }

    private void runTestJustSpec() {
        IRIFactory f = 
            specs[specID].isSchemeSpec()?
                    IRIFactory.iriImplementation():
                    all[specID][Force.MUST];
        
        IRI iri = f.create(uri);
        if (iri.hasViolation(false)) {
            if (good) fail("Unexpected violation found: "+
            ((iri.violations(false).next())).codeName()
            
            );
        } else {
            if (!good) fail("Expected a violation, none found.");
        }
            
        
    }

	

    private void runTestErrorCode() {
        IRIFactory f = new IRIFactory();
        f.setIsError(violation.getCode(), true);

        runTest(f,
                true,
                false,
                "Just this error");

    }

    private void runTestSpec() {
        boolean inSpec = violation.appliesTo(specs[specID]);
        int force = violation.getForce();
        for (int f = 0; f < Force.SIZE; f++) {
            
            runTest(all[specID][f],
                    (force & Force.must)!=0 && inSpec,
                    (force & (1<<f))!=0 && inSpec,
                    "Force: " + f + "; spec: " + specs[specID].name());
        }

    }

    private void runTest(IRIFactory f, boolean expectError,
            boolean expectWarning, String desc) {
        IRI iri = f.create(uri);
        boolean implemented = violation.isImplemented();
        expectError = expectError && implemented;
        expectWarning = expectWarning && (!expectError) && implemented;
        if (good) {
            expectError = expectWarning = false;
        }
        boolean hasError = false;
        boolean hasWarning = false;
        Iterator<Violation> it = iri.violations(true);
        while (it.hasNext()) {
            Violation v = it.next();
            if (v.getViolationCode() == violation.getCode()) {
                if (v.isError()) {
                    if (!expectError)
                        fail("Unexpected error, "+desc);
                    hasError = true;
                } else {
                    if (!expectWarning)
                        fail("Unexpected warning, "+desc);
                    hasWarning = true;
                }
                break;
            }
        }
        if (expectWarning && !hasWarning)
            fail("No warning detected: "+expectError);
        if (expectError && !hasError)
            fail("No error detected: "+expectError);
    }

    public static TestSuite suite() {
        TestSuite rslt = new TestSuite();

        rslt.setName("Examples from documentation");
        for (int sp = 0; sp < specs.length; sp++) {
            TestSuite spec = new TestSuite();
            
            String specName = specs[sp].name();
            spec.setName(specName);
//            if (!specName.equals("http"))
//                continue;
            if (specs[sp].isIRISpec())
                addAllTestsFromExamples(sp, spec);
            
            addExamples(sp,null,specs[sp],spec);
            if (spec.countTestCases()>0)
                rslt.addTest(spec);
        }
//        if (false)
        addAllTestsFromExamples(-1, rslt);
        return rslt;
    }

    private static void addAllTestsFromExamples(int sp, TestSuite spec) {
        for (int i = 0; i < ViolationCodeInfo.all.length; i++)
            addTestsFromExamples(spec, sp, ViolationCodeInfo.all[i]);
    }

    private static void addTestsFromExamples(TestSuite rslt, int sp, ViolationCodeInfo violationCodeInfo) {
      
        if (violationCodeInfo != null) {
            TestSuite ex = new TestSuite();
            ex.setName(violationCodeInfo.getCodeName());
            addExamples(sp, violationCodeInfo, violationCodeInfo, ex);
            if (ex.countTestCases()>0)
            rslt.addTest(ex);
        }
    }

    private static void addExamples(int sp, ViolationCodeInfo violationCodeInfo, IRIExamples examples, TestSuite ex) {
        String e[] = examples.getBadExamples();
        for ( String anE1 : e )
        {
            ex.addTest( new TestExample( sp, anE1, violationCodeInfo, false ) );
        }
        e = examples.getGoodExamples();
        for ( String anE : e )
        {
            ex.addTest( new TestExample( sp, anE, violationCodeInfo, true ) );
        }
    }

}
