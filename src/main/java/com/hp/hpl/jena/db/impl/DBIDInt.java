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
