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

package org.apache.jena.atlas.lib;

/** Operations in someway related to XML */
public class XMLLib
{
    /** Trim the XML whitespace characters strictly needed for whitespace facet collapse.
     * This <b>not</b> full whitespace facet collapse, which also requires processing of
     * internal spaces.  Because none of the datatypes that have whitespace facet
     * collapse and have values we extract can legally contain internal whitespace,
     * we just need to trim the string.
     * 
     *  Java String.trim removes any characters less than 0x20. 
     */
    static String WScollapse(String string)
    {
        int len = string.length();
        if ( len == 0 )
            return string ;
        
        if ( (string.charAt(0) > 0x20) && (string.charAt(len-1) > 0x20) )
            return string ;

        int idx1 = 0 ;
        for ( ; idx1 < len ; idx1++ )
        {
            char ch = string.charAt(idx1) ;
            if ( ! testForWS(ch) )
                break ;
        }
        int idx2 = len-1 ;
        for ( ; idx2 > idx1 ; idx2-- )
        {
            char ch = string.charAt(idx2) ;
            if ( ! testForWS(ch) )
                break ;
        }
        return string.substring(idx1, idx2+1) ;
    }

    private static boolean testForWS(char ch)
    {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' ; 
    }
}
