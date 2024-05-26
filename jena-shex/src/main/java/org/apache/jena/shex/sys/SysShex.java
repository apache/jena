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

package org.apache.jena.shex.sys;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shex.ShexValidator;
import org.apache.jena.shex.semact.SemanticActionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SysShex {

    public static boolean STRICT = true;

    public static String URI = "org.apache.jena.shex";
    public static Logger log = LoggerFactory.getLogger("org.apache.jena.shex.shex");

    // Node used for a START shape.
    public static Node startNode = NodeFactory.createExt("|start|");
    // Node used for FOCUS in a shape map.
    public static Node focusNode = NodeFactory.createExt("|focus|");

    private static Map<String, SemanticActionPlugin> semActPluginIndex;

    public static void registerSemActPlugin(String uri, SemanticActionPlugin plugin) {
        semActPluginIndex.put(uri, plugin);
    }

    // This is thread-safe.
    private static ShexValidator systemValiditor;

    /** Set the current system-wide {@link ShexValidator}. */
    public static void set(ShexValidator validator) { systemValiditor = validator; }

    public static ShexValidator get() {
        return systemValiditor;
    }

    static {
        semActPluginIndex = new ConcurrentHashMap<>();
        systemValiditor = new ShexValidatorImpl(semActPluginIndex);
    }

    public static ShexValidator getNew(Collection<SemanticActionPlugin> pz) {
        Map<String, SemanticActionPlugin> iriToPlugin = new ConcurrentHashMap<>();
        pz.forEach(p -> {
            p.getUris().forEach(u -> iriToPlugin.put(u, p));
        });
        ShexValidator ret = new ShexValidatorImpl(iriToPlugin);
        return ret;
    }
}
