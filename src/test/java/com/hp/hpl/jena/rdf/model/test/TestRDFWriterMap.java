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

package com.hp.hpl.jena.rdf.model.test;

import java.util.*;

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.n3.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.xmloutput.impl.*;

public class TestRDFWriterMap extends JenaTestBase
    {
    public static final String TURTLE_WRITER_ALT2 = N3JenaWriter.turtleWriterAlt2;
    public static final String TURTLE_WRITER_ALT1 = N3JenaWriter.turtleWriterAlt1;
    public static final String TURTLE_WRITER = N3JenaWriter.turtleWriter;

    public static final String RDF_XML ="RDF/XML";
    public static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
    public static final String NTRIPLE = "N-TRIPLE";
    public static final String NTRIPLES = "N-TRIPLES";
    public static final String N3 = "N3";
    public static final String N3_PLAIN = "N3-PLAIN";
    public static final String N3_PP = "N3-PP";
    public static final String N3_TRIPLE = "N3-TRIPLE";
    public static final String N3_TRIPLES = "N3-TRIPLES";
    
    public static class RDFWriterMap implements RDFWriterF
        {
        protected final Map<String, Class<RDFWriter>> map = new HashMap<String, Class<RDFWriter>>();
        
        public RDFWriterMap( boolean preloadDefaults )
            { if (preloadDefaults) loadDefaults(); }
        
        private void loadDefaults()
            {
            setWriterClassName( TURTLE_WRITER, Jena.PATH + ".n3.N3TurtleJenaWriter" );
            setWriterClassName( TURTLE_WRITER_ALT1, Jena.PATH + ".n3.N3TurtleJenaWriter" );
            setWriterClassName( TURTLE_WRITER_ALT2, Jena.PATH + ".n3.N3TurtleJenaWriter" );
            setWriterClassName( RDF_XML, Jena.PATH + ".xmloutput.impl.Basic" );
            setWriterClassName( RDF_XML_ABBREV, Jena.PATH + ".xmloutput.impl.Abbreviated" );
            setWriterClassName( N3, Jena.PATH + ".n3.N3JenaWriter" );
            setWriterClassName( N3_PLAIN, Jena.PATH + ".n3.N3JenaWriterPlain" );
            setWriterClassName( N3_PP, Jena.PATH + ".n3.N3JenaWriterPP" );
            setWriterClassName( N3_TRIPLE, Jena.PATH + ".n3.N3JenaWriterTriples" );
            setWriterClassName( N3_TRIPLES, Jena.PATH + ".n3.N3JenaWriterTriples" );
            setWriterClassName( NTRIPLE, Jena.PATH + ".rdf.model.impl.NTripleWriter" );
            setWriterClassName( NTRIPLES, Jena.PATH + ".rdf.model.impl.NTripleWriter" );
            }

        @Override
        public RDFWriter getWriter()
            { return getWriter( "RDF/XML" ); }

        @Override
        public RDFWriter getWriter( String lang )
            {
            Class<RDFWriter> result = map.get( lang );
            if (result == null)
                throw new NoWriterForLangException( lang );
            try
                { return result.newInstance(); }
            catch (Exception e)
                { throw new JenaException( e ); }
            }

        @Override
        public String setWriterClassName( String lang, String className )
            {
            try
                {
                Class<RDFWriter> old = map.get( lang );
                Class<?> c = Class.forName( className );
                if (RDFWriter.class.isAssignableFrom( c ))
                {
                    @SuppressWarnings("unchecked")
                    Class<RDFWriter> x = (Class<RDFWriter>)c ;
                    map.put( lang, x );
                }
                return old == null ? null : old.getName();
                }
            catch (ClassNotFoundException e)
                { throw new JenaException( e ); }
            }
        }

    public TestRDFWriterMap( String name )
        { super( name );  }
    
    public void testMe()
        {
        fail( "SPOO" );
        }

    public void testWritersAbsent()
        {
        testWriterAbsent( TURTLE_WRITER );
        testWriterAbsent( TURTLE_WRITER_ALT1 );
        testWriterAbsent( TURTLE_WRITER_ALT2 );
        testWriterAbsent( RDF_XML );
        testWriterAbsent( RDF_XML_ABBREV );
        testWriterAbsent( NTRIPLE );
        testWriterAbsent( NTRIPLES );
        testWriterAbsent( N3 );
        testWriterAbsent( N3_PP );
        testWriterAbsent( N3_PLAIN );
        testWriterAbsent( N3_TRIPLE );
        testWriterAbsent( N3_TRIPLES );
        }
    
   private void testWriterAbsent( String w )
        {
        RDFWriterF x = new RDFWriterMap( false );
        try { x.getWriter( w ); }
        catch (NoWriterForLangException e)
            { assertEquals( w, e.getMessage() ); }
        }

   public void testWritersPresent()
        {
        RDFWriterF x = new RDFWriterMap( true );
        assertEquals( N3TurtleJenaWriter.class, x.getWriter( TURTLE_WRITER ).getClass() );
        assertEquals( N3TurtleJenaWriter.class, x.getWriter( TURTLE_WRITER_ALT1 ).getClass() );
        assertEquals( N3TurtleJenaWriter.class, x.getWriter( TURTLE_WRITER_ALT2 ).getClass() );
        assertEquals( Basic.class, x.getWriter( RDF_XML ).getClass() );
        assertEquals( Abbreviated.class, x.getWriter( RDF_XML_ABBREV ).getClass() );
        assertEquals( NTripleWriter.class, x.getWriter( NTRIPLE ).getClass() );
        assertEquals( NTripleWriter.class, x.getWriter( NTRIPLES ).getClass() );
        assertEquals( N3JenaWriter.class, x.getWriter( N3 ).getClass() );
        assertEquals( N3JenaWriterPP.class, x.getWriter( N3_PP ).getClass() );
        assertEquals( N3JenaWriterPlain.class, x.getWriter( N3_PLAIN ).getClass() );
        assertEquals( N3JenaWriterTriples.class, x.getWriter( N3_TRIPLE ).getClass() );
        assertEquals( N3JenaWriterTriples.class, x.getWriter( N3_TRIPLES ).getClass() );
        }
   
   public void testDefaultWriter()
       {
       RDFWriterF x = new RDFWriterMap( true );
       assertEquals( x.getWriter( "RDF/XML" ).getClass(), x.getWriter().getClass() );
       }
    }
