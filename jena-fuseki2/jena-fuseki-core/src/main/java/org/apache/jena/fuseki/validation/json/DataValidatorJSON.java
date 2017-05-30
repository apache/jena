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

import java.io.StringReader ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import static org.apache.jena.fuseki.validation.json.ValidatorJsonLib.*;

public class DataValidatorJSON {

    public DataValidatorJSON() { }
  
    static final String jInput           = "input" ;

    static final String paramFormat           = "outputFormat" ;
    static final String paramIndirection      = "url" ;
    static final String paramData             = "data" ;
    static final String paramSyntax           = "languageSyntax" ;
    
    public static JsonObject execute(ValidationAction action) {
        JsonBuilder obj = new JsonBuilder() ;
        obj.startObject() ;
        
        String syntax = getArgOrNull(action, paramSyntax) ;
        if ( syntax == null || syntax.equals("") )
            syntax = RDFLanguages.NQUADS.getName() ;

        Lang language = RDFLanguages.shortnameToLang(syntax) ;
        if ( language == null ) {
            ServletOps.errorBadRequest("Unknown syntax: " + syntax) ;
            return null ;
        }

        String string = getArg(action, paramData) ;
        StringReader sr = new StringReader(string) ;
        obj.key(jInput).value(string) ;
        StreamRDF dest = StreamRDFLib.sinkNull() ;
        
        try {
            RDFParser.create().source(sr).lang(language).parse(dest);
        } catch (RiotParseException ex) {
            obj.key(jErrors) ;

            obj.startArray() ;      // Errors array
            obj.startObject() ;
            obj.key(jParseError).value(ex.getMessage()) ;
            obj.key(jParseErrorLine).value(ex.getLine()) ;
            obj.key(jParseErrorCol).value(ex.getCol()) ;
            obj.finishObject() ;
            obj.finishArray() ;
            
            obj.finishObject() ; // Outer object
            return obj.build().getAsObject() ;
        } catch (RiotException ex) {
            obj.key(jErrors) ;

            obj.startArray() ;      // Errors array
            obj.startObject() ;
            obj.key(jParseError).value(ex.getMessage()) ;
            obj.finishObject() ;
            obj.finishArray() ;
            
            obj.finishObject() ; // Outer object
            return obj.build().getAsObject() ;
        }

        
        obj.finishObject() ;
        return obj.build().getAsObject() ;
    }
}

