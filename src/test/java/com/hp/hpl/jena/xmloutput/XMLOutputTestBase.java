/*
    (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved. [See end of file]
    $Id: XMLOutputTestBase.java,v 1.1 2009-07-04 16:41:34 andy_seaborne Exp $
*/
package com.hp.hpl.jena.xmloutput;

import java.io.*;
import java.util.Properties;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.xmloutput.impl.BaseXMLWriter;
import com.hp.hpl.jena.xmloutput.impl.SimpleLogger;

public class XMLOutputTestBase extends ModelTestBase
    {
    protected final String lang;

    public XMLOutputTestBase( String name, String lang )
        { 
        super( name ); 
        this.lang = lang; 
        }

    static SimpleLogger realLogger;
    
    static boolean sawErrors;
    
    static SimpleLogger falseLogger = new SimpleLogger() 
        {
        @Override
        public void warn(String s) { sawErrors = true; }

        @Override
        public void warn(String s, Exception e) { sawErrors = true; }
        };
    
    static void blockLogger() 
        {
        realLogger = BaseXMLWriter.setLogger( falseLogger );
        sawErrors = false;
        }
    
    static boolean unblockLogger() 
        {
        BaseXMLWriter.setLogger( realLogger );
        return sawErrors;
        }
    
    static protected class Change 
        {
        public void modify( RDFWriter w ) {}
        public void modify( Model m ) {}
        
        public void modify( Model m, RDFWriter w ) { modify(m); modify(w); }
        
        public static Change none()
            { return new Change(); }
        
        public static Change setProperty( final String property, final String value )
            {
            return new Change()
                { @Override
                public void modify( RDFWriter writer )
                    { writer.setProperty( property, value ); }
                };
            }
        
        public static Change setProperty( final String property, final boolean value )
            {
            return new Change()
                { @Override
                public void modify( RDFWriter writer )
                    { writer.setProperty( property, Boolean.valueOf( value ) ); }
                };
            }
        
        public static Change setPrefix( final String prefix, final String URI )
            {
            return new Change()
                { @Override
                public void modify( Model m )
                    { m.setNsPrefix( prefix, URI ); }
                };
            }
           
        public static Change blockRules( String ruleName )
            { return setProperty( "blockrules", ruleName ); }
        
        public Change andSetPrefix( String prefix, String URI )
            { return and( Change.setPrefix( prefix, URI ) ); }
        
        private Change and( final Change change )
            { return new Change()
                { @Override
                public void modify(  Model m, RDFWriter w )
                    {
                    Change.this.modify( m, w );
                    change.modify( m, w );
                    }
                };
            }
        }  
    
    /**
     * @param code Stuff to do to the writer.
     * @param filename Read this file, write it out, read it in.
     * @param regex    Written file must match this.
     */
    protected void check( String filename, String regex, Change code)
        throws IOException 
        {
        check( filename, regex, null, code );
        }
    
    protected void check(
        String filename,
        String regexPresent,
        String regexAbsent,
        Change code )
        throws IOException {
        check( filename, null, regexPresent, regexAbsent, false, code);
    }

    protected void check(
        String filename,
        String encoding,
        String regexPresent,
        String regexAbsent,
        Change code)
        throws IOException {
        check(filename, encoding, regexPresent, regexAbsent, false, code);
    }
    
    protected void check
        (
        String filename,
        String regexAbsent,
        Change code,
        String base
        )
        throws IOException 
        {
        check( filename, null, regexAbsent, null, false, Change.none(), base );
        check( filename, null, null, regexAbsent, false, code, base );
        }
    
    protected void check(
        String filename,
        String encoding,
        String regexPresent,
        String regexAbsent,
        boolean errs,
        Change code)
        throws IOException {
        check(filename, encoding, regexPresent, regexAbsent, errs, code, "file:"+filename);
    }
    
    protected void check(
        String filename,
        String encoding,
        String regexPresent,
        String regexAbsent,
        boolean errorExpected,
        Change code,
        String base)
        throws IOException {
        //TestLogger tl = new TestLogger(BaseXMLWriter.class);
        blockLogger();
        boolean errorsFound;
        Model m = createMemModel();
        InputStream in = new FileInputStream(filename);
        m.read(in,base);
        in.close();
        //m.read(filename);
        Writer sw;
        ByteArrayOutputStream bos = null;
        if (encoding == null)
            sw = new StringWriter();
        else {
            bos = new ByteArrayOutputStream();
            sw = new OutputStreamWriter(bos, encoding);
        }
        Properties p = (Properties) System.getProperties().clone();
        RDFWriter writer = m.getWriter(lang);
        code.modify( m, writer );
        writer.write( m, sw, base );
        sw.close();

        String contents;
        if (encoding == null)
            contents = sw.toString();
        else {
            contents = bos.toString(encoding);
        }
        try {
            Model m2 = createMemModel();
            m2.read(new StringReader(contents), base);
            assertTrue("Data got changed.",m.isIsomorphicWith(m2));
            if (regexPresent != null)
                assertTrue(
                    "Should find /" + regexPresent + "/",
                    Pattern.compile(regexPresent,Pattern.DOTALL).matcher(contents).find()
//                  matcher.contains(contents, awk.compile(regexPresent))
                    );
            if (regexAbsent != null)
                assertTrue(
                    "Should not find /" + regexAbsent + "/",
                    !Pattern.compile(regexAbsent,Pattern.DOTALL).matcher(contents).find()
//                  !matcher.contains(contents, awk.compile(regexAbsent))
                    );
            contents = null;
        } finally {
            errorsFound = unblockLogger();
            System.setProperties(p);
            if (contents != null) {
                System.err.println("===================");
                System.err.println("Offending content - " + toString());
                System.err.println("===================");
                System.err.println(contents);
                System.err.println("===================");
            }
        }
        assertEquals("Errors (not) detected.", errorExpected, errorsFound);

    }

    }

/*
 *  (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
*/
