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

package org.openjena.fuseki.migrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.shared.JenaException;

// OLD - RIOT replaces 
public class GraphErrorHandler implements RDFErrorHandler 
{
    public static final Logger log = LoggerFactory.getLogger( GraphErrorHandler.class );
    int warnings = 0 ;
    int errors = 0 ;
    int fatalErrors = 0 ;
    
    @Override
    public void warning(Exception arg0)
    {
        warnings ++ ;
        log.warn(arg0.getMessage()) ;
    }

    @Override
    public void error(Exception e) {
        log.error(e.getMessage()) ;
        errors ++ ;
        throw e instanceof RuntimeException 
        ? (RuntimeException) e
        : new JenaException( e );
    }

    @Override
    public void fatalError(Exception e) {
        fatalErrors++ ;
        log.error(e.getMessage());
        
        throw e instanceof RuntimeException 
            ? (RuntimeException) e
            : new JenaException( e );
    }
}
