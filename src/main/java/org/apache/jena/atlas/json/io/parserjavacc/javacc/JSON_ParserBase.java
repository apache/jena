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

package org.apache.jena.atlas.json.io.parserjavacc.javacc;

import org.apache.jena.atlas.json.io.JSONHandler ;
import org.apache.jena.atlas.json.io.JSONHandlerBase ;

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
    protected void startParse(long currLine, long currCol)                 { handler.startParse(currLine, currCol) ; }
    protected void finishParse(long currLine, long currCol)                { handler.finishParse(currLine, currCol) ; }
    
    protected void startObject(long currLine, long currCol)                { handler.startObject(currLine, currCol) ; }
    protected void finishObject(long currLine, long currCol)               { handler.finishObject(currLine, currCol) ; }

    protected void startPair(long currLine, long currCol)                  { handler.startPair(currLine, currCol) ; }
    protected void keyPair(long currLine, long currCol)                    { handler.keyPair(currLine, currCol) ; }
    protected void finishPair(long currLine, long currCol)                 { handler.finishPair(currLine, currCol) ; }
    
    protected void startArray(long currLine, long currCol)                 { handler.startArray(currLine, currCol) ; }
    protected void element(long currLine, long currCol)                    { handler.element(currLine, currCol) ; }
    protected void finishArray(long currLine, long currCol)                { handler.finishArray(currLine, currCol) ; }

    protected void valueString(String image, long currLine, long currCol)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image, currLine, currCol) ;
    }
        
    protected void valueInteger(String image, long currLine, long currCol)   { handler.valueInteger(image, currLine, currCol) ; }
    protected void valueDecimal(String image, long currLine, long currCol)   { handler.valueDecimal(image, currLine, currCol) ; }
    protected void valueDouble(String image, long currLine, long currCol)    { handler.valueDouble(image, currLine, currCol) ; }
    protected void valueBoolean(boolean b, long currLine, long currCol)      { handler.valueBoolean(b, currLine, currCol) ; }
    protected void valueNull(long currLine, long currCol)                    { handler.valueNull(currLine, currCol) ; }
}
