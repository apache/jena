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

package org.openjena.atlas.json.io.parserjavacc.javacc;

import org.openjena.atlas.json.io.JSONHandler ;
import org.openjena.atlas.json.io.JSONHandlerBase ;

public class JSON_ParserBase
{
    JSONHandler handler = new JSONHandlerBase() ;
    
    public void setHandler(JSONHandler handler)
    { 
        if ( handler == null )
            this.handler = new JSONHandlerBase() ;
        else
            this.handler = handler ;
    }
    
    // All the signals from the parsing process.
    protected void startParse()                 { handler.startParse() ; }
    protected void finishParse()                { handler.finishParse() ; }
    
    protected void startObject()                { handler.startObject() ; }
    protected void finishObject()               { handler.finishObject() ; }

    protected void startPair()                  { handler.startPair() ; }
    protected void keyPair()                    { handler.keyPair() ; }
    protected void finishPair()                 { handler.finishPair() ; }
    
    protected void startArray()                 { handler.startArray() ; }
    protected void element()                    { handler.element() ; }
    protected void finishArray()                { handler.finishArray() ; }

    protected void valueString(String image)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image) ;
    }
        
    protected void valueInteger(String image)   { handler.valueInteger(image) ; }
    protected void valueDecimal(String image)   { handler.valueDecimal(image) ; }
    protected void valueDouble(String image)    { handler.valueDouble(image) ; }
    protected void valueBoolean(boolean b)      { handler.valueBoolean(b) ; }
    protected void valueNull()                  { handler.valueNull() ; }
}
