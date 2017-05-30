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

import java.util.logging.LogRecord ;
import java.util.logging.StreamHandler ;

/** A handler and formatter for unadorned output to stdout.
 * Example: NCSA Format logging in Fuseki already formats the whole line 
 * 
 * <pre>
 * org.apache.jena.fuseki.Request.level=INFO
 * org.apache.jena.fuseki.Request.useParentHandlers=false
 * org.apache.jena.fuseki.Request.handlers=logging.FlatHandler
 * </pre>
 * @see FlatFormatter
 */
public class FlatHandler extends StreamHandler {
    
    public FlatHandler() { 
        super(System.out, new FlatFormatter(true)) ;
    }
    
    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }
}
