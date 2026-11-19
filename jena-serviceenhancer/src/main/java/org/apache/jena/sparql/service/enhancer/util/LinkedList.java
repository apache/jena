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


package org.apache.jena.sparql.service.enhancer.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A doubly linked list for keeping track of a sequence of items,
 * with O(1) insertion and deletion (using {@link LinkedListNode#unlink()}).
 * Nodes are created with {@link #newNode()} and are owned by the creating list.
 *
 * Use {@link #append(Object)} to add an item at the end of the list and obtain a corresponding {@link LinkedListNode} instance.
 * Use {@link LinkedListNode#unlink()} to remove a specific node from the list
 * and {@link LinkedListNode#moveToEnd()} to (re-)link a node as the last item of the list.
 *
 * The list is not thread-safe.
 */
public class LinkedList<T>
    extends AbstractList<T>
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static class LinkedListNode<T>
        implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private volatile T value;
        private volatile LinkedListNode<T> prev;
        private volatile LinkedListNode<T> next;
        private final LinkedList<T> list;

        private LinkedListNode(LinkedList<T> list) {
            this(list, null);
        }
        private LinkedListNode(LinkedList<T> list,T value) {
            super();
            this.list = list;
            this.value = value;
        }
        public T getValue() {
            return value;
        }
        public void setValue(T value) {
            this.value = value;
        }
        public LinkedListNode<T> getPrev() {
            return prev;
        }
        public LinkedListNode<T> getNext() {
            return next;
        }
        public LinkedList<T> getList() {
            return list;
        }
        /**
         * A node is linked if either:
         * <ul>
         *   <li>prev or next are non-null</li>
         *   <li>the node is referenced by list.first</li>
         * </ul>
         *
         * Conversely, a node is unlinked if prev and next are both null, and
         * this node is not referenced by list.first.
         */
        public boolean isLinked() {
            return prev != null || next != null || list.first == this;
        }
        public void unlink() {
            list.unlink(this);
        }
        public void moveToEnd() {
            list.moveToEnd(this);
        }
        @Override
        public String toString() {
            return "LinkedListNode [value=" + value + "]";
        }
    }

    private volatile LinkedListNode<T> first;
    private volatile LinkedListNode<T> last;
    private volatile int size;

    public LinkedList() {
        super();
    }

    public LinkedList(Collection<? extends T> items) {
        super();
        items.forEach(this::append);
    }

    public LinkedListNode<T> getFirstNode() {
        return first;
    }

    public LinkedListNode<T> getLastNode() {
        return last;
    }

    /** Create a new unlinked node. The node can only be inserted into this list. */
    public LinkedListNode<T> newNode() {
        return newNode(null);
    }

    public LinkedListNode<T> newNode(T value) {
        return new LinkedListNode<>(this, value);
    }

    /** Use {@link #append(Object)} to add a value and obtain its linked list node. */
    @Override
    public boolean add(T value) {
        append(value);
        return true;
    }

    public LinkedListNode<T> append(T value) {
        LinkedListNode<T> result = newNode();
        result.value = value;
        moveToEnd(result);
        return result;
    }

    public void moveToEnd(LinkedListNode<T> node) {
        unlink(node);
        if (first == null) {
            first = node;
        } else {
            last.next = node;
            node.prev = last;
        }
        last = node;
        ++size;
    }

    /** Add node after the insert point. If the insert point is null then the node becomes first. */
    public void addAfter(LinkedListNode<T> insertPoint, LinkedListNode<T> node) {
        checkOwner(insertPoint);
        if (insertPoint == null) {
            if (first == null) {
                // Insert as first
                checkOwner(node); // node cannot be linked - if it was then first would not be null
                first = node;
                last = node;
            } else {
                // Insert before first
                unlink(node);
                LinkedListNode<T> tmp = first;
                tmp.prev = node;
                node.next = tmp;
                first = node;
            }
        } else {
            if (insertPoint.next != node) {
                unlink(node);

                LinkedListNode<T> tmp = insertPoint.next;
                insertPoint.next = node;
                node.prev = insertPoint;

                if (tmp != null) {
                    tmp.prev = node;
                    node.next = tmp;
                }

                if (last == insertPoint) {
                    last = node;
                }
            }
        }
    }

    private void checkOwner(LinkedListNode<T> node) {
        if (node.list != this) {
            throw new IllegalArgumentException("Cannot unlink a node that does not belong to this list.");
        }
    }

    private void unlink(LinkedListNode<T> node) {
        checkOwner(node);
        if (node.isLinked()) {
            if (node == first) {
                first = node.next;
            }
            if (node == last) {
                last = node.prev;
            }
            if (node.prev != null) {
                node.prev.next = node.next;
            }
            if (node.next != null) {
                node.next.prev = node.prev;
            }
            node.prev = null;
            node.next = null;
            --size;
        }
    }

    @Override
    public int size() {
        return size;
    }

    private LinkedListNode<T> findNode(int index) {
        int s = size();
        if (index < 0 || index >= s) {
            throw new IndexOutOfBoundsException();
        }
        int halfSize = s >> 1;
        LinkedListNode<T> node;
        int i;
        if (index <= halfSize) {
            node = this.first;
            for (i = 0; i < index; ++i) {
                node = node.next;
            }
        } else {
            node = this.last;
            for (i = s - 1; i > index; --i) {
                node = node.prev;
            }
        }
        return node;
    }

    @Override
    public T get(int index) {
        LinkedListNode<T> node = findNode(index);
        return node.value;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> result;
        int s = size();
        if (index == s) { // Special case to position after the last element
            LinkedListNode<T> node = getLastNode();
            result = new LinkedListIterator(node, s, false);
        } else {
            LinkedListNode<T> node = findNode(index);
            result = new LinkedListIterator(node, index, true);
        }
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator(0);
    }

    protected class LinkedListIterator
        implements ListIterator<T> {

        // The pointer to the last returned node - null if absent.
        protected LinkedListNode<T> removable;

        // The next node to be returned by next()
        protected LinkedListNode<T> current;
        protected boolean isForward;
        protected int currentIndex;

        public LinkedListIterator(LinkedListNode<T> current, int currentIndex, boolean isForward) {
            super();
            this.current = current;
            this.isForward = isForward;
            this.currentIndex = currentIndex;
        }

        protected void ensureValid(LinkedListNode<T> node) {
            Objects.requireNonNull(node);
            if (!node.isLinked()) {
                throw new IllegalStateException("Linked list iterator points to an unlinked node.");
            }
        }

        @Override
        public boolean hasNext() {
            return current != null && (isForward || current.getNext() != null);
        }

        private void prepareForwardStep() {
            if (!isForward) {
                ensureValid(current);
                LinkedListNode<T> next = current.getNext();
                if (next != null) {
                    current = next;
                } else {
                    throw new NoSuchElementException();
                }
                isForward = true;
            }
        }

        private void prepareBackwardStep() {
            if (isForward) {
                ensureValid(current);
                LinkedListNode<T> prev = current.getPrev();
                if (prev != null) {
                    current = prev;
                } else {
                    throw new NoSuchElementException();
                }
                isForward = false;
            }
        }

        @Override
        public T next() {
            prepareForwardStep();
            ensureValid(current);
            removable = current;
            LinkedListNode<T> next = current.getNext();
            if (next != null) {
                current = next;
            } else {
                isForward = false;
            }
            ++currentIndex;
            return removable.getValue();
        }

        @Override
        public void remove() {
            Objects.requireNonNull(removable, "Linked list iterator is not positioned at a removable element.");
            unlink(removable);
            removable = null;
        }

        @Override
        public boolean hasPrevious() {
            return current != null && (!isForward || current.getPrev() != null);
        }

        @Override
        public T previous() {
            prepareBackwardStep();
            ensureValid(current);
            removable = current;
            LinkedListNode<T> prev = current.getPrev();
            if (prev != null) {
                current = prev;
            } else {
                isForward = true;
            }
            --currentIndex;
            return removable.getValue();
        }

        @Override
        public int nextIndex() {
            return currentIndex;
        }

        @Override
        public int previousIndex() {
            return currentIndex - 1;
        }

        @Override
        public void set(T e) {
            ensureValid(current);
            current.setValue(e);
        }

        @Override
        public void add(T e) {
            prepareForwardStep();
            ensureValid(current);
            LinkedListNode<T> node = newNode(e);
            LinkedListNode<T> insertPoint = current.getPrev();
            addAfter(insertPoint, node);
            current = node;
            removable = null;
        }
    }
}
