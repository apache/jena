/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.util.ArrayList;
import java.util.List;

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
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public class ResultSetStringIterator extends ResultSetIterator<List<String>> {

    /**
     * Extract the current row - turn into strings.
     * Override in subclasses.
     */
    @Override
    protected void extractRow() throws Exception {
        if (m_row == null) {
            m_nCols = m_resultSet.getMetaData().getColumnCount();
            m_row = new ArrayList<String>(m_nCols);
            for (int i = 0; i < m_nCols; i++) m_row.add(null);
        }
        for (int i = 0; i < m_nCols; i++) {
            m_row.set(i, m_resultSet.getString(i+1));
        }
    }

 }
