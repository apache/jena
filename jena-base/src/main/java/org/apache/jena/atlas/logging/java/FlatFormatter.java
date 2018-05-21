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

package org.apache.jena.atlas.logging.java;

import java.text.MessageFormat ;
import java.util.logging.Formatter ;
import java.util.logging.LogRecord ;

/** Very simple formatter - just the log message.
 * @see FlatHandler
 */ 
public class FlatFormatter extends Formatter {

    private final boolean ensureNL ;

    public FlatFormatter() { this(true) ; }
    
    public FlatFormatter(boolean ensureNewline) {
        this.ensureNL = ensureNewline ;
    }

    @Override
    public String format(LogRecord record) {
        String message = record.getMessage() ;
        if ( record.getParameters() != null )
            message = MessageFormat.format(message, record.getParameters()) ;
        if ( ensureNL && ! message.endsWith("\n") )
            message = message + "\n" ;
        return message ;                 
    }
}
