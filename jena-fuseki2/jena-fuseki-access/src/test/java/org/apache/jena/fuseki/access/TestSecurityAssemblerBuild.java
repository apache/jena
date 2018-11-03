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

package org.apache.jena.fuseki.access;

import static org.junit.Assert.assertNotNull;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

/** Test parsing of assembers with security aspects */ 
public class TestSecurityAssemblerBuild {
    static { JenaSystem.init(); }
    static final String DIR = "testing/Access/";

    @Test public void assembler1() {
        assemblerFile(DIR+"assem-security.ttl");
    }
    
    @Test public void assembler2() {
        assemblerFile(DIR+"assem-security-shared.ttl");
    }
    
    private void assemblerFile(String assemblerFile) { 
        Dataset ds = (Dataset)AssemblerUtils.build(assemblerFile, VocabSecurity.tAccessControlledDataset);
        DatasetGraphAccessControl dsg = (DatasetGraphAccessControl)ds.asDatasetGraph();
        AuthorizationService securityRegistry = dsg.getAuthService();
        assertNotNull(securityRegistry);
    }
}
