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

package org.apache.jena.fuseki;

import static org.apache.jena.fuseki.test.FusekiTest.expect401;

import java.net.URI;

import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAuthUpdate_JDK extends AbstractTestAuth_JDK {
    @Test
    public void update_jdk_auth_01() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessor ue = UpdateExecutionHTTP.create()
                                    .service(authServiceUpdate)
                                    .update(updates)
                                    .build();
        // No auth credentials should result in an error
        expect401(()->ue.execute());
    }

    @Test
    public void update_jdk_auth_02() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessor ue = withAuthJDK(UpdateExecutionHTTP.create()
                                               .service(authServiceUpdate)
                                               .update(updates),
                                           "allowed", "bad-password");
        expect401(()->ue.execute());
    }

    @Test
    public void update_jdk_auth_03() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessor ue = withAuthJDK(UpdateExecutionHTTP.create()
                                               .service(authServiceUpdate)
                                               .update(updates),
                                           "allowed", "password");
        ue.execute();
    }

    @Test
    public void update_with_auth_04() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessor ue = withAuthJDK(UpdateExecutionHTTP.create()
                                               .service(authServiceUpdate)
                                               .update(updates),
                                           "allowed", "password");
        ue.execute();
    }

    @Test
    public void update_authenv_01_good() {
        // Auth credentials for valid user with correct password
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessor ue = UpdateExecutionHTTP.create().service(authServiceUpdate).update(updates).build();
        String dsURL = authServiceUpdate;
        URI uri = URI.create(dsURL);
        AuthEnv.get().registerUsernamePassword(uri, "allowed", "password");
        try {
            ue.execute();
        } finally {
            AuthEnv.get().unregisterUsernamePassword(uri);
        }
    }
}
