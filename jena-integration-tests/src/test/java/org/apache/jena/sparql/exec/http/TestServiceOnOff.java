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

package org.apache.jena.sparql.exec.http;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Service enable/disable */
public class TestServiceOnOff {

    private static String SERVICE;
    private static String QUERY;
    private static EnvTest env;
    // Local dataset for execution of SERVICE.
    private static final DatasetGraph localDataset() {return DatasetGraphZero.create(); }

    @BeforeClass public static void beforeClass() {
        //FusekiLogging.setLogging();
        env = EnvTest.create("/ds");
        SERVICE = env.datasetURL();
        QUERY = "ASK { SERVICE <"+SERVICE+"> {} }";
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    // -- Test the global allow/block setting.

    @Test
    public void service_allowed_1() {
        run_preserveGlobalAllowed(true, ()->{
            service_systemSettings(true, true, ()->exec(localDataset()));
        });
    }

    @Test(expected=QueryDeniedException.class)
    public void service_allowed_2() {
        run_preserveGlobalAllowed(true, ()->{
            service_systemSettings(true, false, ()->exec(localDataset()));
        });
    }

    @Test(expected=QueryDeniedException.class)
    public void service_notAllowed_1() {
        run_preserveGlobalAllowed(false, ()->{
            service_systemSettings(true, false, ()->exec(localDataset()));
        });
    }

    @Test(expected=QueryDeniedException.class)
    public void service_notAllowed_2() {
        run_preserveGlobalAllowed(false, ()->{
            service_systemSettings(false, true, ()->exec(localDataset()));
        });
    }

    // -- Test setting the system context
    @Test(expected=QueryDeniedException.class)
    public void service_query_global_context_disabled() {
        service_systemSettings(true, false, ()->exec(localDataset()));
    }

    @Test
    public void service_query_global_default_enabled() {
        service_systemSettings(true, null, ()->exec(localDataset()));
    }

    @Test(expected=QueryDeniedException.class)
    public void service_query_global_default_disabled() {
        service_systemSettings(false, null, ()->exec(localDataset()));
    }

    // -- Test setting the dataset context
    @Test
    public void service_query_local_dataset_enabled() {
        DatasetGraph localdsg = localDataset();
        localdsg.getContext().set(ARQ.httpServiceAllowed, true);
        service_systemSettings(false, false, ()->exec(localdsg));

    }

    @Test(expected=QueryDeniedException.class)
    public void service_query_local_dataset_disabled() {
        DatasetGraph localdsg = localDataset();
        localdsg.getContext().set(ARQ.httpServiceAllowed, false);
        service_systemSettings(true, true, ()->exec(localdsg));
    }

    @Test public void service_query_global_context_enabled() {
        service_systemSettings(false, true, ()->exec(localDataset()));
    }

    // -- Test setting the execution context
    public void service_queryExecCxt_allowed() {
        service_systemSettings(false, null, ()->{
            Context context = Context.create().set(ARQ.httpServiceAllowed, false);
            try ( QueryExec qExec = QueryExec.dataset(localDataset()).query(QUERY).context(context).build() ) {
                qExec.ask();
            }
        });
    }

    @Test(expected=QueryDeniedException.class)
    public void service_queryExecCxt_disabled() {
        service_systemSettings(true, null, ()->{
            Context context = Context.create().set(ARQ.httpServiceAllowed, false);
            try ( QueryExec qExec = QueryExec.dataset(localDataset()).query(QUERY).context(context).build() ) {
                qExec.ask();
            }
        });
    }

    // --

    private void exec(DatasetGraph dsg) {
        try ( QueryExec qExec = QueryExec.dataset(dsg).query(QUERY).build() ) {
            qExec.ask();
        }
    }

    private void service_systemSettings(boolean dftSetting, Boolean systemSetting, Runnable action) {
        String queryString = "ASK { SERVICE <"+SERVICE+"?format=json> { BIND(now() AS ?now) } }";
        Object testSuiteValue = ARQ.getContext().get(ARQ.httpServiceAllowed);
        run_preserveSettings(()->{
            ARQ.allowServiceDefault = dftSetting;
            if ( systemSetting == null )
                ARQ.getContext().unset(ARQ.httpServiceAllowed);
            else
                ARQ.getContext().set(ARQ.httpServiceAllowed, systemSetting);
            action.run();
        });
    }

    /** Run code, preserving ARQ.globalServiceAllowed. */
    private void run_preserveGlobalAllowed(boolean globalAllowedSetting, Runnable action) {
        boolean globalSetting = ARQ.globalServiceAllowed;
        ARQ.globalServiceAllowed = globalAllowedSetting;
        try {
            action.run();
        } finally {
            ARQ.globalServiceAllowed = globalSetting;
        }
    }


    /** Run code, preserving ARQ.defaultServiceDisabled and the ARQ.context setting. */
    private void run_preserveSettings(Runnable action) {
        boolean globalSetting = ARQ.allowServiceDefault;
        Object contextValue = ARQ.getContext().get(ARQ.httpServiceAllowed);
        try {
            action.run();
        } finally {
            ARQ.allowServiceDefault = globalSetting;
            ARQ.getContext().set(ARQ.httpServiceAllowed, contextValue);
        }
    }
}
