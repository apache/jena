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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;

public class VarUtilsExtra {
    /**
     * Allocate a variable whose name is not in black list
     *
     * @param baseName The desired name. If it is contained in the set of excluded vars
     * then repeated attempts
     * with the name pattern "baseName_counter" are made until successful.
     * @param excludedVars The set of excluded Var instances
     * @return The fresh variable
     */
    public static Var freshVar(String baseName, Collection<Var> excludedVars) {
        Var result = Var.alloc(baseName);
        int i = 0;
        while (excludedVars.contains(result)) {
            result = Var.alloc(baseName + "_" + ++i);
        }

        return result;
    }
}
