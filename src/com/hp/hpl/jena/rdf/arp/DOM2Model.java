/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Transform DOM nodes of RDF.XML into Jena Models. Known not to work with Java
 * 1.4.1.
 * 
 * @author Jeremy J. Carroll
 * 
 */
public class DOM2Model extends SAX2Model {
	
	 static Logger logger = LoggerFactory.getLogger(DOM2Model.class) ;
	   
    /**
     * Create a new DOM2Model.
     * 
     * @param base
     *            The retrieval URL, or the base URI to be used while parsing.
     * @param m
     *            A Jena Model in which to put the triples, this can be null. If
     *            it is null, then use {@link SAX2RDF#getHandlers}or
     *            {@link SAX2RDF#setHandlersWith}to provide a {@link StatementHandler},
     *            and usually an {@link org.xml.sax.ErrorHandler}
     * @throws SAXParseException 
     */    
    static public DOM2Model createD2M(String base, Model m) throws  SAXParseException {
        return new DOM2Model(base,  m, "", true) ;
    }
    /**
     * Create a new DOM2Model. This is particularly intended for when parsing a
     * non-root element within an XML document. In which case the application
     * needs to find this value in the outer context. Optionally, namespace
     * prefixes can be passed from the outer context using
     * {@link SAX2RDF#startPrefixMapping}.
     * 
     * @param base
     *            The retrieval URL, or the base URI to be used while parsing.
     * @param m
     *            A Jena Model in which to put the triples, this can be null. If
     *            it is null, then use {@link SAX2RDF#getHandlers}or
     *            {@link SAX2RDF#setHandlersWith}to provide a {@link StatementHandler},
     *            and usually an {@link org.xml.sax.ErrorHandler}
     * @param lang
     *            The current value of <code>xml:lang</code> when parsing
     *            starts, usually "".
     * @throws SAXParseException 
     */
    static public DOM2Model createD2M(String base, Model m, String lang) throws  SAXParseException {
        return new DOM2Model(base,  m, lang, true) ;
    }

    DOM2Model(String base, Model m, String lang, boolean dummy)
            throws  SAXParseException {
        super(base, m, lang);
    }

    /**
     * Parse a DOM Node with the RDF/XML parser, loading the triples into the
     * associated Model. Known not to work with Java 1.4.1.
     * 
     * @param document
     */
    public void load(Node document) {
        Source input = new DOMSource(document);

        // Make a SAXResult object using this handler
        SAXResult output = new SAXResult(this);
        output.setLexicalHandler(this);

        // Run transform
        TransformerFactory xformFactory = TransformerFactory.newInstance();
        try {
            Transformer idTransform = xformFactory.newTransformer();
            idTransform.transform(input, output);
        }
        catch (FatalParsingErrorException e) {
            // Old code ignored this,
        	// given difficult bug report, don't be silent.
        	logger.error("Unexpected exception in DOM2Model", e) ;
            
        } 
        catch (RuntimeException rte) {
            throw rte;
        } catch (Exception nrte) {
            throw new JenaException(nrte);
        } finally {
            close();
        }
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
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

