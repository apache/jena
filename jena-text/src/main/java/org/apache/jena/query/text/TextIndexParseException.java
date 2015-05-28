/**
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

package org.apache.jena.query.text;

import org.apache.jena.query.QueryParseException ;


public class TextIndexParseException extends QueryParseException 
{
//    public TextIndexParseException(String textQuery) { super(message(textQuery, null),-1,-1) ; }
//    public TextIndexParseException(String textQuery, Throwable cause) { super(message(textQuery,null), cause,-1,-1) ; }
    public TextIndexParseException(String textQuery, String msg) { super(message(textQuery,msg),-1,-1) ; }
    public TextIndexParseException(String textQuery, String msg, Throwable cause) { super(message(textQuery,msg), cause,-1,-1) ; }

    private static String message(String textQuery, String errorMessage) {
        // No need to include the textQuery as Lucene puts it in.
        String msg = ( errorMessage == null )
            ? "Text search parse error: text query '"+textQuery+"'"
            : "Text search parse error:\n"+errorMessage ;
        return msg ;
    }

}

