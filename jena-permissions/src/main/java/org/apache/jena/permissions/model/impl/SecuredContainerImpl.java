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
package org.apache.jena.permissions.model.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredContainer;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.utils.ContainerFilter;
import org.apache.jena.permissions.utils.PermStatementFilter;
import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Implementation of SecuredContainer to be used by a SecuredItemInvoker proxy.
 */
public class SecuredContainerImpl extends SecuredResourceImpl implements SecuredContainer {
    /**
     * Constructor
     * 
     * @param securedModel the Secured Model to use.
     * @param container    The container to secure.
     * @return The SecuredResource
     */
    public static SecuredContainer getInstance(final SecuredModel securedModel, final Container container) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("Container may not be null");
        }

        // check that resource has a securedModel.
        Container goodContainer = container;
        if (goodContainer.getModel() == null) {
            container.asNode();
            goodContainer = securedModel.createBag();
        }

        final ItemHolder<Container, SecuredContainer> holder = new ItemHolder<>(goodContainer);

        final SecuredContainerImpl checker = new SecuredContainerImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (goodContainer instanceof SecuredContainer) {
            if (checker.isEquivalent((SecuredContainer) goodContainer)) {
                return (SecuredContainer) goodContainer;
            }
        }

        return holder.setSecuredItem(new SecuredItemInvoker(container.getClass(), checker));

    }

    // the item holder that contains this SecuredContainer.
    private final ItemHolder<? extends Container, ? extends SecuredContainer> holder;

    /**
     * Constructor
     * 
     * @param securedModel the Secured Model to use.
     * @param holder       The item holder that will contain this SecuredContainer
     */
    protected SecuredContainerImpl(final SecuredModel securedModel,
            final ItemHolder<? extends Container, ? extends SecuredContainer> holder) {
        super(securedModel, holder);
        this.holder = holder;
    }

    /**
     * Returns the Object as an RDFNode. If it is a node return it otherwise convert
     * it as a literal
     *
     * @param o the object to convert.
     * @return an RDFNode
     */
    protected RDFNode asObject(Object o) {
        return o instanceof RDFNode ? (RDFNode) o : holder.getBaseItem().getModel().createTypedLiteral(o);
    }

    /**
     * Create an RDFNode (Literal) from a string value and language.
     *
     * @param value    the value
     * @param language the language
     * @return a Literal RDFNode.
     */
    protected RDFNode asLiteral(String value, String language) {
        return holder.getBaseItem().getModel().createLiteral(value, language);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final boolean o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final char o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final double o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final float o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final long o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final Object o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final RDFNode o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final int pos = holder.getBaseItem().size();
        checkAdd(pos, o.asNode());
        holder.getBaseItem().add(o);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final String o)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(o, "");
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer add(final String o, final String l)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        return add(asLiteral(o, l));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li, o );
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkAdd(final int pos, final Literal literal)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        checkAdd(pos, literal.asNode());
    }

    protected void checkAdd(final int pos, final Node node)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        checkCreate(Triple.create(holder.getBaseItem().asNode(), RDF.li(pos).asNode(), node));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final boolean o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final char o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final double o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final float o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final long o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Object o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final RDFNode o) throws ReadDeniedException, AuthenticationRequiredException {
        // iterator checks reads
        final SecuredNodeIterator<RDFNode> iter = iterator();
        try {
            while (iter.hasNext()) {
                if (iter.next().asNode().equals(o.asNode())) {
                    return true;
                }
            }
            return false;
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final String o) throws ReadDeniedException, AuthenticationRequiredException {
        return contains(o, "");
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple( this, RDF.li, o );
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final String o, final String l)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(asLiteral(o, l));
    }

    protected int getAddIndex() {
        int pos = -1;
        final ExtendedIterator<Statement> iter = holder.getBaseItem().listProperties();
        try {
            while (iter.hasNext()) {
                pos = Math.max(pos, getIndex(iter.next().getPredicate()));
            }
        } finally {
            iter.close();
        }
        return pos + 1;
    }

    protected static int getIndex(final Property p) {
        if (p.getNameSpace().equals(RDF.getURI()) && p.getLocalName().startsWith("_")) {
            try {
                return Integer.parseInt(p.getLocalName().substring(1));
            } catch (final NumberFormatException e) {
                // acceptable;
            }
        }
        return -1;
    }

    /**
     * An iterator of statements that have predicates that start with '_' followed
     * by a number and for which the user has the specified permission.
     *
     * @param perm the permission to check
     * @return an ExtendedIterator of statements.
     */
    protected ExtendedIterator<Statement> getStatementIterator(final Action perm) {
        return holder.getBaseItem().listProperties().filterKeep(new ContainerFilter())
                .filterKeep(new PermStatementFilter(perm, this));
    }

    /**
     * An iterator of statements that have predicates that start with '_' followed
     * by a number and for which the user has the specified permissions.
     *
     * @param perm the permissions to check
     * @return an ExtendedIterator of statements.
     */
    protected ExtendedIterator<Statement> getStatementIterator(final Set<Action> perm) {
        return holder.getBaseItem().listProperties().filterKeep(new ContainerFilter())
                .filterKeep(new PermStatementFilter(perm, this));
    }

    @Override
    public boolean isAlt() {
        return holder.getBaseItem().isAlt();
    }

    @Override
    public boolean isBag() {
        return holder.getBaseItem().isBag();
    }

    @Override
    public boolean isSeq() {
        return holder.getBaseItem().isSeq();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each triple ( this, rdf:li_? node ) returned by iterator;
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty iterator is
     *             returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredNodeIterator<RDFNode> iterator() {
        // listProperties calls checkRead();
        SecuredStatementIterator iter = listProperties();
        try {
            // List<Statement> ls = iter.toList();
            SortedSet<Statement> result = new TreeSet<>(new ContainerComparator());
            while (iter.hasNext()) {
                Statement stmt = iter.next();
                if (stmt.getPredicate().getOrdinal() > 0) {
                    result.add(stmt);
                }
            }
            return new SecuredNodeIterator<>(getModel(),
                    new StatementRemovingIterator(result.iterator()).mapWith(s -> s.getObject()));
        } finally {
            iter.close();
        }
    }

    /**
     * @param perms the Permissions required on each node returned
     * @sec.graph Read
     * @sec.triple Read + perms on each triple ( this, rdf:li_? node ) returned by
     *             iterator;
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty iterator is
     *             returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected SecuredNodeIterator<RDFNode> iterator(final Set<Action> perms) {
        checkRead();
        final Set<Action> permsCopy = new HashSet<>(perms);
        permsCopy.add(Action.Read);
        final ExtendedIterator<RDFNode> ni = getStatementIterator(perms).mapWith(o -> o.getObject());
        return new SecuredNodeIterator<>(getModel(), ni);

    }

    /**
     * @sec.graph Update
     * @sec.triple Delete s as triple;
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredContainer remove(final Statement s)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkDelete(s.asTriple());
        holder.getBaseItem().remove(s);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int size() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().size();
    }

    static class ContainerComparator implements Comparator<Statement> {

        @Override
        public int compare(Statement arg0, Statement arg1) {
            return Integer.valueOf(arg0.getPredicate().getOrdinal()).compareTo(arg1.getPredicate().getOrdinal());
        }

    }

    static class StatementRemovingIterator extends WrappedIterator<Statement> {
        private Statement stmt;

        public StatementRemovingIterator(Iterator<? extends Statement> base) {
            super(base);
        }

        @Override
        public Statement next() {
            stmt = super.next();
            return stmt;
        }

        @Override
        public void remove() {
            stmt.remove();
            super.remove();
        }
    }
}
