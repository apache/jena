/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.impl.CachedSecurityEvaluator;
import org.apache.jena.permissions.utils.PermTripleFilter;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * Since we sit between the graph and other items we have to determine when the
 * message is first seen and send it to the underlying graph if necessary.
 */
public class SecuredGraphEventManager implements GraphEventManager {
    private class SecuredGraphListener implements GraphListener {
        private final GraphListener wrapped;
        private final Object runAs;

        SecuredGraphListener(final GraphListener wrapped) {
            if (wrapped == null) {
                throw new IllegalArgumentException("Wrapped listener may not be null");
            }
            this.wrapped = wrapped;
            this.runAs = securedGraph.getSecurityEvaluator().getPrincipal();
        }

        private Triple[] getArray(final Graph g, final Triple[] triples, final Set<Action> perms)
                throws AuthenticationRequiredException {
            Triple[] retval = triples;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, perms, sg.getModelNode())) {
                    if (!evaluator.evaluateAny(runAs, perms, sg.getModelNode(), Triple.ANY)) {
                        final List<Triple> list = wrapPermIterator(sg, Arrays.asList(triples).iterator(), perms)
                                .toList();
                        retval = list.toArray(new Triple[list.size()]);
                    } else {
                        retval = triples;
                    }
                } else {
                    retval = new Triple[0];
                }
            }
            return retval;
        }

        @Override
        public void notifyAddArray(final Graph g, final Triple[] triples) throws AuthenticationRequiredException {
            final Triple[] added = getArray(g, triples, SecuredGraphEventManager.ADD);

            if (added.length > 0) {
                wrapped.notifyAddArray(g, added);
            }
        }

        @Override
        public void notifyAddGraph(final Graph g, final Graph added) throws AuthenticationRequiredException {
            Graph addGraph = added;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode())) {
                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode(), Triple.ANY)) {
                        final List<Triple> lst = added.find(Triple.ANY).toList();
                        addGraph = new CollectionGraph(Arrays.asList(
                                getArray(g, lst.toArray(new Triple[lst.size()]), SecuredGraphEventManager.ADD)));
                    } else {
                        addGraph = added;
                    }
                } else {
                    addGraph = new CollectionGraph(Collections.<Triple>emptyList());
                }
            }
            if (addGraph.size() > 0) {
                wrapped.notifyAddGraph(g, addGraph);

            }
        }

        @Override
        public void notifyAddIterator(final Graph g, final Iterator<Triple> it) throws AuthenticationRequiredException {

            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                // only report if we can write to the graph
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode())) {
                    final ExtendedIterator<Triple> iter = wrapPermIterator(sg, it, SecuredGraphEventManager.ADD);
                    try {
                        wrapped.notifyAddIterator(g, iter);
                    } finally {
                        iter.close();
                    }
                }
            } else {
                wrapped.notifyAddIterator(g, it);
            }

        }

        @Override
        public void notifyAddList(final Graph g, final List<Triple> triples) throws AuthenticationRequiredException {
            List<Triple> list = triples;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode())) {
                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode(), Triple.ANY)) {
                        list = wrapPermIterator(sg, triples.iterator(), SecuredGraphEventManager.ADD).toList();
                    } else {
                        list = triples;
                    }
                } else {
                    list = Collections.emptyList();
                }
            }

            if (list.size() > 0) {

                wrapped.notifyAddList(g, list);
            }
        }

        @Override
        public void notifyAddTriple(final Graph g, final Triple t) throws AuthenticationRequiredException {
            boolean notify = false;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                notify = evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode());
                if (notify) {
                    notify = evaluator.evaluateAny(runAs, SecuredGraphEventManager.ADD, sg.getModelNode(), t);
                }
            } else {
                notify = true;
            }
            if (notify) {
                wrapped.notifyAddTriple(g, t);
            }
        }

        @Override
        public void notifyDeleteArray(final Graph g, final Triple[] triples) throws AuthenticationRequiredException {
            Triple[] deleted = triples;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode())) {
                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode(), Triple.ANY)) {
                        final List<Triple> list = wrapPermIterator(sg, Arrays.asList(triples).iterator(),
                                SecuredGraphEventManager.DELETE).toList();
                        deleted = list.toArray(new Triple[list.size()]);
                    } else {
                        deleted = triples;
                    }
                } else {
                    deleted = new Triple[0];
                }
            }

            if (deleted.length > 0) {
                wrapped.notifyDeleteArray(g, deleted);
            }
        }

        @Override
        public void notifyDeleteGraph(final Graph g, final Graph removed) throws AuthenticationRequiredException {
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode())) {
                    Graph g2 = removed;
                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode(), Triple.ANY)) {
                        g2 = new CollectionGraph(removed.find(Triple.ANY)
                                .filterKeep(new PermTripleFilter(SecuredGraphEventManager.DELETE, sg, evaluator))
                                .toList());

                    }
                    wrapped.notifyDeleteGraph(g, g2);
                } else {
                    // do nothing.
                }
            } else {
                wrapped.notifyDeleteGraph(g, removed);
            }
        }

        @Override
        public void notifyDeleteIterator(final Graph g, final Iterator<Triple> it)
                throws AuthenticationRequiredException {
            Iterator<Triple> iter = it;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode())) {

                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode(), Triple.ANY)) {
                        iter = WrappedIterator.create(it)
                                .filterKeep(new PermTripleFilter(SecuredGraphEventManager.DELETE, sg, evaluator));
                    }
                    // else use the default list as all can bee seen
                    wrapped.notifyDeleteIterator(g, iter);
                } else {
                    // do nothing.
                }
            } else {
                wrapped.notifyDeleteIterator(g, iter);
            }

        }

        @Override
        public void notifyDeleteList(final Graph g, final List<Triple> triples) throws AuthenticationRequiredException {
            List<Triple> list = triples;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                if (evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode())) {
                    if (!evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode(), Triple.ANY)) {
                        list = WrappedIterator.create(triples.iterator())
                                .filterKeep(new PermTripleFilter(SecuredGraphEventManager.DELETE, sg, evaluator))
                                .toList();
                    }
                    // else use the default list as all can bee seen
                } else {
                    list = Collections.emptyList();
                }
            }

            if (list.size() > 0) {
                wrapped.notifyDeleteList(g, list);
            }
        }

        @Override
        public void notifyDeleteTriple(final Graph g, final Triple t) throws AuthenticationRequiredException {
            boolean notify = false;
            if (g instanceof SecuredGraph) {
                final SecuredGraph sg = (SecuredGraph) g;
                final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
                notify = evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode());
                if (notify) {
                    notify = evaluator.evaluateAny(runAs, SecuredGraphEventManager.DELETE, sg.getModelNode(), t);
                }
            } else {
                notify = true;
            }
            if (notify) {
                wrapped.notifyDeleteTriple(g, t);
            }
        }

        @Override
        public void notifyEvent(final Graph source, final Object value) {
            wrapped.notifyEvent(source, value);
        }

        private ExtendedIterator<Triple> wrapPermIterator(final SecuredGraph sg, final Iterator<Triple> it,
                final Set<Action> perms) throws AuthenticationRequiredException {
            final SecurityEvaluator evaluator = new CachedSecurityEvaluator(sg.getSecurityEvaluator(), runAs);
            if (!evaluator.evaluateAny(runAs, perms, sg.getModelNode(), Triple.ANY)) {
                // nope so wrap the iterator with security iterator
                return WrappedIterator.create(it).filterKeep(new PermTripleFilter(perms, sg, evaluator));
            }
            return WrappedIterator.create(it);
        }

    }

    // the security evaluator in use
    private final SecuredGraph securedGraph;
    private final Graph baseGraph;
    private final Map<GraphListener, Stack<SecuredGraphListener>> listenerMap = new HashMap<>();
    private static Set<Action> DELETE;
    private static Set<Action> ADD;

    static {
        SecuredGraphEventManager.ADD = new HashSet<Action>(Arrays.asList(Action.Create, Action.Read));
        SecuredGraphEventManager.DELETE = new HashSet<Action>(Arrays.asList(Action.Delete, Action.Read));
    }

    public SecuredGraphEventManager(final SecuredGraph securedGraph, final Graph baseGraph,
            final GraphEventManager manager) {
        this.securedGraph = securedGraph;
        this.baseGraph = baseGraph;
        manager.register(this);
    }

    private synchronized Collection<SecuredGraphListener> getListenerCollection() {
        ExtendedIterator<SecuredGraphListener> retval = NiceIterator.emptyIterator();
        for (final Collection<SecuredGraphListener> coll : listenerMap.values()) {
            retval = retval.andThen(coll.iterator());
        }
        return retval.toList();
    }

    @Override
    public boolean listening() {
        return !listenerMap.isEmpty();
    }

    @Override
    public void notifyAddArray(final Graph g, final Triple[] triples) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyAddArray(securedGraph, triples);
            } else {
                sgl.notifyAddArray(g, triples);
            }
        }
    }

    @Override
    public void notifyAddGraph(final Graph g, final Graph added) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyAddGraph(securedGraph, added);
            } else {
                sgl.notifyAddGraph(g, added);
            }
        }
    }

    @Override
    public void notifyAddIterator(final Graph g, final Iterator<Triple> it) throws AuthenticationRequiredException {
        notifyAddIterator(g, WrappedIterator.create(it).toList());
        baseGraph.equals(g);
    }

    @Override
    public void notifyAddIterator(final Graph g, final List<Triple> triples) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyAddIterator(securedGraph, triples.iterator());
            } else {
                sgl.notifyAddIterator(g, triples.iterator());
            }
        }
    }

    @Override
    public void notifyAddList(final Graph g, final List<Triple> triples) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyAddList(securedGraph, triples);
            } else {
                sgl.notifyAddList(g, triples);
            }
        }
    }

    @Override
    public void notifyAddTriple(final Graph g, final Triple t) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyAddTriple(securedGraph, t);
            } else {
                sgl.notifyAddTriple(g, t);
            }
        }
    }

    @Override
    public void notifyDeleteArray(final Graph g, final Triple[] triples) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyDeleteArray(securedGraph, triples);
            } else {
                sgl.notifyDeleteArray(g, triples);
            }
        }
    }

    @Override
    public void notifyDeleteGraph(final Graph g, final Graph removed) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyDeleteGraph(securedGraph, removed);
            } else {
                sgl.notifyDeleteGraph(g, removed);
            }
        }
    }

    @Override
    public void notifyDeleteIterator(final Graph g, final Iterator<Triple> it) throws AuthenticationRequiredException {
        notifyDeleteIterator(g, WrappedIterator.create(it).toList());
    }

    @Override
    public void notifyDeleteIterator(final Graph g, final List<Triple> triples) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyDeleteIterator(securedGraph, triples.iterator());
            } else {
                sgl.notifyDeleteIterator(g, triples.iterator());
            }
        }
    }

    @Override
    public void notifyDeleteList(final Graph g, final List<Triple> L) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyDeleteList(securedGraph, L);
            } else {
                sgl.notifyDeleteList(g, L);
            }
        }
    }

    @Override
    public void notifyDeleteTriple(final Graph g, final Triple t) throws AuthenticationRequiredException {
        final boolean wrap = baseGraph.equals(g);

        for (final SecuredGraphListener sgl : getListenerCollection()) {
            if (wrap) {
                sgl.notifyDeleteTriple(securedGraph, t);
            } else {
                sgl.notifyDeleteTriple(g, t);
            }
        }
    }

    @Override
    public void notifyEvent(final Graph source, final Object value) throws AuthenticationRequiredException {
        if ((source instanceof SecuredGraph) && securedGraph.equals(source)) {
            baseGraph.getEventManager().notifyEvent(baseGraph, value);
        } else {

            final boolean wrap = baseGraph.equals(source);

            for (final SecuredGraphListener sgl : getListenerCollection()) {
                if (wrap) {
                    sgl.notifyEvent(securedGraph, value);
                } else {
                    sgl.notifyEvent(source, value);
                }
            }
        }
    }

    @Override
    public synchronized GraphEventManager register(final GraphListener listener) {
        Stack<SecuredGraphListener> sgl = listenerMap.get(listener);
        if (sgl == null) {
            sgl = new Stack<>();
        }
        sgl.push(new SecuredGraphListener(listener));
        listenerMap.put(listener, sgl);
        return this;
    }

    @Override
    public synchronized GraphEventManager unregister(final GraphListener listener) {
        final Stack<SecuredGraphListener> sgl = listenerMap.get(listener);
        if (sgl != null) {
            if (sgl.size() == 1) {
                listenerMap.remove(listener);
            } else {
                sgl.pop();
                listenerMap.put(listener, sgl);
            }
        }
        return this;
    }

}
