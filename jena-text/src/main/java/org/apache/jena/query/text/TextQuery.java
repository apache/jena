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

package org.apache.jena.query.text;

import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.jena.query.text.cmd.InitTextCmds;
import org.apache.jena.sparql.pfunction.PropertyFunction ;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;

public class TextQuery
{
    private static volatile boolean initialized = false ;
    private static final Object lock              = new Object() ;

    public static final String NS                 = "http://jena.apache.org/text#" ;
    public static final String IRI                = "http://jena.apache.org/#text" ;

    public static final Symbol textIndex    = Symbol.create(NS+"index") ;
    public static final String PATH         = "org.apache.jena.query.text";

    static { JenaSystem.init(); }

    public static void init()
    {
        if ( initialized )
            return ;
        synchronized(lock) {
            if ( initialized ) {
                JenaSystem.logLifecycle("TextQuery.init - skip") ;
                return ;
            }
            initialized = true ;
            JenaSystem.logLifecycle("TextQuery.init - start") ;
            TextAssembler.init() ;

            PropertyFunctionRegistry.get().put("http://jena.apache.org/text#query", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new TextQueryPF() ;
                }
            });
            JenaSystem.logLifecycle("TextQuery.init - finish") ;
            // Register indirections.
            InitTextCmds.cmds();
        }
    }
}


