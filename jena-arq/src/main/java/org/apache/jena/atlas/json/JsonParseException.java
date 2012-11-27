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

package org.apache.jena.atlas.json;


public class JsonParseException extends JsonException
{
    private int line = -1 ;
    private int column = -1 ;

    public JsonParseException(int line, int column)
    { super() ; set(line, column) ; }

    public JsonParseException(Throwable cause, int line, int column)
    { super(cause) ; set(line, column) ; }

    public JsonParseException(String msg, int line, int column)
    { super(msg) ; set(line, column) ; }

    public JsonParseException(String msg, Throwable cause,int line, int column)
    { super(msg, cause) ; set(line, column) ; }

    private void set(int line, int column)
    { this.line = line ; this.column = column ; }

    /** Column number where the parse exception occurred. */
    public int getColumn() { return column ; }

    /** Line number where the parse exception occurred. */
    public int getLine()   { return line ; }
    
    
    public static String formatMessage(String msg, int line, int column)
    { 
        if ( line == -1 || column == -1 )
            return msg ; 
        return String.format("[line: %d, col: %d] "+msg, line, column) ; 
    }
}
