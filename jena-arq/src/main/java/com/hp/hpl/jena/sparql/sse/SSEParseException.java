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

package com.hp.hpl.jena.sparql.sse;

import com.hp.hpl.jena.sparql.ARQException ;

public class SSEParseException extends ARQException
{
    private int line = -1 ;
    private int column = -1 ;
    
    public SSEParseException(int line, int column)
    { super() ; set(line, column) ; }
    
    public SSEParseException(Throwable cause, int line, int column)
    { super(cause) ; set(line, column) ; }
    
    public SSEParseException(String msg, int line, int column)
    { super(msg) ; set(line, column) ; }
    
    public SSEParseException(String msg, Throwable cause,int line, int column)
    { super(msg, cause) ; set(line, column) ; }
    
    private void set(int line, int column)
    { this.line = line ; this.column = column ; }

    /** Column number where the parse exception occurred. */
    public int getColumn() { return column ; }

    /** Line number where the parse exception occurred. */
    public int getLine()   { return line ; }
}
