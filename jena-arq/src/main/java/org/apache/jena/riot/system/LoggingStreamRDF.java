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

package org.apache.jena.riot.system ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.sparql.core.Quad ;
import org.slf4j.Logger ;

public class LoggingStreamRDF implements StreamRDF {
    private Logger log ;

    public LoggingStreamRDF(Logger log) {
        this.log = log ;
    }

    @Override
    public void start() {}

    @Override
    public void triple(Triple triple) {
        String string = NodeFmtLib.str(triple) ;
        log.info(string) ;
    }

    @Override
    public void quad(Quad quad) {
        String string = NodeFmtLib.str(quad) ;
        log.info(string) ;
    }

    @Override
    public void base(String base) {
        log.info("BASE -- " + base) ;
    }

    @Override
    public void prefix(String prefix, String iri) {
        log.info("Prefix (" + prefix + "," + iri + ")") ;
    }

    @Override
    public void finish() {}

}
