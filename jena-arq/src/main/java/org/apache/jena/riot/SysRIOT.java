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

package org.apache.jena.riot;

import java.io.File ;

import org.apache.jena.riot.system.IRILib ;
import org.apache.jena.riot.system.IRIResolver ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.util.FileUtils ;

public class SysRIOT
{
    public static final String riotLoggerName = "org.apache.jena.riot" ;
    private static Logger riotLogger = LoggerFactory.getLogger(riotLoggerName) ;
    
    public static boolean StrictXSDLexicialForms = false ;
    public static boolean strictMode             = false ;
    
    public static final String BNodeGenIdPrefix = "genid" ;
    
    public static final boolean isWindows = (File.pathSeparatorChar == ';' ) ;
    
    static public String fmtMessage(String message, long line, long col)
    {
        if ( col == -1 && line == -1 )
                return message ;
        if ( col == -1 && line != -1 )
            return String.format("[line: %d] %s", line, message) ;
        if ( col != -1 && line == -1 )
            return String.format("[col: %d] %s", col, message) ;
        // Mild attempt to keep some alignment
        return String.format("[line: %d, col: %-2d] %s", line, col, message) ;
    }

    public static Logger getLogger()
    {
        return riotLogger ;
    }
    
    public static String chooseBaseIRI()
    {
        return IRIResolver.chooseBaseURI().toString() ;
    }
    
    /** Return a URI suitable for a baseURI, based on some input (which may be null) */
    public static String chooseBaseIRI(String baseURI)
    {
      String scheme = FileUtils.getScheme(baseURI) ;
      // Assume scheme of one letter are Windows drive letters. 
      if ( scheme != null && scheme.length() == 1 ) 
          scheme = "file" ;
      if ( scheme != null && scheme.equals("file") )
          return IRILib.filenameToIRI(baseURI) ;
      return IRIResolver.resolveString(baseURI) ;
    }

    public static String filename2baseIRI(String filename)
    {
        if ( filename == null || filename.equals("-") )
            return "http://localhost/stdin/" ;
        String x = IRILib.filenameToIRI(filename) ;
        return x ;
    }

    /** Choose base IRI, from a given one and a filename.
     *  Prefer the given base ; turn any filename into an IRI.   
     */
    public static String chooseBaseIRI(String baseIRI, String filename)
    {
        if ( baseIRI != null )
            return baseIRI ;
        return filename2baseIRI(filename) ;
    }
}
