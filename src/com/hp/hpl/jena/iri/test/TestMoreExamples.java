/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.*;

import com.hp.hpl.jena.iri.*;
import com.hp.hpl.jena.iri.impl.PatternCompiler;

public class TestMoreExamples extends TestCase implements
        ViolationCodes {
    static class TestReader extends DefaultHandler {
        private Stack stack = new Stack();

        TestReader(TestSuite s) {
            stack.push(s);
        }
        private void push(Test t) {
            ((TestSuite)stack.peek()).addTest(t);
            stack.push(t);
        }

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

        public void characters(char ch[], int st, int lg) {
            String text = new String(ch,st,lg).trim();
            if (text.length()>0)
                ((TestMoreExamples) stack.peek()).add(text);
        }
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

    Attributes att;
    TestSuite parent;
    private Map methods = new HashMap();
    private long violations = 0l;
    private IRI iri;

    public TestMoreExamples(String nm, Attributes att) {
        this(nm,att,null);
    }

    public void add(String text) {
        violations |= (1l << PatternCompiler.errorCode(text));
    }

    public TestMoreExamples(String nm, Attributes att, TestSuite suite) {
        super(nm);
        this.att = att;
        this.parent = suite;
    }

    public TestMoreExamples(String string) {
        super(string);
    }
    
    static int cnt = 0;
    
    public void setUp() throws Exception {
        System.err.println("setUp"+cnt);
        super.setUp();
    }

    public void tearDown() throws Exception {
        System.err.println("tearDown"+cnt++);
        super.tearDown();
    }
    private void add(String name, Attributes att) {
        if (name.equals("violation"))
            return;
        if (name.equals("violations"))
            return;
//        methods.put(name,att);
    }

    public void runTest() {
        System.err.println("runTest"+cnt);
//       iri = getIRI();
//       Iterator it = methods.entrySet().iterator();
//       while (it.hasNext()) {
//           Map.Entry e = (Map.Entry)it.next();
//           String method = (String)e.getKey();
//           Attributes att = (Attributes)e.getValue();
//       }
    }

    final IRI getIRI() { if (iri!=null) iri = computeIRI(); return null; }

    private IRI computeIRI() {
        return null;
    }

    static TestSuite suitex() throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        TestSuite result = new TestSuite();
        result.setName("More IRI Tests");
        InputStream in = TestCreator.class.getClassLoader().getResourceAsStream("com/hp/hpl/jena/iri/test/test.xml");
            fact.newSAXParser().parse(in,
            new TestReader(result)
            );

       in.close();
       return result;
    }
    public static TestSuite suite() {
        TestSuite r;
        try {
            return 
             r = suitex();
            
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        System.err.println("Yes chris we know");
//        return 
        TestSuite r2 = new TestSuite("exception-while-building-testsuite");
        r2.addTest(new TestMoreExamples("testDummy"));
        return r2;
    }
    
    static public void main(String args[]) throws IOException, ParserConfigurationException, SAXException{
//        try {
////            load();
//        } catch (SAXParseException e) {
//            System.err.println(e.getLineNumber());
//            System.err.println(e.toString());
//            System.err.println(e.getMessage());
//            
//        } 
    }
    
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

