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

package org.apache.jena.iri.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.iri.ViolationCodes ;
import org.apache.jena.iri.impl.AbsIRIImpl ;
import org.apache.jena.iri.impl.PatternCompiler ;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TestMoreExamples extends TestCase implements
        ViolationCodes {
    static class TestReader extends DefaultHandler {
        private Stack<Test> stack = new Stack<>();

        TestReader(TestSuite s) {
            stack.push(s);
        }
        private void push(Test t) {
            ((TestSuite)stack.peek()).addTest(t);
            stack.push(t);
        }

        @Override
        public void startElement(String arg1, String arg2, String name,
                Attributes att) {
            if (name.equals("IRI"))
                push(new TestMEIri(att));
            else if (name.equals("Result"))
                push(new TestMEResult(att,(TestSuite)stack.peek()));
            else if (name.equals("Relativize"))
                push(new TestMERelativize(att,(TestSuite)stack.peek()));
            else if (name.equals("Resolve"))
                push(new TestSuite());
            else if (!name.equals("UriTests"))
                add(name, att);
        }

        private void add(String name, Attributes att) {
            ((TestMoreExamples) stack.peek()).add(name, att);
        }

        @Override
        public void characters(char ch[], int st, int lg) {
            String text = new String(ch,st,lg).trim();
            if (text.length()>0)
                ((TestMoreExamples) stack.peek()).add(text);
        }
        @Override
        public void endElement(String arg1, String arg2, String name) {
            if (name.equals("Resolve")) {
                TestSuite t = (TestSuite) stack.pop();
                t.
                setName(((TestCase)t.testAt(0)).getName() + "  " +
                        ((TestCase)t.testAt(1)).getName());
            } else if (name.equals("IRI") || name.equals("Result")
                    || name.equals("Relativize")) {
                stack.pop();
            }

        }

    }

    static Map<String, String> attr2map(Attributes a) {
        Map<String, String> rslt = new HashMap<>();
        for (int i = a.getLength()-1;i>=0;i--)
            rslt.put(a.getQName(i),a.getValue(i));
        return rslt;
    }
    Map<String, String> att;
    TestSuite parent;
    private Map<String, Map<String, String>> methods = new HashMap<>();
    private long violations = 0l;
    private IRI iri;

    public TestMoreExamples(String nm, Attributes att) {
        this(nm,att,null);
    }

    private String savedText = null;
    public void add(String text) {
        if (savedText!=null) {
            text = savedText + text;
            savedText = null;
//            System.err.println(text);
        }
        try {
        violations |= (1l << PatternCompiler.errorCode(text));
        }
        catch (NoSuchFieldException e){
                savedText = text;
        }
    }

    public TestMoreExamples(String nm, Attributes att, TestSuite suite) {
        super(escape(nm));
        this.att = attr2map(att);
        this.parent = suite;
    }

    private static String escape(String nm) {
        StringBuilder rslt = new StringBuilder();
        for (int i=0; i<nm.length();i++) {
            char ch = nm.charAt(i);
            if (ch>=32 && ch<=126)
                rslt.append(ch);
            else
                rslt.append("\\u"+pad4(Integer.toHexString(ch)));
                
        }
        return rslt.toString();
    }

    private static String pad4(String string) {
        switch (string.length()) {
        case 0:
            return "0000";
        case 1:
            return "000"+string;
        case 2:
            return "00"+string;
        case 3:
            return "0"+string;
            default:
                return string;
       
        }
    }

    public TestMoreExamples(String string) {
        super(escape(string));
    }
    
//    static int cnt = 0;
    
    @Override
    public void setUp() throws Exception {
//        System.err.println("setUp"+cnt);
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
//        System.err.println("tearDown"+cnt++);
        super.tearDown();
    }
    private void add(String name, Attributes attrs) {
        if (name.equals("violation"))
            return;
        if (name.equals("violations"))
            return;
        methods.put(name,attr2map(attrs));
    }

    private long getViolations() {
    	long result = 0l;
    	Iterator<Violation> it = ((AbsIRIImpl)iri).allViolations();
        while (it.hasNext()) {
           result |= (1l<<(it.next()).getViolationCode());
                  
        }
        return result;
    }
    @Override
    public void runTest() {
//        System.err.println("runTest"+cnt + " " + getName());
       iri = getIRI();
       
       
       assertEquals("violations",violations,getViolations());
       
       Iterator<Map.Entry<String, Map<String,String>>> it = methods.entrySet().iterator();
       while (it.hasNext()) {
           Map.Entry<String, Map<String,String>> ent = it.next();
           String m = ent.getKey();
           Map<String,String> attrs = ent.getValue();
           try {
               Object r = IRI.class.getDeclaredMethod(m,TestCreator.nullSign)
                .invoke(iri,new Object[]{});
               if (r==null)
                   assertEquals(attrs.get("nullValue"),"true");
               else
                   assertEquals(attrs.get("value"),r.toString());
               
            } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }
           catch (InvocationTargetException e) {
                Throwable t = e;
                if (t.getCause()!=null)
                    t= t.getCause();
                String s = t.getMessage()!=null?t.getMessage():t.toString();
                
                assertEquals(attrs.get("exception"),s);
            }
       }
    }

    final IRI getIRI() { if (iri==null) iri = computeIRI(); return iri; }

    IRI computeIRI() {
        throw new UnsupportedOperationException();
    }

    static TestSuite suitex() throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        TestSuite result = new TestSuite();
        result.setName("More IRI Tests");
        try (InputStream in = TestCreator.class.getClassLoader().getResourceAsStream("org/apache/jena/iri/test/test.xml")) {
            fact.newSAXParser().parse(in,new TestReader(result));
            return result;
        }
    }
    public static TestSuite suite() {
        try {
            return 
             suitex();
            
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
//        System.err.println("Yes chris we know");
//        return 
        TestSuite r2 = new TestSuite("exception-while-building-testsuite");
//        r2.addTest(new TestMoreExamples("testDummy"));
        return r2;
    }
}
