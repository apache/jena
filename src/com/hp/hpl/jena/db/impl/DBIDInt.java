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
* This variant just uses integers allocated by the database driver as indices.
* This would be sufficient for databases up to 4x10^9 statements.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2003-04-25 02:57:17 $
*/

public class DBIDInt implements IDBID {

    /** The index */
    protected Integer m_dbid;

    /** constructor */
    public DBIDInt(int id) {
        m_dbid = new Integer(id);
    }

    /** constructor */
    public DBIDInt(Integer id) {
        m_dbid = id;
    }

    /** get the identifier as an Integer, fits calling signature of our generic sql interface. */
    public Object getID() {
        return m_dbid;
    }

    /** get the identifier as a plain int */
    public int getIntID() {
        return m_dbid.intValue();
    }

    /** Hash is based on the underlying object */
    public int hashCode() {
        return m_dbid.hashCode();
    }

    /** Equality is based on the underlying object */
    public boolean equals(Object obj) {
        if (obj instanceof DBIDInt) {
            return getIntID() == ((DBIDInt)obj).getIntID();
        } else {
            return false;
        }
    }
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

