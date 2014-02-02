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
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.Fuseki ;

import com.hp.hpl.jena.util.FileUtils ;

public class TemplateFunctions
{
    /** Read a template file, substitute for {NAME} and return the model. */
    public static String template(String templateFile, Map<String, String> params) {
        String template ;
        try { template = FileUtils.readWholeFileAsUTF8(templateFile) ; }
        catch (IOException ex) { 
            Fuseki.serverLog.error("File not found: "+templateFile);
            IO.exception(ex); return null ;
        }
        for ( Entry<String, String> e : params.entrySet() ) {
            template = template.replaceAll("\\{"+e.getKey()+"\\}", e.getValue()) ;
        }
        return template ;
    }
}
