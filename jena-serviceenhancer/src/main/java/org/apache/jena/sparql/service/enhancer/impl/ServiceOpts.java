/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;

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
    // Use ':' as a separator unless it is preceeded by the escape char ':'
    // The regex matches the actual delimiter in group 1
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<!(sep))(?:(sep){2})*((sep))(?!(sep))"
            .replace("(sep)", ":"));

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
            String prefixStr = ServiceOpts.unparseOptions(options);
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

    public static List<Entry<String, String>> parseEntries(Node node) {
        return node.isURI() ? parseEntries(node.getURI()) : null;
    }

    /** Split an iri by ':' and attempt to parse the splits as key=value pairs. */
    public static List<String> parseEntriesRaw(String iri) {
        List<String> result = new ArrayList<>();
        Matcher matcher = SPLIT_PATTERN.matcher(iri);
        int nextEntryStart = 0;
        int group = 1; // The separator is in this group
        while (matcher.find()) {
            int delimStart = matcher.start(group);
            int delimEnd = matcher.end(group);
            if (delimStart > nextEntryStart) {
                String entryStr = iri.substring(nextEntryStart, delimStart);
                result.add(entryStr);
            }
            String delimStr = iri.substring(delimStart, delimEnd);
            result.add(delimStr);
            nextEntryStart = delimEnd;
        }
        // Add the entry after the last separator (if there is one)
        int n = iri.length();
        if (nextEntryStart < n) {
            String entryStr = iri.substring(nextEntryStart, n);
            result.add(entryStr);
        }
        return result;
    }

    /** Split an iri by ':' and attempt to parse the splits as key=value pairs. */
    public static List<Entry<String, String>> parseEntries(String iri) {
        List<String> rawSplits = parseEntriesRaw(iri);
        List<Entry<String, String>> result = rawSplits.stream().map(rawSplit -> {
            String split = rawSplit.equals(":") ? "" : rawSplit.replace("::", ":");
            String[] kv = split.split("\\+", 2);
            return new SimpleEntry<>(kv[0], kv.length == 2 ? kv[1] : null);
        }).collect(Collectors.toList());
        return result;
    }

    public static boolean isSeparator(String key) {
        return "".equals(key);
    }

    public static String escape(String str) {
        String result = isSeparator(str) ? ":" : str.replace(":", "::");
        return result;
    }

    /** Convert a list of entries back into an escaped string. */
    public static String unparseEntries(List<Entry<String, String>> entryList) {
        String result = entryList.stream()
            .map(ServiceOpts::unparseEntry)
            .collect(Collectors.joining());
        return result;
    }

    public static String unparseEntry(Entry<String, String> e) {
        String result = escape(e.getKey()) + Optional.ofNullable(e.getValue()).map(v -> "+" + escape(v)).orElse("");
        return result;
    }

    /**
     * Differs from {@link #unparseEntries(List)} that the input is expected to be free from separators.
     */
    public static String unparseOptions(List<Entry<String, String>> optionList) {
        String result = optionList.stream()
            .map(ServiceOpts::unparseEntry)
            .map(x -> x + ":")
            .collect(Collectors.joining());
        return result;
    }

    public static ServiceOpts getEffectiveService(OpService opService, String fallbackServiceIri, Predicate<String> isKnownOption) {
        List<Entry<String, String>> opts = new ArrayList<>();
        OpService currentOp = opService;
        boolean isSilent;
        String serviceStr = null;

        // The outer loop  descends into sub OpService instances as long as options are known
        // SERVICE <cache:loop:> { } is equivalent to SERVICE <cache:> { SERVICE <loop:> { } }
        while (true) {
            isSilent = currentOp.getSilent();
            Node node = currentOp.getService();
            List<Entry<String, String>> parts = ServiceOpts.parseEntries(node);
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
                if (isKnownOption.test(key)) {
                    opts.add(e);
                    // Skip over the next separator (if there is one)
                    int j = i + 1;
                    if (j < n) {
                        String nextKey = parts.get(j).getKey();
                        isSeparator(nextKey);
                        i = j;
                    }
                } else {
                    break;
                }
            }

            List<Entry<String, String>> subList = parts.subList(i, n);
            serviceStr = ServiceOpts.unparseEntries(subList);
            if (serviceStr.isEmpty()) {
                Op subOp = opService.getSubOp();
                if (subOp instanceof OpService) {
                    currentOp = (OpService)subOp;
                } else {
                    serviceStr = fallbackServiceIri;
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
