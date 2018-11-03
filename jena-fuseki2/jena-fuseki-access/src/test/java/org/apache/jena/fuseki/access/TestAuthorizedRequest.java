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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.build.RequestAuthorization;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class TestAuthorizedRequest {
    
    static Model model = RDFDataMgr.loadModel("testing/Access/allowedUsers.ttl");

    
    @Test public void authrequest_anon() {
        RequestAuthorization req = RequestAuthorization.policyAllowAnon();
        assertTrue(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
    }
    
    @Test public void authrequest_anyLoggedIn_1() {
        RequestAuthorization req = RequestAuthorization.policyAllowAuthenticated();
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
    }
    
    @Test public void authrequest_anyLoggedIn_2() {
        RequestAuthorization req = RequestAuthorization.policyAllowSpecific("*");
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
    }

    @Test public void authrequest_noOne() {
        RequestAuthorization req = RequestAuthorization.policyNoAccess();
        assertFalse(req.isAllowed(null));
        assertFalse(req.isAllowed("user1"));
    }


    @Test public void authrequest_user_1() {
        RequestAuthorization req = RequestAuthorization.policyAllowSpecific("user1", "user2");
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
        assertTrue(req.isAllowed("user2"));
        assertFalse(req.isAllowed("user3"));
    }
    
    @Test public void authrequest_parse_no_info_1() {
        Resource r = model.createResource("http://example/notInData");
        RequestAuthorization req = FusekiBuilder.allowedUsers(r);
        assertNull(req);
    }

    @Test public void authrequest_parse_no_info_2() {
        Resource r = model.createResource("http://example/none");
        RequestAuthorization req = FusekiBuilder.allowedUsers(r);
        assertNull(req);
    }

    @Test public void authrequest_parse_1() {
        Resource r = model.createResource("http://example/r1");
        RequestAuthorization req = FusekiBuilder.allowedUsers(r);
        assertNotNull(req);
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
        assertTrue(req.isAllowed("user2"));
        assertFalse(req.isAllowed("user3"));
    }
    
    @Test public void authrequest_parse_2() {
        Resource r = model.createResource("http://example/r2");
        RequestAuthorization req = FusekiBuilder.allowedUsers(r);
        assertNotNull(req);
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
        assertTrue(req.isAllowed("user2"));
        assertFalse(req.isAllowed("user3"));
    }
    
    @Test public void authrequest_parse_loggedIn() {
        Resource r = model.createResource("http://example/rLoggedIn");
        RequestAuthorization req = FusekiBuilder.allowedUsers(r);
        assertNotNull(req);
        assertFalse(req.isAllowed(null));
        assertTrue(req.isAllowed("user1"));
        assertTrue(req.isAllowed("user3"));
    }
}
