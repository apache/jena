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
import com.hp.hpl.jena.shared.*;
import java.util.Properties;
import com.hp.hpl.jena.JenaRuntime ;

/**
 *
 * @author  bwm
 * @version $Revision: 1.1 $ $Date: 2009-06-29 08:55:32 $
 */
public class RDFReaderFImpl extends Object implements RDFReaderF {

    private static final String GRDDLREADER = "com.hp.hpl.jena.grddl.GRDDLReader";
    private static final String TURTLEREADER = "com.hp.hpl.jena.n3.turtle.TurtleReader" ;
    
    // Old reader (N3 based)
    //private static final String TURTLEREADER = "com.hp.hpl.jena.n3.N3TurtleJenaReader" ;

	protected static Properties langToClassName = null;

    // predefined languages - these should probably go in a properties file

    protected static final String LANGS[] = { "RDF/XML",
                                              "RDF/XML-ABBREV",
                                              "N-TRIPLE",
                                              "N-TRIPLES",
                                              "N3",
                                              "TURTLE",
                                              "Turtle",
                                              "TTL",
                                              "GRDDL"};
    // default readers for each language

    protected static final String DEFAULTREADERS[] = {
        "com.hp.hpl.jena.rdf.arp.JenaReader",
        "com.hp.hpl.jena.rdf.arp.JenaReader",
        Jena.PATH + ".rdf.model.impl.NTripleReader",
        Jena.PATH + ".rdf.model.impl.NTripleReader",
        TURTLEREADER, //com.hp.hpl.jena.n3.N3JenaReader.class.getName(),  // N3 replaced by a Turtle-based parser 
        TURTLEREADER,
        TURTLEREADER,
        TURTLEREADER,
        GRDDLREADER
    };

    protected static final String DEFAULTLANG = LANGS[0];

    protected static final String PROPNAMEBASE = Jena.PATH + ".reader.";

    static { // static initializer - set default readers
        langToClassName = new Properties();
        for (int i = 0; i<LANGS.length; i++) {
            langToClassName.setProperty(
                               LANGS[i],
                               JenaRuntime.getSystemProperty(PROPNAMEBASE + LANGS[i],
                                                  DEFAULTREADERS[i]));
        }
    }


    /** Creates new RDFReaderFImpl */
    public  RDFReaderFImpl() {
    }

    @Override
    public RDFReader getReader()  {
        return getReader(DEFAULTLANG);
    }

    @Override
    public RDFReader getReader(String lang)  {

        // setup default language
        if (lang==null || lang.equals("")) {
            lang = LANGS[0];
        }

        String className = langToClassName.getProperty(lang);
        if (className == null || className.equals("")) {
            throw new NoReaderForLangException( lang );
        }
        try {
          return (RDFReader) Class.forName(className)
                                  .newInstance();
        } catch (ClassNotFoundException e) {
        	if (className.equals(GRDDLREADER))
                throw new ConfigException("The GRDDL reader must be downloaded separately from Sourceforge, and included on the classpath.",e);
        	throw new ConfigException("Reader not found on classpath",e);
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }


    @Override
    public String setReaderClassName( String lang,String className ) {
        return setBaseReaderClassName( lang, className );
    }
    
    public static String setBaseReaderClassName( String lang, String className ) {
        String oldClassName = langToClassName.getProperty(lang);
        langToClassName.setProperty(lang, className);
        return oldClassName;
    }
}
