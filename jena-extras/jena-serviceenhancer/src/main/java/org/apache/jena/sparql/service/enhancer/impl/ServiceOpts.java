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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;

/**
 * Utilities to exploit url scheme pattern to represent key value pairs.
 * <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC3986</a> only
 * allows for a very limited set of characters:
 * <pre>
 * scheme      = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
 * </pre>
 *
 * For this reason '+' is used where usually '=' would be used.
 * Separator is ':'.
 *
 * Examples:
 * <pre>
 * SERVICE &lt;cache:&gt; {} Trailing colon is needed to discriminate from relative IRIs. Resulting map: {cache=null}
 * SERVICE &lt;cache:bulk+20&gt; {cache=null, bulk=20}
 * </pre>
 */
public class ServiceOpts {
    // Use ':' as a separator unless it is preceeded by the escape char '\'
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<!(esc))(?:(esc){2})*(sep)"
            .replace("(esc)", ":")
            .replace("(sep)", ":"));

    // Service options
    public static final String SO_OPTIMIZE = "optimize";
    public static final String SO_CACHE = "cache";
    public static final String SO_BULK = "bulk";

    // Undo scoping of variables
    public static final String SO_LOOP = "loop";

    protected OpService opService;
    protected List<Entry<String, String>> options;

    public ServiceOpts(OpService opService, List<Entry<String, String>> options) {
        super();
        this.opService = opService;
        this.options = options;
    }

    public OpService getTargetService() {
        return opService;
    }

    public ServiceOpts copy() {
        return new ServiceOpts(opService, new ArrayList<>(options));
    }

    public void add(String key, String value) {
        options.add(new SimpleEntry<>(key, value));
    }

    /** Removes all occurrences of a key */
    public void removeKey(Object key) {
        Iterator<? extends Entry<?, ?>> it = options.iterator();
        while (it.hasNext()) {
            Entry<?, ?> e = it.next();
            if (Objects.equals(e.getKey(), key)) {
                it.remove();
            }
        }
    }

    public List<Entry<String, String>> getOptions() {
        return options;
    }

    public boolean containsKey(Object key) {
        boolean result = options.stream().anyMatch(e -> Objects.equals(e.getKey(), key));
        return result;
    }

    /**
     * Find a key's first value in the list of options
     *
     * @param key The key to find in the options
     * @param valueIfNull The value to return if the key is present with a null value
     * @param valueIfAbsent The value to return if the key is absent
     */
    public String getFirstValue(Object key, String valueIfNull, String valueIfAbsent) {
        String result = options.stream()
                .filter(e -> Objects.equals(e.getKey(), key))
                .map(e -> {
                    String r = e.getValue();
                    if (r == null) {
                        r = valueIfNull;
                    }
                    return r;
                })
                .findFirst()
                .orElse(valueIfAbsent);
        return result;
    }

    /** Encode the options as a OpService */
    public OpService toService() {
        OpService result;
        if (options.isEmpty()) {
            result = opService;
        } else {
            Node node = opService.getService();
            String prefixStr = ServiceOpts.unparse(options);
            if (!node.isURI()) {
                Node uri = NodeFactory.createURI(prefixStr);
                result = new OpService(uri, opService, false);
            } else {
                Node uri = NodeFactory.createURI(prefixStr + node.getURI());
                result = new OpService(uri, opService.getSubOp(), false);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "ServiceOpts [options=" + options + ", opService=" + opService + "]";
    }

    public static List<Entry<String, String>> parseAsOptions(Node node) {
        String iri = node.isURI() ? node.getURI() : null;
        List<Entry<String, String>> result = iri == null ? null : parseAsOptions(iri);
        return result;
    }

    /** Split an iri by ':' and attempt to parse the splits as key=value pairs. */
    public static List<Entry<String, String>> parseAsOptions(String iri) {
        List<Entry<String, String>> result = new ArrayList<>();
        String[] rawSplits = SPLIT_PATTERN.split(iri);
        for (String rawSplit : rawSplits) {
            String split = rawSplit.replace("\\\\", "\\");
            String[] kv = split.split("\\+", 2);
            result.add(new SimpleEntry<>(kv[0], kv.length == 2 ? kv[1] : null));
        }

        return result;
    }

    public static String escape(String str) {
        String result = str.replace("\\", "\\\\").replace(":", "\\:");
        return result;
    }

    /** Convert a list of options back into an escaped string */
    public static String unparse(List<Entry<String, String>> optionList) {
        String result = optionList.stream()
            .map(e -> escape(e.getKey()) + (Optional.ofNullable(e.getValue()).map(v -> "+" + escape(v)).orElse("")))
            .collect(Collectors.joining(":"));

        ListIterator<? extends Entry<String, String>> it = optionList.listIterator(optionList.size());
        if (it.hasPrevious()) {
            Entry<String, String> lastEntry = it.previous();
            if (isKnownOption(lastEntry.getKey())) {
                result += ":";
            }
        }

            //.collect(Collectors.joining(":"));
        return result;
    }

    public static boolean isKnownOption(String key) {
        Set<String> knownOptions = new LinkedHashSet<>();
        knownOptions.add(SO_CACHE);
        knownOptions.add(SO_BULK);
        knownOptions.add(SO_LOOP);
        knownOptions.add(SO_OPTIMIZE);

        return knownOptions.contains(key);
    }

    public static ServiceOpts getEffectiveService(OpService opService) {
        List<Entry<String, String>> opts = new ArrayList<>();
        OpService currentOp = opService;
        boolean isSilent;
        String serviceStr = null;

        while (true) {
            isSilent = currentOp.getSilent();
            Node node = currentOp.getService();
            List<Entry<String, String>> parts = ServiceOpts.parseAsOptions(node);

            if (parts == null) { // node is not an iri
                break;
            }

            // If there are only options then check whether to merge with a sub service op
            // If there is none then append 'self'
            int n = parts.size();
            int i = 0;
            for (; i < n; ++i) {
                Entry<String, String> e = parts.get(i);
                String key = e.getKey();

                if (isKnownOption(key)) {
                    opts.add(e);
                } else {
                    break;
                }
            }

            List<Entry<String, String>> subList = parts.subList(i, n);
            serviceStr = ServiceOpts.unparse(subList);
            if (serviceStr.isEmpty()) {
                Op subOp = opService.getSubOp();
                if (subOp instanceof OpService) {
                    currentOp = (OpService)subOp;
                } else {
                    serviceStr = ServiceEnhancerConstants.SELF.getURI();
                    break;
                }
            } else {
                break;
            }
        }

        ServiceOpts result = opts.isEmpty()
                ? new ServiceOpts(opService, opts)
                : new ServiceOpts(new OpService(NodeFactory.createURI(serviceStr), currentOp.getSubOp(), isSilent), opts);
        return result;
    }
}
