/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 *
 */

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.sql.*;
import java.util.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.shared.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//=======================================================================
/**
* Iterates over an SQL result set returning each row as an ArrayList of
* objects. The returned array is shared at each iteration so calling next() or even hasNext()
* changes the array contents. When the iterator terminates the resources
* are cleaned up and the underlying SQL PreparedStatement is returned to
* the SQLCache pool from whence it came.
* 
* <p>Override the extractRow, getRow, and remove methods in subclasses 
* to return an object collection derived from the row contents instead 
* of the raw row contents.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.3 $ on $Date: 2007-01-02 11:50:42 $
*/

public class ResultSetNodeIterator extends ResultSetIterator {

    /**
     * Extract the current row
     * Override in subclasses.
     */
    protected void extractRow() throws Exception {
        if (m_row == null) {
            m_nCols = m_resultSet.getMetaData().getColumnCount();
            m_row = new ArrayList(m_nCols);
            for (int i = 0; i < m_nCols; i++) m_row.add(null);
        }
        for (int i = 0; i < m_nCols; i++) {
            m_row.set(i, m_resultSet.getString(i+1));
        }
    }

 } // End class

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
