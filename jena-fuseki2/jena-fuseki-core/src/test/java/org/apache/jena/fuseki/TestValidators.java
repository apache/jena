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

import org.apache.jena.fuseki.server.Validators;
import org.junit.Test;

public class TestValidators {
    @Test public void validator_service_1() {
        Validators.serviceName("");
    }

    @Test public void validator_service_2() {
        Validators.serviceName("abc");
    }

    @Test public void validator_service_3() {
        Validators.serviceName("/abc");
    }

    @Test public void validator_service_4() {
        Validators.serviceName("-");
    }

    @Test public void validator_service_20() {
        Validators.serviceName("abc-def");
    }

    @Test public void validator_service_21() {
        Validators.serviceName("$/op");
    }

    @Test public void validator_service_22() {
        Validators.serviceName("/abc.def_ghi");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_1() {
        Validators.serviceName(null);
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_2() {
        Validators.serviceName(" ");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_3() {
        Validators.serviceName("\\");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_4() {
        Validators.serviceName("<");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_5() {
        Validators.serviceName(">");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_6() {
        Validators.serviceName("?");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_7() {
        Validators.serviceName("#");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_8() {
        Validators.serviceName("\"");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_service_bad_20() {
        Validators.serviceName("<http://example/>");
    }

    @Test  public void validator_endpoint_null() {
        Validators.endpointName(null);
    }

    @Test public void validator_endpoint_1() {
        Validators.endpointName("");
    }

    @Test public void validator_endpoint_2() {
        Validators.endpointName("abc");
    }

    @Test public void validator_endpoint_3() {
        Validators.endpointName("/abc");
    }

    @Test public void validator_endpoint_4() {
        Validators.endpointName("-");
    }

    @Test public void validator_endpoint_20() {
        Validators.endpointName("abc-def");
    }

    @Test public void validator_endpoint_21() {
        Validators.endpointName("$/op");
    }

    @Test public void validator_endpoint_22() {
        Validators.endpointName("/abc.def_ghi");
    }

//    @Test(expected=FusekiConfigException.class)
//    public void validator_endpoint_bad_1() {
//        Validators.endpointName(null);
//    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_2() {
        Validators.endpointName(" ");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_3() {
        Validators.endpointName("\\");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_4() {
        Validators.endpointName("<");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_5() {
        Validators.endpointName(">");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_6() {
        Validators.endpointName("?");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_7() {
        Validators.endpointName("#");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_8() {
        Validators.endpointName("\"");
    }

    @Test(expected=FusekiConfigException.class)
    public void validator_endpoint_bad_20() {
        Validators.endpointName("<http://example/>");
    }

}
