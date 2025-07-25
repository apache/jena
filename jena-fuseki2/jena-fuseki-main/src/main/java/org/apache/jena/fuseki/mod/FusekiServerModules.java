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

package org.apache.jena.fuseki.mod;

import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mod.admin.FMod_Admin;
import org.apache.jena.fuseki.mod.prometheus.FMod_Prometheus;
import org.apache.jena.fuseki.mod.shiro.FMod_Shiro;
import org.apache.jena.fuseki.mod.ui.FMod_UI;

public class FusekiServerModules {

    /** A use-once {@link FusekiModules} for the full-featured Fuseki server. */
    public static FusekiModules serverModules() {
        // Modules may have state that is carried across the build steps or used for reload.
        FusekiModule fmodShiro = FMod_Shiro.create();
        FusekiModule fmodAdmin = FMod_Admin.create();
        FusekiModule fmodUI = FMod_UI.create();
        FusekiModule fmodPrometheus = FMod_Prometheus.create();

        FusekiModules serverModules = FusekiModules.create(fmodAdmin, fmodUI, fmodShiro, fmodPrometheus);
        return serverModules;
    }

}
