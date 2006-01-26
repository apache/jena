/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

/**
 * Information concerning a
 * violation of some specification concerning IRIs.
 * This may be wrapped in 
 * an {@link IRIException} and thrown, 
 * or may be returned by
 * {@link IRI#violations(boolean)}. Which conditions
 * result in errors and warnings
 * depends on the setting of the related
 * {@link IRIFactory}.
 * @author Jeremy J. Carroll
 *
 */
public abstract class  Violation implements ViolationCodes, IRIComponents {


    // TODO e-mail about dot-segments
    
    // TODO single script in a component
    /**
     * The value from {@link ViolationCodes}
     * corresponding to this condition.
     * @return An error code.
     */
    public abstract int getViolationCode();
    /**
     * The IRI that triggered this condition.
     * If an IRI has been constructed by
     * resolving a relative reference
     * against a base IRI then exceptions
     * associated with that IRI will have the
     * most informative value here, which can
     * be any of the three IRIs involved (the base IRI, the
     * relative IRI or the resolved IRI).
     * @return The IRI that triggered the error.
     */
    public abstract IRI getIRI();
    /**
     * A value from {@link IRIComponents}
     * indicating which component of  the IRI
     * is involved with this error.
     * @return A code indicating the IRI component in which the error occurred.
     */
    abstract public int getComponent();
    /**
     * A string version of the code number,
     * corresponding to the name of the java identifier.
     * @return The name of the java identifier of the error code for this error.
     */
    abstract public String codeName();
    /**
     * A short description of the error condition.
     * (Short is in comparison with {@link #getLongMessage()},
     * not an absolute value).
     * @return The error message.
     */
    abstract public String getShortMessage();
    
    /**
     * A long description of the error condition,
     * typically including the 
     * @return The error message.
     */
    abstract public String getLongMessage();
    
    
    /**
     * The URL of the 
     * section of the specification which has been violated.
     * @return The error message.
     */
    abstract public String getSpecificationURL();
    
    /**
     * Using the settings of the factory associated
     * with the IRI associated with this violation,
     * is this condition intended as an error (or as
     * a warning)?
     * @return true if this condition is an error, false if it is a warning.
     */
    public abstract boolean isError();
    
   
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
 
