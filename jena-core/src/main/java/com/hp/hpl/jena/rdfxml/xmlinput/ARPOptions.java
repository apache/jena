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

import org.apache.jena.iri.IRIFactory;


/**
 * The interface to set the various options on ARP.
 *  User defined
 * implementations of this interface are not supported. This is a class rather
 * than an interface to have better backward compatibilitiy with earlier
 * versions, however constructing instances of this class is deprecated.
 * In addition, accessing the fields of {@link ARPErrorNumbers} through this
 * class is not supported. The inheritance of this interface will be removed.
 */
public class ARPOptions implements ARPErrorNumbers {
    
    /**
     * Do not use this constructor.
     * An example of not using this constructor is as follows.
     * <br/>
     * Deprecated usage:
     * <br/>
     * <pre>
        ARP arp = new ARP();
        ARPOptions options = new ARPOptions();
     </pre>
     <br/>
     * Preferred code:
     * <br/>
     * <pre>
        ARP arp = new ARP();
        ARPOptions options = arp.getOptions();
     </pre>
     */
    private ARPOptions() {
        //*@ deprecated Use {@link ARPConfig#getOptions()}
        
    }
    
    /** Internal use only */
    public static ARPOptions createNewOptions() { return new ARPOptions() ; }
    
    private static int defaultErrorMode[] = new int[400];
    static {
        for (int i = 0; i < defaultErrorMode.length; i++)
            defaultErrorMode[i] = i / 100;
        
    }
    private boolean embedding = false;
    private int errorMode[] = defaultErrorMode.clone();
    
    private static IRIFactory defaultIriFactory = IRIFactory.jenaImplementation() ;
    private IRIFactory iriFactory = defaultIriFactory ;

    /** Sets or gets the error handling mode for a specific error condition.
     * Changes that cannot be honoured are silently ignored.
     * Illegal error numbers may result in an ArrayIndexOutOfBoundsException but
     * are usually ignored.
     * Most conditions are associated with one or more specific resources or literals
     * formed during the parse. 
     * Triples involving resource or literal associated with an error condition 
     * are not produced.
     * The precise definition of 'associated with' is deliberately 
     * undefined, and may change in future releases.
     * This method can be used to downgrade an error condition to 
     * a warning, or to upgrade a warning to an error.
     * Such a change modifies which triples are produced.
     * <p>
     * 
     * When the condition is a violation of the RDF/XML Syntax (Revised) Recommendations, 
     * and the error mode is {@link ARPErrorNumbers#EM_IGNORE} or  {@link ARPErrorNumbers#EM_WARNING},
     * the precise rules which ARP uses to generate triples for such ill-formed input are 
     * not defined by any standard and are subject to change with future releases.
     * For input involving no errors, ARP creates triples in accordance with 
     * the RDF/XML Syntax Revised Recommendation. 
     * <p>
     * 
     * The mode can have one of the following four values.
     * 
     * <dl>
     * <dt>{@link ARPErrorNumbers#EM_IGNORE}</dt>
     * <dd>Ignore this condition. Produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_WARNING}</dt>
     * <dd>Invoke ErrorHandler.warning() for this condition. Produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_ERROR}</dt>
     * <dd>Invoke ErrorHandler.error() for this condition. Do not produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_FATAL}</dt>
     * <dd>Aborts parse and invokes ErrorHandler.errorError() for this condition.
     * Do not produce triples.
     * In unusual situations, a few further warnings and errors may be reported.
     * </dd>
     * </dl>
     * 
     * 
     * @param errno The specific error condition to change.
     * @param mode The new mode for this condition.
     * @return The old error mode for this condition.
     */
    public int setErrorMode(int errno, int mode) {
        int old = errorMode[errno];
        errorMode[errno] = mode;
        return old;
    }

    /** Resets error mode to the default values:
     * many errors are reported as warnings, and resulting triples are produced.
     */
    public void setDefaultErrorMode() {
        errorMode = defaultErrorMode.clone();
    }

    /** As many errors as possible are ignored.
     * As many triples as possible are produced.
     */
    public void setLaxErrorMode() {
        setDefaultErrorMode();
        for (int i = 100; i < 200; i++)
            setErrorMode(i, EM_IGNORE);
    }

    /** This sets strict conformance to the W3C Recommendations.
     */
    public void setStrictErrorMode() {
        setStrictErrorMode(EM_IGNORE);
    }

    /**
     * This method detects and prohibits errors according to
     *the W3C Recommendations.
     * For other conditions, such as 
     {@link ARPErrorNumbers#WARN_PROCESSING_INSTRUCTION_IN_RDF}, nonErrorMode is used. 
     *@param nonErrorMode The way of treating non-error conditions.
     */
    public void setStrictErrorMode(int nonErrorMode) {
        setDefaultErrorMode();
        for (int i = 1; i < 100; i++)
            setErrorMode(i, nonErrorMode);
        int warning = EM_WARNING;
        int error = EM_ERROR;
        switch (nonErrorMode) {
            case EM_ERROR :
                warning = EM_ERROR;
                break;
            case EM_FATAL :
                warning = error = EM_FATAL;
                break;
        }
        for (int i = 100; i < 200; i++)
            setErrorMode(i, error);
        // setErrorMode(IGN_XMLBASE_USED,warning);
        // setErrorMode(IGN_XMLBASE_SIGNIFICANT,error);
        setErrorMode(WARN_DEPRECATED_XMLLANG, warning);
        setErrorMode(WARN_STRING_NOT_NORMAL_FORM_C, warning);
        //       setErrorMode(WARN_EMPTY_ABOUT_EACH,nonErrorMode);
        setErrorMode(WARN_UNKNOWN_PARSETYPE, warning);
        //     setErrorMode(WARN_BAD_XML, nonErrorMode);
        setErrorMode(WARN_PROCESSING_INSTRUCTION_IN_RDF, nonErrorMode);
//      setErrorMode(WARN_LEGAL_REUSE_OF_ID, nonErrorMode);
        setErrorMode(WARN_RDF_NN_AS_TYPE, nonErrorMode);
        setErrorMode(WARN_UNKNOWN_RDF_ELEMENT, warning);
        setErrorMode(WARN_UNKNOWN_RDF_ATTRIBUTE, warning);
        setErrorMode(WARN_UNQUALIFIED_RDF_ATTRIBUTE, warning);
        setErrorMode(WARN_UNKNOWN_XML_ATTRIBUTE, nonErrorMode);
        setErrorMode(WARN_NOT_RDF_NAMESPACE,nonErrorMode);
        // setErrorMode(WARN_QNAME_AS_ID, error);
        //      setErrorMode(WARN_BAD_XML, error);
        setErrorMode(WARN_SAX_WARNING, warning);
    }

    /**
     * Internal use only.
     * Copies this object.
     * @return A copy.
     */
    public ARPOptions copy() {
        //* @ deprecated
    	ARPOptions rslt = new ARPOptions();
    	rslt.errorMode = errorMode.clone() ;
    	rslt.embedding = embedding;
    	return rslt;
    }

    /** Sets whether the XML document is only RDF, or contains RDF embedded in other XML.
     * The default is non-embedded mode.
     * Embedded mode also matches RDF documents that use the
     * rdf:RDF tag at the top-level.
     * Non-embeded mode matches RDF documents which omit that optional tag, and consist of a single rdf:Description or
     * typed node.
     * To find embedded RDF it is necessary to setEmbedding(true).
     * @param embed true: Look for embedded RDF; or false: match a typed node or rdf:Description against the whole document (the default).
     * @return Previous setting.
     */

    public boolean setEmbedding(boolean embed) {
        boolean old = embedding;
        embedding = embed;
        return old;
    }

    /**
     * Returns the error mode for the given error code.
     * @param eCode
     * @return One of {@link ARPErrorNumbers#EM_IGNORE},
     * {@link ARPErrorNumbers#EM_WARNING},
     * {@link ARPErrorNumbers#EM_ERROR},
     * {@link ARPErrorNumbers#EM_FATAL}
     */
    public int getErrorMode(int eCode) {
    		return errorMode[eCode];
    }

    /**
     * True if the embedding flag is set.
     * Indicates that the parser should look for rdf:RDF
     * element, rather than treat the whole file as an RDF/XML
     * document (possibly without rdf:RDF element).
     */
    public boolean getEmbedding() {
    	return embedding;
    }
    
    /** Set the IRI factory (and hence the IRI checking rules) */
    public void setIRIFactory(IRIFactory f) { iriFactory = f ; }
    
    /** Get the IRI factory (and hence the IRI checking rules) */
    public IRIFactory getIRIFactory() { return iriFactory ; }
    
    /** Set the system-wide default IRI factory, which incorporates the checking rules.
     * By default, Jena provides checking in compliance with the RDF spec but
     * that is quite loose and allows strings that are not IRIs (the final
     * IRI spec came along after the RDF spec).  Example: spaces are
     * strictly legal in RDF URIReferences but not in IRIs or URIs.
     * Note that options to the RDF/XML parser override this. 
     */ 

    public static void setIRIFactoryGlobal(IRIFactory f) { defaultIriFactory = f ; }  
    
    /** Get the default (global) IRI factory (and hence the IRI checking rules) */
    public static IRIFactory getIRIFactoryGlobal() { return defaultIriFactory ; }
}
