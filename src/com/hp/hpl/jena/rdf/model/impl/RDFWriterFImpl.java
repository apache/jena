/*
 *  (c) Copyright 2000, 2001 Hewlett-Packard Development Company, LP
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
 * $Id: RDFWriterFImpl.java,v 1.9 2004-07-06 13:36:59 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Properties;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.n3.N3JenaWriter;
import com.hp.hpl.jena.JenaRuntime ;


/**
 *
 * @author  bwm
 * @version $Revision: 1.9 $ $Date: 2004-07-06 13:36:59 $
 */
public class RDFWriterFImpl extends Object implements RDFWriterF {

    protected static Properties langToClassName = null;

    // predefined languages - these should probably go in a properties file

    protected static final String LANGS[] =
        { "RDF/XML",
          "RDF/XML-ABBREV",
          "N-TRIPLE",
          "N-TRIPLES",
          "N3",
          N3JenaWriter.n3WriterPrettyPrinter,
          N3JenaWriter.n3WriterPlain,
          N3JenaWriter.n3WriterTriples,
          N3JenaWriter.n3WriterTriplesAlt,
        };
    // default readers for each language

    protected static final String DEFAULTWRITERS[] =
        {
            Jena.PATH + ".xmloutput.impl.Basic",
            Jena.PATH + ".xmloutput.impl.Abbreviated",
            Jena.PATH + ".rdf.model.impl.NTripleWriter",
            Jena.PATH + ".rdf.model.impl.NTripleWriter",
            Jena.PATH + ".n3.N3JenaWriter",
            Jena.PATH + ".n3.N3JenaWriterPP",
            Jena.PATH + ".n3.N3JenaWriterPlain",
            Jena.PATH + ".n3.N3JenaWriterTriples",
            Jena.PATH + ".n3.N3JenaWriterTriples",  // Same writer, different writer name
             };

    protected static final String DEFAULTLANG = LANGS[0];

    protected static final String PROPNAMEBASE = Jena.PATH + ".writer.";

    static { // static initializer - set default readers
        langToClassName = new Properties();
        for (int i = 0; i < LANGS.length; i++) {
            langToClassName.setProperty(
                LANGS[i],
                JenaRuntime.getSystemProperty(PROPNAMEBASE + LANGS[i], DEFAULTWRITERS[i]));
        }
    }

    /** Creates new RDFReaderFImpl */
    public RDFWriterFImpl() {
    }

    public RDFWriter getWriter()  {
        return getWriter(DEFAULTLANG);
    }

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
            throw new JenaException(e);
        }
    }

    public String setWriterClassName(String lang, String className) {
        String oldClassName = langToClassName.getProperty(lang);
        langToClassName.setProperty(lang, className);
        return oldClassName;
    }
}
