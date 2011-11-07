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

package com.hp.hpl.jena.db;

import com.hp.hpl.jena.shared.*;

/**
* Used to signal most errors with RDB access. Updated by kers to
* switch to using JenaException not the (now deprecated) RDFException.
*
* Deprecated for use outside the RDB codebase.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @author Chris Dollin <a href="mailto:chris.dollin@hp.com">email</a>
*/

public class RDFRDBException extends JenaException {

    /** Construct an exception with given error message */
    public RDFRDBException( String message ) {
        super( message );
    }

    /** Construct an exception with given error message */
    public RDFRDBException(String message, Exception e) {
        super( message, e ); 
    }

	
}
