/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *  [See end of file]
 */

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;


//=======================================================================
// Imports


//=======================================================================
/**
* Interface for database identifiers.
* Now only used for used for Graph IDs and long literals. 
* 
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
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
    // OLD
    @Override
    public Object _getID() {
        return m_dbid;
    }

    /** get the identifier as a plain int */
    @Override
    public int getIntID() {
        return m_dbid.intValue();
    }
    
    /** Hash is based on the underlying object */
    @Override
    public int hashCode() {
        return m_dbid.hashCode();
    }

    /** Equality is based on the underlying object */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DBIDInt) {
            return getIntID() == ((DBIDInt)obj).getIntID();
        } else {
            return false;
        }
    }
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

