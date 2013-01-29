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

package com.hp.hpl.jena.shared;

/**
    Exception to throw when a character cannot be encoded into some context
    for some reason.
*/
public class CannotEncodeCharacterException extends JenaException
    {
    protected final char badChar;
    protected final String encodingContext;
    
    public CannotEncodeCharacterException( char badChar, String encodingContext )
        {
        super( "cannot encode (char) " + badChar + " in context " + encodingContext );
        this.badChar = badChar; 
        this.encodingContext = encodingContext;
        }

    /**
        Answer the character that could not be encoded.
    */
    public char getBadChar()
        { return badChar; }

    /**
        Answer the name of the context in which the encoding failed.
    */
    public String getEncodingContext()
        { return encodingContext; }
    }
