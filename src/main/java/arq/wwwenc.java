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

package arq;

import org.apache.jena.atlas.lib.StrUtils ;

public class wwwenc
{
    /* http://en.wikipedia.org/wiki/Percent-encoding
     * Reserved characters after percent-encoding 
     *   !    *   "   '   (   )   ;   :   @   &   =   +   $   ,   /   ?   %   #   [   ]
     *   %21  %2A %22 %27 %28 %29 %3B %3A %40 %26 %3D %2B %24 %2C %2F %3F %25 %23 %5B %5D
     * These loose any reserved meaning if encoded.
     *   
     * Other common, but unreserved, characters after percent-encoding 
     *   <   >   ~   .   {   }   |   \   -   `   _   ^
     *   %3C %3E %7E %2E %7B %7D %7C %5C %2D %60 %5F %5E
     * 
     * Unreserved characters treated equivalent to their unencoded form.  
     *   
     *   
     */
    public static void main(String...args)
    {
        // Reserved characters + space
        char reserved[] = 
            {' ',
             '\n','\t',
             '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
             '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;
        
        char[] other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;        
        
        for ( String x : args)
        {
            // Not URLEncoder which does www-form-encoding.
            String y = StrUtils.encodeHex(x, '%', reserved) ;
            System.out.println(y) ;
            
//            String s2 = URLEncoder.encode(s, "utf-8") ;
//            System.out.println(s2) ;

        }
    }
}
