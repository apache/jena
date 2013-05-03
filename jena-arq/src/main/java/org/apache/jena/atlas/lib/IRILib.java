/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE filoved to riot.syae
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

package org.apache.jena.atlas.lib;

import java.io.File ;

/** @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib} */
@Deprecated
public class IRILib
{
    /** Encode using the rules for a component (e.g. ':' and '/' get encoded) 
     * Does not encode non-ASCII characters 
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String encodeUriComponent(String string) { 
        return org.apache.jena.riot.system.IRILib.encodeUriComponent(string) ;
    }
    
    /** Encode using the rules for a file: URL.  Same as encodeUriPath except
     * add "~" to the encoded set.
     *  Does not encode non-ASCII characters
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String encodeFileURL(String string)
    {
        return org.apache.jena.riot.system.IRILib.encodeFileURL(string) ;
    }
    
    /** Encode using the rules for a path (e.g. ':' and '/' do not get encoded)
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String encodeUriPath(String uri)
    {
        return org.apache.jena.riot.system.IRILib.encodeUriPath(uri) ;
    }
    
    /** @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib} */
    @Deprecated
    public static String decode(String string)
    {
        return org.apache.jena.riot.system.IRILib.decode(string) ;
    }
    
    /** Return a string that is an IRI for the filename.
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String fileToIRI(File f)
    {
        return org.apache.jena.riot.system.IRILib.fileToIRI(f) ;
    }
    
    /** Create a string that is a IRI for the filename.
     *  The file name may already have file:.
     *  The file name may be relative. 
     *  Encode using the rules for a path (e.g. ':' and'/' do not get encoded)
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String filenameToIRI(String fn)
    {
        return org.apache.jena.riot.system.IRILib.filenameToIRI(fn) ;

    }
    
    /** Convert an IRI to a filename
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String IRIToFilename(String iri)
    {
        return org.apache.jena.riot.system.IRILib.IRIToFilename(iri) ;
    }
    
    /**
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static String encodeNonASCII(String string)
    {
        return org.apache.jena.riot.system.IRILib.encodeNonASCII(string) ;
    }

    /**
     * @deprecated Moved to {@linkplain org.apache.jena.riot.system.IRILib}
     */
    @Deprecated
    public static boolean containsNonASCII(String string)
    {
        return org.apache.jena.riot.system.IRILib.containsNonASCII(string) ;
    } 
}
