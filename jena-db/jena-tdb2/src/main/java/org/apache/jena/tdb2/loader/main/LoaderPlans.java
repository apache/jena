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

package org.apache.jena.tdb2.loader.main;

import org.apache.jena.tdb2.loader.basic.LoaderBasic;
import org.apache.jena.tdb2.loader.sequential.LoaderSequential;

/**
 * Some algorithms expressed as {@link LoaderPlan}s.
 */
public class LoaderPlans {

    /**
     * Data phase: data to SPO and GSPO. <br/>
     * Index phase: POS and OSP + GPOS and GOSP <br/>
     * Index phase: SPOG, POSG and OSPG
     * <p>
     * on the principle that data is often triples or quads, but rarely a collection of
     * both in bulk.
     * </p><p>
     * This is the default loader option for {@code tdb2.tdbloader}, {@code --loader=phased}.
     * </p>
     */
    public static final LoaderPlan loaderPlanPhased = new LoaderPlan(
        InputStage.MULTI, 
        new String[]{ "SPO" },
        new String[]{ "GSPO" },
        new String[][]{ { "POS", "OSP" } },
        new String[][]{ {"GPOS", "GOSP"}, {"SPOG", "POSG", "OSPG"} }
        );

    /**
     * Do everything at once.  Maximum parallel action - may swamp the machine and be slower than {@link #loaderPlanPhased}.
     * <p>
     * Data phase: data to SPO and all secondary indexes; and to GSPO and all secondary indexes. <br/>
     * No other phases.
     * <p>
     * This is the loader option for {@code tdb2.tdbloader --loader=parallel}.
     */
    public static final LoaderPlan loaderPlanParallel = new LoaderPlan(
        InputStage.MULTI,
        new String[]{ "SPO", "POS", "OSP" },
        new String[]{ "GSPO", "GPOS", "GOSP", "SPOG", "POSG", "OSPG" },
        new String[][]{ },
        new String[][]{ }
        );

    /**
     * Lightly parallel, intermediate plan: for triples, this is two threaded. It aims to
     * speed up the data phase on a machine where an index is larger than the size of
     * available RAM (not heap). In this case it is better to use RAM for better caching a
     * single index than trying to work in parallel on two or more indexes. Like all load
     * plans, data shape and machine characteristics affect speed so experimentation is
     * recommended.
     * <p>
     * Data phase: One thread for parser and building the node table, and one thread for
     * each primary index.
     * <p>
     * Index phase: Secondary indexes: one by one.
     */
    public static final LoaderPlan loaderPlanLight = new LoaderPlan(
        InputStage.PARSE_NODE,
        new String[]{ "SPO" },
        new String[]{ "GSPO" },
        new String[][]{ { "POS" }, { "OSP" } },
        new String[][]{ { "GPOS" }, { "GOSP" }, { "SPOG" }, { "POSG" } , { "OSPG" } }
        );

    /**
     * A nearly sequential process, as a loader plan including single threaded first
     * phase. Each index is calculated separately but on a separate thread.
     * <p>
     * It is similar in performance and characteristics of
     * {@code tdb2.tdbloader --loader=sequential}, which is provided by
     * {@link LoaderSequential}.
     *
     * @see LoaderSequential
     */
    public static final LoaderPlan loaderPlanMinimal = new LoaderPlan(
        InputStage.PARSE_NODE_INDEX,
        new String[]{ "SPO" },
        new String[]{ "GSPO" },
        new String[][]{ { "POS" } , { "OSP" } },
        new String[][]{ { "GPOS" }, { "GOSP" }, { "SPOG" }, { "POSG" } , { "OSPG" } }
        );

    /**
     * A simple loader - do everything, at once, on the calling thread.
     * {@link LoaderBasic} is a similar algorithm but with a wrapper transaction
     * and not manipulating TDB2 via internal means.
     * <p>
     * It is similar in performance to {@code tdb2.tdbloader --loader=basic},
     * which is provided by {@link LoaderBasic}.
     *
     * @see LoaderBasic
     */
    public static final LoaderPlan loaderPlanSimple = new LoaderPlan(
        InputStage.PARSE_NODE_INDEX,
        new String[]{ "SPO" , "POS" , "OSP" },
        new String[]{ "GSPO" , "GPOS"  , "GOSP" , "SPOG" ,  "POSG" , "OSPG" },
        new String[][]{},
        new String[][]{}
        );

}
