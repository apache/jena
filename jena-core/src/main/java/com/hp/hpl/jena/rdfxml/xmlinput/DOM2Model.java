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

package com.hp.hpl.jena.rdfxml.xmlinput;

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
