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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Arrays;
import java.util.Properties;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.n3.N3JenaWriter;


/**
 */
public class RDFWriterFImpl extends Object implements RDFWriterF {

    protected static Properties langToClassName = null;

    // predefined languages - these should probably go in a properties file

    protected static final String LANGS[] =
        { "RDF/XML",
          "RDF/XML-ABBREV",
          
          "N-TRIPLE",
          "N-TRIPLES",
          "N-Triples",
          
          "N3",
          N3JenaWriter.n3WriterPrettyPrinter,
          N3JenaWriter.n3WriterPlain,
          N3JenaWriter.n3WriterTriples,
          N3JenaWriter.n3WriterTriplesAlt,
          
          N3JenaWriter.turtleWriter,
          N3JenaWriter.turtleWriterAlt1, 
          N3JenaWriter.turtleWriterAlt2 
        };
    // default readers for each language

    protected static final String DEFAULTWRITERS[] =
        {
        
        
            com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic.class.getName(),
            com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated.class.getName(),
            
            com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName(),
            com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName(),
            com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName(),
            
//            /*
//            Jena.PATH + ".n3.N3JenaWriter",
//            Jena.PATH + ".n3.N3JenaWriterPP",
//
//            Jena.PATH + ".n3.N3TurtleJenaWriter",   // Write Turtle to ensure safe round tripping.
//            Jena.PATH + ".n3.N3TurtleJenaWriter",   // Ditto.
//
////            Jena.PATH + ".n3.N3JenaWriterPlain",      // Keep as N3 for now - a test fails.
////            Jena.PATH + ".n3.N3JenaWriterTriples",
//            
//            Jena.PATH + ".n3.N3JenaWriterTriples",  // Same writer, different writer name
//            Jena.PATH + ".n3.N3TurtleJenaWriter",   // Alternative names for Turtle
//            Jena.PATH + ".n3.N3TurtleJenaWriter",
//            Jena.PATH + ".n3.N3TurtleJenaWriter",             */
//            
            com.hp.hpl.jena.n3.N3JenaWriter.class.getName(),
            com.hp.hpl.jena.n3.N3JenaWriterPP.class.getName(),
            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName(),   // Write Turtle to ensure safe round tripping.
            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName(),   // Ditto.
            com.hp.hpl.jena.n3.N3JenaWriterTriples.class.getName(),     

            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName(),      // Alternative names for Turtle
            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName(),
            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()
             };

    protected static final String DEFAULTLANG = LANGS[0];

    protected static final String PROPNAMEBASE = Jena.PATH + ".writer.";

    static { // static initializer - set default readers
    	reset();
    }
    
    private static void reset() {
    	Properties newLangToClassName = new Properties();
        for (int i = 0; i < LANGS.length; i++) {
        	newLangToClassName.setProperty(
                LANGS[i],
                JenaRuntime.getSystemProperty(PROPNAMEBASE + LANGS[i], DEFAULTWRITERS[i]));
        }
        // do the setup all at once.
        langToClassName = newLangToClassName;  	
    }
    
    private static String remove( String lang) throws IllegalArgumentException {
    	if (Arrays.asList( LANGS ).contains( lang ))
    	{
    		throw new IllegalArgumentException( lang+" is a required language set in initialization");
    	}
    	Object prev = langToClassName.remove(lang);
    	return prev==null?null:prev.toString();  	
    }

    /** Creates new RDFReaderFImpl */
    public RDFWriterFImpl() {
    }

    @Override
    public RDFWriter getWriter()  {
        return getWriter(DEFAULTLANG);
    }

    @Override
    public RDFWriter getWriter(String lang)  {

        // setup default language
        if (lang == null || lang.equals("")) {
            lang = LANGS[0];
        }

        String className = langToClassName.getProperty(lang);
        if (className == null || className.equals("")) {
            throw new NoWriterForLangException( lang );
        }
        try {
            return (RDFWriter) Class.forName(className).newInstance();
        } catch (Exception e) {
            if ( e instanceof JenaException )
                throw (JenaException)e ;
            throw new JenaException(e);
        }
    }

    @Override
    public String setWriterClassName(String lang, String className) {
    	return setBaseWriterClassName(lang, className);
    }

    public static String setBaseWriterClassName(String lang, String className) {
        String oldClassName = langToClassName.getProperty(lang);
        langToClassName.setProperty(lang, className);
        return oldClassName;
    }

	@Override
	public void resetRDFWriterF() {
		reset();
	}

	@Override
	public String removeWriter(String lang) throws IllegalArgumentException {
		return remove(lang);
	}

}
