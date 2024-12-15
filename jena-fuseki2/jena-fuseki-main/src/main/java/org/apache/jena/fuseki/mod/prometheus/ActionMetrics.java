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
package org.apache.jena.fuseki.mod.prometheus;

import org.apache.jena.fuseki.ctl.ActionCtl;
import org.apache.jena.fuseki.metrics.MetricsProviderRegistry;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

public class ActionMetrics extends ActionCtl {

    public ActionMetrics() { super(); }

    @Override
    public void execGet(HttpAction action) {
        super.executeLifecycle(action);
    }

    @Override
    public void execOptions(HttpAction action) {
        ActionLib.doOptionsGet(action);
        ServletOps.success(action);
    }

    @Override
    public void validate(HttpAction action) {}

    @Override
    public void execute(HttpAction action) {
        MetricsProviderRegistry.get().scrape( action );
        ServletOps.success(action);
    }
}
