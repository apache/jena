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

package org.apache.jena.fuseki.main.cmds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;

/**
 * The list of {@link FusekiServerArgsCustomiser FusekiServerArgsCustomisers} used by FusekiMain.
 */
class ArgCustomizers {
    private ArgCustomizers(){}

    // Null means "use system modules"
    static private List<FusekiServerArgsCustomiser> customisers = null;

    /*package*/ static void addCustomiser(FusekiServerArgsCustomiser customiser) {
        Objects.requireNonNull(customiser);
        if ( customisers == null )
            customisers = new ArrayList<>();
        customisers.add(customiser);
    }

    /**
     * Remove any previously registered CLI customisers
     */
    /*package*/ static void resetCustomisers() {
        // Goes back to supplying the system modules.
        customisers = null;
    }

    /**
     * Return the current setting of
     * {@list FusekiServerArgsCustomiser argument line customisers}.
     * If none have been registered, return the system set of fuseki modules.
     */
    /*package*/ static List<FusekiServerArgsCustomiser> get() {
        if ( customisers == null )
            return supplierSystemCustomizers.get();
        return customisers;
    }

    private static Supplier<List<FusekiServerArgsCustomiser>> supplierSystemCustomizers = ()->{
        List<FusekiModule> x = FusekiModules.getSystemModules().asList();
        // Generics: need an explicit cast
        return x.stream().map(z->(FusekiServerArgsCustomiser)z).toList();
    };
}
