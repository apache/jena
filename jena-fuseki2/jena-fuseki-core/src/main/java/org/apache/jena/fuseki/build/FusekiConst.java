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

package org.apache.jena.fuseki.build;

import org.apache.jena.atlas.lib.StrUtils;

/** Internal constants */
public class FusekiConst {

    public static String PREFIXES = StrUtils.strjoinNL
    ("BASE            <http://example/base#>",
     "PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>",
     "PREFIX fu:      <http://jena.apache.org/fuseki#>",
     "PREFIX fuseki:  <http://jena.apache.org/fuseki#>",
     "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
     "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
     "PREFIX tdb:     <http://jena.hpl.hp.com/2008/tdb#>",
     "PREFIX sdb:     <http://jena.hpl.hp.com/2007/sdb#>",
     "PREFIX list:    <http://jena.apache.org/ARQ/list#>",
     "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>",
     "PREFIX apf:     <http://jena.apache.org/ARQ/property#>",
     "PREFIX afn:     <http://jena.apache.org/ARQ/function#>",
     "",
     "") ;

}
