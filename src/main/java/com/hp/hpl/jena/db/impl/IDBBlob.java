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

import java.sql.Blob;
import java.sql.SQLException;

//=======================================================================
// Imports


//=======================================================================
/**
* Interface for database blob objects.
* 
* @author <a href="mailto:harumi.kuno@hp.com">Harumi Kuno</a>
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public interface IDBBlob {
	
	/**
	 * Put data to the BLOB at the requested position. 
	 * @return int the number of bytes actually written
	 */
	public int putBytes(long pos, byte bytes[]) throws SQLException;
								 
    /** Return as blob  */
    public Blob getBlob();
}
