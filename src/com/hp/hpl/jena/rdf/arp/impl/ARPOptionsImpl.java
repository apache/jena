/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import com.hp.hpl.jena.rdf.arp.ARP;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.ARPOptions;
import com.hp.hpl.jena.rdf.arp.SAX2Model;
import com.hp.hpl.jena.rdf.arp.SAX2RDF;


/**
 * Configure error reporting options etc on
 * {@link ARP}, {@link SAX2RDF} and {@link SAX2Model}
 * instances.
 * @author Jeremy J. Carroll
 *
 */
public class ARPOptionsImpl extends  ARPOptions implements ARPErrorNumbers {
	private static int defaultErrorMode[] = new int[400];
	private boolean embedding = false;
    
    
    /**
     * Copies this object.
     * @return A copy.
     */
	public ARPOptionsImpl copy() {
		ARPOptionsImpl rslt = new ARPOptionsImpl();
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
	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setErrorMode(int, int)
     */
	public int setErrorMode(int errno, int mode) {
		int old = errorMode[errno];
		errorMode[errno] = mode;
		return old;
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setDefaultErrorMode()
     */
	public void setDefaultErrorMode() {
		errorMode = (int[]) defaultErrorMode.clone();
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setLaxErrorMode()
     */
	public void setLaxErrorMode() {
		setDefaultErrorMode();
		for (int i = 100; i < 200; i++)
			setErrorMode(i, EM_IGNORE);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, EM_WARNING);
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setStrictErrorMode()
     */
	public void setStrictErrorMode() {
	    setStrictErrorMode(EM_IGNORE);
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setStrictErrorMode(int)
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
        setErrorMode(WARN_NOT_RDF_NAMESPACE,nonErrorMode);
		// setErrorMode(WARN_QNAME_AS_ID, error);
		//      setErrorMode(WARN_BAD_XML, error);
		setErrorMode(WARN_SAX_WARNING, warning);
		setErrorMode(IGN_DAML_COLLECTION, error);
	}

	/* (non-Javadoc)
     * @see com.hp.hpl.jena.rdf.arp.ARPOptionsI#setEmbedding(boolean)
     */
	public boolean setEmbedding(boolean embed) {
		boolean old = embedding;
		embedding = embed;
		return old;
	}
	public boolean getEmbedding() {
		return embedding;
	}

}


/*
 *  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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
 
