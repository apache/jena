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

package org.apache.jena.iri;

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
