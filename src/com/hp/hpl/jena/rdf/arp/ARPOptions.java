/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

/**
 * The interface to set the various options on ARP.
 * User defined implementations of this interface are
 * not supported.
 * This is an abstract class rather than an interface
 * to have better backward compatibilitiy with earlier
 * versions.
 * @author Jeremy J. Carroll
 *
 */
abstract public class ARPOptions {

    /** Sets or gets the error handling mode for a specific error condition.
     * Changes that cannot be honoured are silently ignored.
     * Illegal error numbers may result in an ArrayIndexOutOfBoundsException but
     * are usually ignored.
     * Most conditions are associated with one or more specific resources or literals
     * formed during the parse. 
     * The precise definition of 'associated with' is deliberately 
     * undefined, and may change in future releases.
     * Depending on the error mode associated with
     * the error triples involving those resources or literals may or may not
     * be produced.
     * 
     * When the condition is a violation of the RDF/XML Syntax (Revised) Recommendations, 
     * and the error mode is {@link ARPErrorNumbers#EM_IGNORE} or  {@link ARPErrorNumbers#EM_WARNING},
     * the precise rules which ARP uses to generate triples for such ill-formed input are 
     * not defined by any standard and are subject to change with future releases.
     * For input involving no errors, ARP creates triples in accordance with 
     * the RDF/XML Syntax Revised Recommendation. 
     * 
     * The mode can have one of the following four values.
     * 
     * <dl>
     * <dt>{@link ARPErrorNumbers#EM_IGNORE}</dt>
     * <dd>Ignore this condition. Produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_WARNING}</dt>
     * <dt>Invoke ErrorHandler.warning() for this condition. Produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_ERROR}</dt>
     * <dt>Invoke ErrorHandler.error() for this condition. Do not produce triples.</dd>
     * <dt>{@link ARPErrorNumbers#EM_FATAL}</dt>
     * <dt>Aborts parse and invokes ErrorHandler.fatalError() for this condition.
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
    abstract public int setErrorMode(int errno, int mode);

    /** Resets error mode to the default values:
     * many errors are reported as warnings, and resulting triples are produced.
     */
    abstract public void setDefaultErrorMode();

    /** As many errors as possible are ignored.
     * As many triples as possible are produced.
     */
    abstract public void setLaxErrorMode();

    /** This sets strict conformance to the W3C Recommendations.
     */
    abstract public void setStrictErrorMode();

    /**
     * This method detects and prohibits errors according to
     *the W3C Recommendations.
     * For other conditions, such as 
     {@link ARPErrorNumbers#WARN_PROCESSING_INSTRUCTION_IN_RDF}, nonErrorMode is used. 
     *@param nonErrorMode The way of treating non-error conditions.
     */
    abstract public void setStrictErrorMode(int nonErrorMode);

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
    abstract public boolean setEmbedding(boolean embed);

}

/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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

