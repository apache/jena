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

package org.apache.jena.shex.expressions;

import java.util.function.Supplier;

import org.apache.jena.atlas.io.IndentedWriter;

/** Misc internal code */
public class Sx2 {
    public static boolean TRACE = false;

    public static IndentedWriter out = IndentedWriter.clone(IndentedWriter.stdout);

    public static void start(String method) {
        if ( TRACE ) {
            out.printf("> %s\n",method);
            out.incIndent();
        }
    }

    public static void finish(String label) {
        if ( TRACE ) {
            out.decIndent();
            out.printf("< %s\n",label);
        }
    }

    public static void run(String label, Runnable action) {
        if ( ! TRACE ) {
            action.run();
            return;
        }

        start(label);
        try {
            action.run();
        } finally {
            finish(label);
        }
    }

    public static <X> X runRtn(String label, Supplier<X> action) {
        if ( ! TRACE ) {
            return action.get();
        }
        start(label);
        try {
            return action.get();
        } finally {
            finish(label);
        }
    }

    public static void trace(String label, String fmt, Object ... args) {
        if ( TRACE ) {
            out.printf(fmt,  args);
            if ( ! fmt.endsWith("\n") )
                out.println();
        }
    }
}
