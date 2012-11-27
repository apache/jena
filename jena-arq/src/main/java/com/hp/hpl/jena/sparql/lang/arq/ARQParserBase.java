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

package com.hp.hpl.jena.sparql.lang.arq;
import org.apache.jena.atlas.json.io.JSONHandler ;
import org.apache.jena.atlas.json.io.JSONHandlerBase ;
import org.apache.jena.atlas.lib.NotImplemented ;

import com.hp.hpl.jena.sparql.lang.SPARQLParserBase ;

class ARQParserBase
    extends SPARQLParserBase
    implements ARQParserConstants
{
    // JSON
    JSONHandler handler = new JSONHandlerBase() ;
    
    public void setHandler(JSONHandler handler)
    { 
        if ( handler == null )
            this.handler = new JSONHandlerBase() ;
        else
            this.handler = handler ;
    }
    
    // All the signals from the parsing process.
    protected void jsonStartParse(long currLine, long currCol)                 { handler.startParse(currLine, currCol) ; }
    protected void jsonFinishParse(long currLine, long currCol)                { handler.finishParse(currLine, currCol) ; }
    
    protected void jsonStartObject(long currLine, long currCol)                { handler.startObject(currLine, currCol) ; }
    protected void jsonFinishObject(long currLine, long currCol)               { handler.finishObject(currLine, currCol) ; }

    protected void jsonStartPair(long currLine, long currCol)                  { handler.startPair(currLine, currCol) ; }
    protected void jsonKeyPair(long currLine, long currCol)                    { handler.keyPair(currLine, currCol) ; }
    protected void jsonFinishPair(long currLine, long currCol)                 { handler.finishPair(currLine, currCol) ; }
    
    protected void jsonStartArray(long currLine, long currCol)                 { handler.startArray(currLine, currCol) ; }
    protected void jsonElement(long currLine, long currCol)                    { handler.element(currLine, currCol) ; }
    protected void jsonFinishArray(long currLine, long currCol)                { handler.finishArray(currLine, currCol) ; }

    protected void jsonValueString(String image, long currLine, long currCol)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image, currLine, currCol) ;
    }
        
    protected void jsonValueKeyString(String image, long currLine, long currCol) { handler.valueString(image, currLine, currCol) ; }
    protected void jsonValueInteger(String image, long currLine, long currCol)   { handler.valueInteger(image, currLine, currCol) ; }
    protected void jsonValueDecimal(String image, long currLine, long currCol)   { handler.valueDecimal(image, currLine, currCol) ; }
    protected void jsonValueDouble(String image, long currLine, long currCol)    { handler.valueDouble(image, currLine, currCol) ; }
    protected void jsonValueBoolean(boolean b, long currLine, long currCol)      { handler.valueBoolean(b, currLine, currCol) ; }
    protected void jsonValueNull(long currLine, long currCol)                    { handler.valueNull(currLine, currCol) ; }
    
    protected void jsonValueVar(String image, long currLine, long currCol)       { throw new NotImplemented("yet") ; }
}
