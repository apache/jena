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

package org.apache.jena.fuseki.build;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.Fuseki ;

import com.hp.hpl.jena.util.FileUtils ;

public class TemplateFunctions
{
    /** Read in a template from a file, substitute for {NAME} and return the string. */
    public static String templateFile(String templateName, Map<String, String> params) {
        String templateFilename = Template.getPath(templateName).toString() ;
        String template ;
        try { template = FileUtils.readWholeFileAsUTF8(templateFilename) ; }
        catch (IOException ex) { 
            Fuseki.serverLog.error("File not found: "+templateFilename);
            IO.exception(ex); return null ;
        }
        return templateString(template, params) ;
    }
    
    /** Read a template file, substitute for {NAME} and return the model. */
    public static String templateResource(String resourceName, Map<String, String> params) {
        String template ;
        try {
            InputStream in = TemplateFunctions.class.getClassLoader().getResourceAsStream(resourceName) ;
            if ( in == null )
                Fuseki.serverLog.error("Resource not found: "+resourceName);
            template = FileUtils.readWholeFileAsUTF8(in) ;
        }
        catch (IOException ex) { 
            Fuseki.serverLog.error("Error reading resource: "+resourceName);
            IO.exception(ex); return null ;
        }
        return templateString(template, params) ;
    }

    /** Create a template from a String */ 
    public static String templateString(String template, Map<String, String> params) {
        for ( Entry<String, String> e : params.entrySet() ) {
            template = template.replaceAll("\\{"+e.getKey()+"\\}", e.getValue()) ;
        }
        return template ;
    }
}
