/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import org.xml.sax.ErrorHandler;

/**
 * Configure error reporting options etc on
 * {@link ARP}, {@link SAX2RDF} and {@link SAX2Model}
 * instances.
 * @author Jeremy J. Carroll
 *
 */
public class ARPOptions  implements ARPErrorNumbers {
	private static int defaultErrorMode[] = new int[400];
	private boolean embedding = false;

	ARPOptions copy() {
		ARPOptions rslt = new ARPOptions();
		rslt.errorMode = (int[])errorMode.clone() ;
		rslt.embedding = embedding;
		return rslt;
	}
	static {
		for (int i = 0; i < defaultErrorMode.length; i++)
			defaultErrorMode[i] = i / 100;
	}
	private int errorMode[] = (int[]) defaultErrorMode.clone();

    int getErrorMode()[] {
    		return errorMode;
    }
	/** Sets or gets the error handling mode for a specific error condition.
	 * Changes that cannot be honoured are silently ignored.
	 * Illegal error numbers may result in an ArrayIndexOutOfBoundsException but
	 * are usually ignored.
	 * @param errno The specific error condition to change.
	 * @param mode The new mode one of:
	 * <dl>
	 * <dt>IGNORE</dt>
	 * <dd>Ignore this condition.</dd>
	 * <dt>WARNING</dt>
	 * <dt>Invoke ErrorHandler.warning() for this condition.</dd>
	 * <dt>ERROR</dt>
	 * <dt>Invoke ErrorHandler.error() for this condition.</dd>
	 * <dt>FATAL</dt>
	 * <dt>Aborts parse and invokes ErrorHandler.fatalError() for this condition.
	 * In unusual situations, a few further warnings and errors may be reported.
	 * </dd>
	 * </dl>
	 * @return The old error mode for this condition.
	 */
	public int setErrorMode(int errno, int mode) {
		int old = errorMode[errno];
		switch (mode) {
			case EM_WARNING :
			case EM_IGNORE :
				if (errno >= 100 * EM_ERROR && errno != ERR_NOT_WHITESPACE)
					break;
			case EM_ERROR :
			case EM_FATAL :
				switch (errno) {
					case ERR_UNABLE_TO_RECOVER :
						break;
					default :
						errorMode[errno] = mode;
				}
		}
		return old;
	}

	/** Resets error mode to the default values:
	 * most errors are reported as warnings, but triples are produced.
	 */
	public void setDefaultErrorMode() {
		errorMode = (int[]) defaultErrorMode.clone();
	}

	/** As many errors as possible are ignored.
	 * As many triples as possible are produced.
	 */
	public void setLaxErrorMode() {
		setDefaultErrorMode();
		for (int i = 100; i < 200; i++)
			setErrorMode(i, EM_IGNORE);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, EM_WARNING);
	}

	/** This method tries to emulate the latest Working Group recommendations.
	 */
	public void setStrictErrorMode() {
	    setStrictErrorMode(EM_IGNORE);
	}

	/**
	 * This method detects and prohibits errors according to
	 *the latest Working Group recommendations.
	 * For other conditions, such as 
	 {@link ARPErrorNumbers#WARN_PROCESSING_INSTRUCTION_IN_RDF} and
	 {@link ARPErrorNumbers#WARN_LEGAL_REUSE_OF_ID}, nonErrorMode is used. 
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
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, warning);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, warning);
		setErrorMode(WARN_DEPRECATED_XMLLANG, warning);
		setErrorMode(WARN_STRING_NOT_NORMAL_FORM_C, warning);
		//       setErrorMode(WARN_EMPTY_ABOUT_EACH,nonErrorMode);
		setErrorMode(WARN_UNKNOWN_PARSETYPE, warning);
		//     setErrorMode(WARN_BAD_XML, nonErrorMode);
		setErrorMode(WARN_PROCESSING_INSTRUCTION_IN_RDF, nonErrorMode);
//		setErrorMode(WARN_LEGAL_REUSE_OF_ID, nonErrorMode);
		setErrorMode(WARN_RDF_NN_AS_TYPE, nonErrorMode);
		setErrorMode(WARN_UNKNOWN_RDF_ELEMENT, warning);
		setErrorMode(WARN_UNKNOWN_RDF_ATTRIBUTE, warning);
		setErrorMode(WARN_UNQUALIFIED_RDF_ATTRIBUTE, warning);
		setErrorMode(WARN_UNKNOWN_XML_ATTRIBUTE, nonErrorMode);
		// setErrorMode(WARN_QNAME_AS_ID, error);
		//      setErrorMode(WARN_BAD_XML, error);
		setErrorMode(WARN_SAX_WARNING, warning);
		setErrorMode(IGN_DAML_COLLECTION, error);
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
	boolean getEmbedding() {
		return embedding;
	}

}


/*
 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
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
 
