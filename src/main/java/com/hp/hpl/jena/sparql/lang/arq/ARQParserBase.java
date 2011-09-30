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

package com.hp.hpl.jena.sparql.lang.arq;
import org.openjena.atlas.json.io.JSONHandler ;
import org.openjena.atlas.json.io.JSONHandlerBase ;
import org.openjena.atlas.lib.NotImplemented ;

import com.hp.hpl.jena.sparql.lang.ParserQueryBase ;

class ARQParserBase
    extends ParserQueryBase
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
    protected void jsonStartParse()                 { handler.startParse() ; }
    protected void jsonFinishParse()                { handler.finishParse() ; }
    
    protected void jsonStartObject()                { handler.startObject() ; }
    protected void jsonFinishObject()               { handler.finishObject() ; }

    protected void jsonStartPair()                  { handler.startPair() ; }
    protected void jsonKeyPair()                    { handler.keyPair() ; }
    protected void jsonFinishPair()                 { handler.finishPair() ; }
    
    protected void jsonStartArray()                 { handler.startArray() ; }
    protected void jsonElement()                    { handler.element() ; }
    protected void jsonFinishArray()                { handler.finishArray() ; }

    protected void jsonValueString(String image)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image) ;
    }
        
    protected void jsonValueKeyString(String image) { handler.valueString(image) ; }
    protected void jsonValueInteger(String image)   { handler.valueInteger(image) ; }
    protected void jsonValueDecimal(String image)   { handler.valueDecimal(image) ; }
    protected void jsonValueDouble(String image)    { handler.valueDouble(image) ; }
    protected void jsonValueBoolean(boolean b)      { handler.valueBoolean(b) ; }
    protected void jsonValueNull()                  { handler.valueNull() ; }
    
    protected void jsonValueVar(String image)       { throw new NotImplemented("yet") ; }
}
