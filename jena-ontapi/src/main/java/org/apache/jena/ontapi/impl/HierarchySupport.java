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

package org.apache.jena.ontapi.impl;

import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to handle resource hierarchy.
 */
public final class HierarchySupport {

    /**
     * Answers {@code true} if the specified {@code test} node is in the closure of the specified {@code root} nodes
     *
     * @param root                       the root of tree
     * @param test                       object to test
     * @param listChildren               a {@link Function} that provides {@code Stream} of child nodes for the given parent node
     * @param direct                     if {@code true}, only return the direct (adjacent) values
     * @param useBuiltinHierarchySupport if {@code true} collect a nodes' tree by traversing the graph,
     *                                   this parameter is used when there is no reasoner attached to the graph
     * @param <X>                        any subtype of {@link OntObject}
     * @return boolean
     */
    public static <X extends OntObject> boolean contains(
            X root,
            X test,
            Function<X, Stream<X>> listChildren,
            boolean direct,
            boolean useBuiltinHierarchySupport) {
        if (direct) {
            return hasDirectNode(root, test, useBuiltinHierarchySupport, listChildren);
        }
        if (useBuiltinHierarchySupport) {
            return hasIndirectNode(root, test, listChildren);
        }
        return !root.equals(test) && listChildren.apply(root).anyMatch(test::equals);
    }

    /**
     * Lists tree nodes for the given root using {@code listChildren} function, which provides child nodes.
     *
     * @param root                       the root of tree
     * @param listChildren               a {@link Function} that provides {@code Stream} of child nodes for the given parent node
     * @param direct                     if {@code true}, only return the direct (adjacent) values
     * @param useBuiltinHierarchySupport if {@code true} collect a nodes' tree by traversing the graph,
     *                                   this parameter is used when there is no reasoner attached to the graph
     * @param <X>                        any subtype of {@link OntObject}
     * @return a {@link Stream} of tree nodes
     */
    public static <X extends OntObject> Stream<X> treeNodes(
            X root,
            Function<X, Stream<X>> listChildren,
            boolean direct,
            boolean useBuiltinHierarchySupport) {
        if (direct) {
            return directNodesAsStream(root, useBuiltinHierarchySupport, listChildren);
        }
        if (useBuiltinHierarchySupport) {
            return indirectNodesAsStream(root, listChildren);
        }
        return listChildren.apply(root).filter(x -> !root.equals(x));
    }

    /**
     * For the given object returns a {@code Set} of objects the same type,
     * that are its (direct or indirect) children which is determined by the operation {@code listChildren}.
     *
     * @param root         {@code X}
     * @param listChildren a {@code Function} that returns {@code Iterator} for an object of type {@code X}
     * @param <X>          any subtype of {@link Resource}
     * @return {@code Set} of {@code X}, {@code root} is not included
     */
    static <X extends Resource> Stream<X> indirectNodesAsStream(X root, Function<X, Stream<X>> listChildren) {
        return Iterators.fromSet(() -> {
            Set<X> res = new HashSet<>();
            Function<X, Set<X>> getChildren = it -> listChildren.apply(it).collect(Collectors.toSet());
            collectIndirect(root, getChildren, res);
            res.remove(root);
            return res;
        });
    }

    static <X extends Resource> boolean hasIndirectNode(X root, X test, Function<X, Stream<X>> listChildren) {
        if (root.equals(test)) {
            return false;
        }
        Set<X> seen = new HashSet<>();
        Deque<X> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            X next = queue.removeFirst();
            if (!seen.add(next)) {
                continue;
            }
            try (Stream<X> children = listChildren.apply(next)) {
                Iterator<X> it = children.iterator();
                while (it.hasNext()) {
                    X child = it.next();
                    if (child.equals(test)) {
                        return true;
                    }
                    queue.add(child);
                }
            }
        }
        return false;
    }

    /**
     * Returns a forest (collection of indirect node trees) for the given roots.
     *
     * @param listRoots    {@code Supplier<Stream<X>>} roots provider
     * @param listChildren {@code Function<X, Stream<X>>} called for each root
     * @param <X>          any subtype of {@link Resource}
     * @return {@code Set} of {@code X} including roots
     */
    public static <X extends Resource> Set<X> allTreeNodesSetInclusive(
            Supplier<Stream<X>> listRoots,
            Function<X, Stream<X>> listChildren) {
        @SuppressWarnings("DuplicatedCode") Set<X> res = new HashSet<>();
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Function<X, Set<X>> getChildren = it -> getChildren(it, listChildren, childrenNodesCache);
        try (Stream<X> roots = listRoots.get()) {
            roots.forEach(root -> collectIndirect(root, getChildren, res));
        }
        return res;
    }

    /**
     * For the given object recursively collects all children determined by the operation {@code listChildren}.
     *
     * @param root        {@code X}
     * @param getChildren a {@code Function} that returns {@code Set} explicit children of an object of type {@code X}
     * @param res         {@code Set} to store result
     * @param <X>         any subtype of {@link Resource}
     */
    static <X extends Resource> void collectIndirect(X root,
                                                     Function<X, Set<X>> getChildren,
                                                     Set<X> res) {
        Deque<X> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            X next = queue.removeFirst();
            if (!res.add(next)) {
                continue;
            }
            queue.addAll(getChildren.apply(next));
        }
    }

    public static <X extends Resource> Stream<X> directNodesAsStream(X object,
                                                                     boolean useBuiltinHierarchySupport,
                                                                     Function<X, Stream<X>> listChildren) {
        return Iterators.fromSet(() ->
                useBuiltinHierarchySupport ? directNodesAsSetWithBuiltinInf(object, listChildren) : directNodesAsSetStandard(object, listChildren)
        );
    }

    public static <X extends Resource> boolean hasDirectNode(X object,
                                                             X test,
                                                             boolean useBuiltinHierarchySupport,
                                                             Function<X, Stream<X>> listChildren) {
        return useBuiltinHierarchySupport ?
                hasDirectNodeWithBuiltinInf(object, test, listChildren) :
                hasDirectNodeStandard(object, test, listChildren);
    }

    public static <X extends Resource> Set<X> directNodesAsSetStandard(X root,
                                                                       Function<X, Stream<X>> listChildren) {
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Function<X, Set<X>> getChildren = it -> getChildren(it, listChildren, childrenNodesCache);
        return getChildren.apply(root).stream()
                .filter(it -> !equivalent(it, root, getChildren) && !hasAnotherPath(it, root, getChildren))
                .collect(Collectors.toSet());
    }

    public static <X extends Resource> Set<X> directNodesAsSetWithBuiltinInf(X root,
                                                                             Function<X, Stream<X>> listChildren) {
        Map<X, Node<X>> tree = collectTree(root, listChildren);
        Node<X> theRoot = tree.get(root);
        return theRoot.childrenWithEquivalents()
                .flatMap(it -> collectDirect(theRoot, it))
                .collect(Collectors.toSet());
    }

    public static <X extends Resource> boolean hasDirectNodeStandard(X root,
                                                                     X test,
                                                                     Function<X, Stream<X>> listChildren) {
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Function<X, Set<X>> getChildren = it -> getChildren(it, listChildren, childrenNodesCache);
        return getChildren.apply(root).stream()
                .anyMatch(it -> test.equals(it) && !equivalent(it, root, getChildren) && !hasAnotherPath(it, root, getChildren));
    }

    public static <X extends Resource> boolean hasDirectNodeWithBuiltinInf(X root,
                                                                           X test,
                                                                           Function<X, Stream<X>> listChildren) {
        Map<X, Node<X>> tree = collectTree(root, listChildren);
        Node<X> theRoot = tree.get(root);
        return theRoot.childrenWithEquivalents().anyMatch(it -> hasDirectNode(theRoot, it, test));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static <X extends Resource> boolean hasAnotherPath(X given,
                                                               X root,
                                                               Function<X, Set<X>> getChildren) {
        return getChildren.apply(root).stream()
                .filter(it -> !equivalent(it, root, getChildren))
                .flatMap(it ->
                        getChildren.apply(it).stream().filter(x -> !equivalent(x, it, getChildren))
                )
                .anyMatch(given::equals);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static <X extends Resource> boolean equivalent(X left, X right, Function<X, Set<X>> getChildren) {
        return getChildren.apply(right).contains(left) && getChildren.apply(left).contains(right);
    }

    private static <X extends Resource> Stream<X> collectDirect(Node<X> rootNode, Node<X> current) {
        Set<X> equivalents = current.equivalents();
        if (!equivalents.contains(rootNode.node)) {
            Set<X> siblings = new HashSet<>(equivalents);
            siblings.remove(current.node);
            if (current.hasMoreThanOnePathTo(rootNode, siblings)) {
                return Stream.empty();
            } else {
                equivalents.add(current.node);
                return equivalents.stream();
            }
        } else {
            return Stream.empty();
        }
    }

    private static <X extends Resource> boolean hasDirectNode(Node<X> rootNode, Node<X> current, X test) {
        Set<X> equivalents = current.equivalents();
        if (!equivalents.contains(rootNode.node)) {
            Set<X> siblings = new HashSet<>(equivalents);
            siblings.remove(current.node);
            if (current.hasMoreThanOnePathTo(rootNode, siblings)) {
                return false;
            } else {
                return current.node.equals(test) || equivalents.contains(test);
            }
        } else {
            return false;
        }
    }

    private static <X extends Resource> Map<X, Node<X>> collectTree(X root, Function<X, Stream<X>> listChildren) {
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Map<X, Node<X>> res = new HashMap<>();
        Set<X> visited = new HashSet<>();
        Deque<X> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            X next = queue.removeFirst();
            if (!visited.add(next)) {
                continue;
            }
            Set<X> nextChildren = getChildren(next, listChildren, childrenNodesCache);
            Node<X> nextNode = res.computeIfAbsent(next, Node::new);
            nextChildren.forEach(child -> {
                Node<X> childNode = res.computeIfAbsent(child, Node::new);
                nextNode.children.add(childNode);
                queue.add(child);
            });
        }
        return res;
    }

    private static <X extends Resource> Set<X> getChildren(
            X root,
            Function<X, Stream<X>> listChildren,
            Map<X, Set<X>> childrenNodesCache
    ) {
        return childrenNodesCache.computeIfAbsent(root, it -> {
            try (Stream<X> children = listChildren.apply(it)) {
                return children.collect(Collectors.toSet());
            }
        });
    }

    /**
     * Auxiliary class, tree node that is used for builtin hierarchy support.
     *
     * @param <X> resource
     */
    private static class Node<X extends Resource> {
        final X node;
        final Set<Node<X>> children = new HashSet<>();

        Node(X node) {
            this.node = node;
        }

        Stream<Node<X>> childrenWithEquivalents() {
            Set<X> equivalents = this.equivalents();
            return children.stream().flatMap(ch -> {
                if (equivalents.contains(ch.node)) {
                    return ch.children.stream().filter(ech -> !ech.equals(Node.this));
                } else {
                    return Stream.of(ch);
                }
            });
        }

        boolean hasMoreThanOnePathTo(Node<X> given, Set<X> exclude) {
            Deque<Node<X>> queue = new ArrayDeque<>();
            Set<X> visited = new HashSet<>();
            int res = 0;
            Iterator<Node<X>> firstLevelChildren = given.childrenWithEquivalents().iterator();
            while (firstLevelChildren.hasNext()) {
                Node<X> child = firstLevelChildren.next();
                if (exclude.contains(child.node)) {
                    continue;
                }
                if (child.node.equals(this.node)) {
                    res++;
                } else {
                    queue.add(child);
                }
            }
            while (!queue.isEmpty()) {
                Node<X> next = queue.removeFirst();
                if (exclude.contains(next.node)) {
                    continue;
                }
                if (next.node.equals(given.node)) {
                    continue;
                }
                if (next.node.equals(this.node)) {
                    if (++res > 1) {
                        return true;
                    }
                }
                if (!visited.add(next.node)) {
                    continue;
                }
                next.childrenWithEquivalents().forEach(queue::add);
            }
            return false;
        }

        Set<X> equivalents() {
            Deque<Node<X>> queue = new ArrayDeque<>();
            queue.add(this);
            Set<X> visited = new HashSet<>();
            Map<X, Set<X>> paths = new HashMap<>();
            Set<X> res = new HashSet<>();
            while (!queue.isEmpty()) {
                Node<X> next = queue.removeFirst();
                Set<X> nextPaths = paths.computeIfAbsent(next.node, it -> new HashSet<>());
                if (!nextPaths.isEmpty() && next.node.equals(this.node)) {
                    // cycle, all nodes in cycle are equivalent
                    res.addAll(nextPaths);
                    nextPaths.forEach(p -> {
                        Set<X> other = paths.get(p);
                        if (other != null) {
                            res.addAll(other);
                        }
                    });
                    continue;
                }
                if (!visited.add(next.node)) {
                    continue;
                }
                next.children.forEach(child -> {
                    Set<X> childPaths = paths.computeIfAbsent(child.node, it -> new HashSet<>());
                    childPaths.add(next.node);
                    childPaths.addAll(nextPaths);
                    queue.add(child);
                });
            }
            return res;
        }

        @Override
        public String toString() {
            return node.asNode().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            return node.equals(((Node<?>) o).node);
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }
    }

}
