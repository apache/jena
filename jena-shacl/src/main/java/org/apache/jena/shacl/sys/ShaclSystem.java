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

package org.apache.jena.shacl.sys;

import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.validation.ShaclPlainValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaclSystem {
    public static Logger systemShaclLogger = LoggerFactory.getLogger("SHACL"); 
    public static ErrorHandler systemShaclErrorHandler = ErrorHandlerFactory.errorHandlerStd(systemShaclLogger);
    
    private static ShaclValidator globalDefault = new ShaclPlainValidator();
    
    /** Set the current system-wide {@link ShaclValidator}. */
    public static void set(ShaclValidator validator) { globalDefault = validator; }

    /** The current system-wide {@link ShaclValidator}. */ 
    public static ShaclValidator get() { return globalDefault; }
}
