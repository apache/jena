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

import java.io.StringReader ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.FusekiVocab ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

// Check whether this is used or needed
public abstract class DataServiceDesc
{
    public static DataServiceDesc createFromTemplate(String templateFile, String dbName) {
        Map<String, String> params = new HashMap<>() ;
        params.put(Template.NAME, dbName) ;
        String template = TemplateFunctions.templateFile(templateFile, params) ;
        Lang lang = RDFLanguages.filenameToLang(templateFile, Lang.TTL) ;
        StringReader sr = new StringReader(template) ;
        return create(sr, lang) ;
    }
    
    public static DataServiceDesc create(StringReader strReader, Lang lang ) {
        Model model = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model, strReader, "http://base/", lang) ;
        Resource root ;
        try {
            root = GraphUtils.findRootByType(model, FusekiVocab.fusekiService) ;
            if ( root == null )
                throw new FusekiConfigException("No root of type "
                    + FmtUtils.stringForResource(FusekiVocab.fusekiService) + "found") ;
        } catch (TypeNotUniqueException ex) {
            throw new FusekiConfigException("Multiple items of type: " + FusekiVocab.fusekiService) ;
        }
        return new DataServiceDescResource(root) ;
    }

    public static DataServiceDesc create(DataService dataService) {
        return new DataServiceDescPrebuilt(dataService) ;
    }
    
    //public abstract Resource getResource() ;

    public abstract DataService build() ;
//    public abstract void unbuild() ;


    private static class DataServiceDescResource extends DataServiceDesc {
        protected Resource resource ; 

        protected DataServiceDescResource(Resource resource) {
            this.resource = resource ;
        }

        public Resource getResource() { return resource ; }

        @Override
        public DataService build() {
            return Builder.buildDataService(resource) ;
        }
    }
    
    private static class DataServiceDescPrebuilt extends DataServiceDesc {

        private DataService dataService ;

        protected DataServiceDescPrebuilt(DataService dataService) {
            this.dataService = dataService ;
        }

        @Override
        public DataService build() {
            return dataService ;
        }
    }

}
