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
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.IRIRelativize;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.iri.impl.AbsIRIImpl;

final class TestCreator extends DefaultHandler implements IRIRelativize {
    
    public static String substituteStandardEntities(String s) {
        s = replace(s, "&", "&amp;");
        s = replace(s, "<", "&lt;");
        s = replace(s, ">", "&gt;");
        s = replace(s, "'", "&apos;");
        s = replace(s, "\t", "&#9;");
        s = replace(s, "\n", "&#xA;");
        s = replace(s, "\r", "&#xD;");
        return replace(s, "\"", "&quot;");
    }

    public static String replace(
        String s,
        String oldString,
        String newString) {
        String result = "";
        int length = oldString.length();
        int pos = s.indexOf(oldString);
        int lastPos = 0;
        while (pos >= 0) {
            result = result + s.substring(lastPos, pos) + newString;
            lastPos = pos + length;
            pos = s.indexOf(oldString, lastPos);
        }
        return result + s.substring(lastPos, s.length());
    }
//    static final IRI empty = IRIFactory.defaultFactory().emptyIRI();
    static final Class attSign[] = new Class[] { Attributes.class };
    static final Class nullSign[] = new Class[] { };

    static PrintWriter out;
    static void load() throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        out = new PrintWriter(new OutputStreamWriter(
          new FileOutputStream("src/com/hp/hpl/jena/iri/test/test.xml"),
          "utf-8"
        ));
        out.println("<UriTests>");
        
        InputStream in = TestCreator.class.getClassLoader().getResourceAsStream("com/hp/hpl/jena/iri/test/uris.xml");
            fact.newSAXParser().parse(in,
            new TestCreator()
            );

       out.println("</UriTests>");
       in.close();
       out.close();
    }
    
    static public void main(String args[]) throws IOException, ParserConfigurationException, SAXException{
        try {
            load();
        } catch (SAXParseException e) {
            System.err.println(e.getLineNumber());
            System.err.println(e.toString());
            System.err.println(e.getMessage());
            
        } 
    }
    
    
    public void startElement(
    String arg1,
    String arg2,
    String name,
    Attributes att
    ) {
        try {
            this.getClass().getDeclaredMethod(name,attSign)
            .invoke(this,new Object[]{att});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public void endElement(
            String arg1,
            String arg2,
            String name
            ) {
    }
            
    private void uris(Attributes att) {
    }

    private void uri(Attributes att) {
        String absolute = att.getValue("absolute");
        String base = att.getValue("base");
        String relative = att.getValue("relative");
        
        doIt(absolute);
        
        if (base!=null) {
            out.println("<Resolve>");
            IRI b = doIt(base);
            IRI r = doIt(relative);
            out.println("<Result>");
            IRI result = b.create(r);
            doIt(result);
            out.println("</Result>");
            IRI rAgain =  b.relativize(
                    result,
                  ABSOLUTE|GRANDPARENT|NETWORK|PARENT|CHILD|SAMEDOCUMENT  
                    );
            if (r.equals(rAgain)) {
                out.println("<Relativize same='true'/>");
            } else {
                out.println("<Relativize>");
                  doIt(rAgain);
                out.println("</Relativize>");
            }
            
           out.println("</Resolve>");
        }
    }


    static String methods[] =  {
        "getHost",
        "getPath",
        "getPort",
        "getQuery",
        "getScheme",
        "getUserinfo",
        "getFragment",
//        "hasException",
        "isAbsolute",
//        "isIRI",
        "isOpaque",
//        "isRDFURIReference",
        "isRelative",
//        "isURIinASCII",
//        "isVeryBad",
//        "isXSanyURI",
        "toASCIIString"
    };

    private void doIt(IRI iri) {
        if (iri==null)
            return;
        for (int i=0;i<methods.length;i++) {
            String m = methods[i];
            try {
               Object r = IRI.class.getDeclaredMethod(m,nullSign)
                .invoke(iri,new Object[]{});
               if (r==null)
                       out.println("<"+m+
                               " nullValue='true'/>"
                               );
               else
               out.println("<"+m+
                       " value='" +
                       substituteStandardEntities(r.toString())
                       + "'/>"
                       );
               
               
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Throwable t = e;
                if (t.getCause()!=null)
                    t= t.getCause();
                String s = t.getMessage()!=null?t.getMessage():t.toString();
                out.println("<"+m+
                        " exception='" +
                        substituteStandardEntities(s)
                        + "'/>"
                        );
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            
        }

        Iterator it = ((AbsIRIImpl)iri).allViolations();
        out.println("<exceptions>");
        while (it.hasNext()) {
            out.print("<exception>");
            out.print(((Violation)it.next()).codeName());
            out.println("</exception>");
                    
        }
        out.println("</exceptions>");
    }

    private IRI doIt(String iri) {
        if (iri==null)
            return null;
        IRI rslt = 
            factory.create(iri);
//            empty.create(iri);
        out.println("<IRI iri='"+substituteStandardEntities(iri)+"'>");
        doIt(rslt);
        out.println("</IRI>");
        return rslt;
    }
    // TODO set conformance level for this factory
    static private IRIFactory factory = new IRIFactory();
}

/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
