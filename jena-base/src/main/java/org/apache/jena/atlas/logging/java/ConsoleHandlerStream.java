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

import java.io.OutputStream ;
import java.nio.charset.StandardCharsets ;
import java.util.logging.ConsoleHandler ;
import java.util.logging.LogManager ;

/** Console handler that modifies {@link java.util.logging.ConsoleHandler}.
 * Supports the configuration parameters of {@link ConsoleHandler} -- {@code .level},
 * {@code .filter}, {@code .formatter} and {@code .encoding}.
 * <p>
 * Defaults:
 * <ul>
 * <li>Stdout, rather than stderr</li>
 * <li>{@link TextFormatter} rather than {@link java.util.logging.SimpleFormatter}</li>
 * <li>UTF-8, rather than platform charset</li>
 * </ul>
 */
public class ConsoleHandlerStream extends ConsoleHandler {

    public ConsoleHandlerStream() {
        this(System.out) ;
    }
    
    public ConsoleHandlerStream(OutputStream outputStream) {
        super() ;
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        // Change the formatter default from SimpleFormatter to TextFormatter.
        String pNameFormatter = cname +".formatter" ;
        if ( manager.getProperty(pNameFormatter) == null )
            setFormatter(new TextFormatter()) ;
        String pNameEncoding = cname +".encoding" ;
        if ( manager.getProperty(pNameEncoding) == null ) {
            try { setEncoding(StandardCharsets.UTF_8.name()) ; }
            catch (Exception e) { 
                // That should work as it is a required charset. 
                System.err.print("Failed to set encoding: "+e.getMessage()) ;
                // Ignore and try to carry on.
            }
        }
        // Temporary fix : setOutputStream closes the old setting which is backed by System.err.  
        //setOutputStream(outputStream);
    }
}
