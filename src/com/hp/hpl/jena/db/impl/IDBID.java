/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 *
 */


//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports


//=======================================================================
/**
* Interface for database identifiers.
* Most RDF entities (resources, literals, statements) have an associated
* database index. These are cached using RDB-specific variants of the jena
* "impl" classes. This can avoid some redundant database lookup.
* <p>
* In simple implementations the database identifier is simply an integer
* given a database row index allocated by some generator. In other implementations
* we use a "unique" hash of the resource or literal data itself. This interface
* encapsulates the different ID representations.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2003-04-25 02:57:17 $
*/

public interface IDBID {

    /** Return identifier as a generic object which can be supplied to the sql index/store statement */
    public Object getID();

}


/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 */

