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

package com.hp.hpl.jena.sparql.mgt ;

import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

public class SystemInfo implements SystemInfoMBean {
    private final String name ;
    private final Node   iri ;
    private final String jmxPath ;
    private final String version ;
    private final String buildDate ;

    public SystemInfo(String name, String jmxPath, String version, String buildDate) {
        this.name = name ;
        this.iri = createIRI(name) ;
        this.jmxPath = jmxPath ;
        this.version = version ;
        this.buildDate = buildDate ;
    }

    private static Node createIRI(String iriStr) {
        try {
            return NodeFactory.createURI(IRIResolver.resolveString(iriStr)) ;
        } catch (RuntimeException ex) {
            return null ;
        }
    }

    @Override
    public String getBuildDate() {
        return buildDate ;
    }

    @Override
    public String getVersion() {
        return version ;
    }

    @Override
    public String getName() {
        return name ;
    }

    public Node getIRI() {
        return NodeFactory.createURI(name) ;
    }

    public String getJmxPath() {
        return jmxPath ;
    }
}
