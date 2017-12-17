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

package org.apache.jena.fuseki.validation.json;

import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.getArg;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.getArgOrNull;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jErrors;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseError;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseErrorCol;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.jParseErrorLine;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

public class UpdateValidatorJSON {

    public UpdateValidatorJSON() {}
    
    static final String paramUpdate           = "update" ;
    static final String paramSyntax           = "languageSyntax" ;
    
    static final String jInput           = "input" ;
    static final String jFormatted       = "formatted" ;

    public static JsonObject execute(ValidationAction action) {
        JsonBuilder obj = new JsonBuilder() ;
        obj.startObject() ;
        
        final String updateString = getArg(action, paramUpdate) ;
        String updateSyntax = getArgOrNull(action, paramSyntax) ;
        if ( updateSyntax == null || updateSyntax.equals("") )
            updateSyntax = "SPARQL" ;
        
        Syntax language = Syntax.lookup(updateSyntax) ;
        if ( language == null ) {
            ServletOps.errorBadRequest("Unknown syntax: " + updateSyntax) ;
            return null ;
        }
        
        obj.key(jInput).value(updateString) ;
        UpdateRequest request = null ;
        try {
            request = UpdateFactory.create(updateString, "http://example/base/", language) ;
        } catch (QueryParseException ex) {
            obj.key(jErrors) ;
            obj.startArray() ;      // Errors array
            obj.startObject() ;
            obj.key(jParseError).value(ex.getMessage()) ;
            obj.key(jParseErrorLine).value(ex.getLine()) ;
            obj.key(jParseErrorCol).value(ex.getColumn()) ;
            obj.finishObject() ;
            obj.finishArray() ;
            
            obj.finishObject() ; // Outer object
            return obj.build().getAsObject() ;
        }
        
        formatted(obj, request) ;
        
        obj.finishObject() ;
        return obj.build().getAsObject() ;
    }

    private static void formatted(JsonBuilder obj, UpdateRequest updateRequest) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        updateRequest.output(out) ;
        obj.key(jFormatted).value(out.asString()) ;
    }
}

