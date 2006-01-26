/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

/**
 * This interface provides constants
 * used as the return value of
 * {@link Violation#getComponent()}.
 * Each identifies a component
 * of an IRI. 
 * The values of these constants
 * will change with future releases,
 * since they integrate tightly with
 * implementation details.
 * @author Jeremy J. Carroll
 *
 */
public interface IRIComponents {
    /**
     * Indicates the scheme component.
     */
    static final int SCHEME = 2;
    /**
     * Indicates the authority component.
     */
    static final int AUTHORITY = 4;
    /**
     * Indicates the user information part of the authority component,
     * including the password if any.
     */
    static final int USER = 6;
    /**
     * Indicates the host  part of the authority  component.
     */
    static final int HOST = 7;
    /**
     * Indicates the port  part of the authority  component.
     */
    static final int PORT = 10;
    /**
     * Indicates the path component.
     */
   static final int PATH = 11;
   /**
    * Indicates the query component.
    */
   static final int QUERY = 13;
   /**
    * Indicates the fragment component.
    */
    static final int FRAGMENT = 15;
    
    /**
     * Indicates the PATH and QUERY components combined,
     * for schemes in which ? is not special (e.g. ftp and file)
     */
    static final int PATHQUERY = 31;  
    // 31 is big enough hopefully to not interfere with pattern, and small enough for int bit mask
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
 
