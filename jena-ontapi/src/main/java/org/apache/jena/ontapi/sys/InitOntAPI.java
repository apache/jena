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

package org.apache.jena.ontapi.sys;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.ontapi.assemblers.DocumentGraphRepositoryAssembler;
import org.apache.jena.ontapi.assemblers.OA;
import org.apache.jena.ontapi.assemblers.OntModelAssembler;
import org.apache.jena.ontapi.assemblers.OntReasonerFactoryAssembler;
import org.apache.jena.ontapi.assemblers.OntRuleSetAssembler;
import org.apache.jena.ontapi.assemblers.OntSpecificationAssembler;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitOntAPI implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        Initializer.init();
    }

    @Override
    public void stop() {
    }

    @Override
    public int level() {
        return 15;
    }

    private static class Initializer {
        static volatile boolean initialized = false;

        static void init() {
            if (initialized)
                return;
            synchronized (Initializer.class) {
                if (initialized)
                    return;
                initialized = true;
                AssemblerUtils.init();
                Assembler.general().implementWith(OA.DocumentGraphRepository, new DocumentGraphRepositoryAssembler());
                Assembler.general().implementWith(OA.OntSpecification, new OntSpecificationAssembler());
                Assembler.general().implementWith(OA.ReasonerFactory, new OntReasonerFactoryAssembler());
                Assembler.general().implementWith(OA.RuleSet, new OntRuleSetAssembler());
                Assembler.general().implementWith(OA.OntModel, new OntModelAssembler());
            }
        }
    }
}
