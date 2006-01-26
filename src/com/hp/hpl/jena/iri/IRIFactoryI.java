/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

/**
 * This interface is used for
 * making new {@link IRI} objects.
 * It is used for making IRIs in two ways:
 * <ol>
 * <li>Without
 * resolving against a base (by the class {@link IRIFactory}). 
 * <li>By resolving against a base (by the interface
 * {@link IRI}).
 * </ol>
 * Which properties of the IRIs result in errors or
 * warnings is determined by the 
 * current settings of the underlying {@link IRIFactory},
 * which is the factory object being used in the first
 * case, or the factory object used to create the base
 * IRI in the second case.
 * @author Jeremy J. Carroll
 *
 */
public interface IRIFactoryI {

    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * This method both allows IRI resolution
     * against a base, and for creating a new
     * IRI using a different factory, 
     * with different conformance settings,
     * implementing a different URI or IRI standard,
     * or variant thereof.
     * @param i The IRI to use.
     * @return A new IRI object.
     * @throws IRIException If a violation of
     * the standards being enforced by the factory 
     * has been detected, and this violation is 
     * classified by the factory as an error.
     */
    IRI construct(IRI i) throws IRIException;
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * @param s The IRI to use.
     * @return A new IRI object.
     * @throws IRIException If a violation of
     * the standards being enforced by the factory 
     * has been detected, and this violation is 
     * classified by the factory as an error.
     */
    IRI construct(String s) throws IRIException;
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * This method both allows IRI resolution
     * against a base, and for creating a new
     * IRI using a different factory, with different
     *  conformance settings,
     * implementing a different URI or IRI standard,
     * or variant thereof.
     *  This method does not throw exceptions, but
     *  records all errors and warnings found
     *  to be queried later using {@link IRI#hasViolation(boolean)}
     *  and {@link IRI#violations(boolean)}.
     * @param i The IRI to use.
     * @return A new IRI object.
     * 
     */
    IRI create(IRI i);
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     *  This method does not throw exceptions, but
     *  records all errors and warnings found
     *  to be queried later using {@link IRI#hasViolation(boolean)}
     *  and {@link IRI#violations(boolean)}.
     * @param s The IRI to use.
     * @return A new IRI object.
     * 
     */
    IRI create(String s);

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
 
