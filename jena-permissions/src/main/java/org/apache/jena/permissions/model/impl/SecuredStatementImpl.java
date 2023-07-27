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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemImpl;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredAlt;
import org.apache.jena.permissions.model.SecuredBag;
import org.apache.jena.permissions.model.SecuredLiteral;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredProperty;
import org.apache.jena.permissions.model.SecuredRDFList;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.permissions.model.SecuredSeq;
import org.apache.jena.permissions.model.SecuredStatement;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * Implementation of SecuredStatement to be used by a SecuredItemInvoker proxy.
 */
public class SecuredStatementImpl extends SecuredItemImpl implements SecuredStatement {
    /**
     * get a SecuredStatement
     *
     * @param securedModel The secured model that provides the security context
     * @param stmt         The statement to secure.
     * @return the SecuredStatement
     */
    public static SecuredStatement getInstance(final SecuredModel securedModel, final Statement stmt) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (stmt == null) {
            throw new IllegalArgumentException("Statement may not be null");
        }

        final ItemHolder<Statement, SecuredStatement> holder = new ItemHolder<>(stmt);

        final SecuredStatementImpl checker = new SecuredStatementImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (stmt instanceof SecuredStatement) {
            if (checker.isEquivalent((SecuredStatement) stmt)) {
                return (SecuredStatement) stmt;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(holder.getBaseItem().getClass(), checker));
    }

    // the item holder that contains this SecuredStatement.
    private final ItemHolder<Statement, SecuredStatement> holder;

    private final SecuredModel securedModel;

    /**
     * Constructor.
     *
     * @param securityEvaluator The security evaluator to use.
     * @param graphIRI          the graph IRI to verify against.
     * @param holder            The item holder that will contain this
     *                          SecuredStatement.
     */
    private SecuredStatementImpl(final SecuredModel securedModel,
            final ItemHolder<Statement, SecuredStatement> holder) {
        super(securedModel, holder);
        this.holder = holder;
        this.securedModel = securedModel;
    }

    @Override
    public Triple asTriple() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        final Triple retval = holder.getBaseItem().asTriple();
        checkRead(retval);
        return retval;
    }

    @Override
    public boolean canCreate() throws AuthenticationRequiredException {
        return super.canCreate() && canCreate(holder.getBaseItem());
    }

    @Override
    public boolean canDelete() throws AuthenticationRequiredException {
        return super.canDelete() && canDelete(holder.getBaseItem());
    }

    @Override
    public boolean canRead() throws AuthenticationRequiredException {
        return super.canRead() && canRead(holder.getBaseItem());
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final boolean o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final char o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final double o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final float o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final int o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeLiteralObject(final long o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeLiteralObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeObject(final RDFNode o)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = Triple.create(base.getSubject(), base.getPredicate(), o.asNode());
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeObject(final String o) throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = getNewTriple(base, o);
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement changeObject(final String o, final String l)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple base = holder.getBaseItem().asTriple();
        final Triple newBase = Triple.create(base.getSubject(), base.getPredicate(),
                NodeFactory.createLiteral(o, l));
        checkUpdate(base, newBase);
        return SecuredStatementImpl.getInstance(getModel(), holder.getBaseItem().changeObject(o, l));
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredAlt getAlt() {
        return SecuredAltImpl.getInstance(getModel(), holder.getBaseItem().getAlt());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag getBag() {
        return SecuredBagImpl.getInstance(getModel(), holder.getBaseItem().getBag());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq getSeq() {
        return SecuredSeqImpl.getInstance(getModel(), holder.getBaseItem().getSeq());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList getList() {
        return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem().getList());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean getBoolean() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getBoolean();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public byte getByte() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getByte();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public char getChar() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getChar();

    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public double getDouble() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getDouble();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public float getFloat() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getFloat();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int getInt() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getInt();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getLanguage() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getLiteral().getLanguage();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredLiteral getLiteral() {
        return SecuredLiteralImpl.getInstance(getModel(), holder.getBaseItem().getLiteral());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public long getLong() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getLong();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel getModel() {
        return securedModel;
    }

    private Triple getNewTriple(final Triple t, final Object o) {
        return Triple.create(t.getSubject(), t.getPredicate(), NodeFactory.createLiteral(String.valueOf(o), ""));
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFNode getObject() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        final RDFNode rdfNode = holder.getBaseItem().getObject();
        return SecuredRDFNodeImpl.getInstance(getModel(), rdfNode);

    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredProperty getPredicate() {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return SecuredPropertyImpl.getInstance(getModel(), holder.getBaseItem().getPredicate());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getProperty(final Property p) throws AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        final StmtIterator s = holder.getBaseItem().getModel()
                .listStatements(holder.getBaseItem().getObject().asResource(), p, (RDFNode) null);
        final SecuredStatementIterator iter = new SecuredStatementIterator(getModel(), s);
        try {
            if (iter.hasNext()) {
                return SecuredStatementImpl.getInstance(getModel(), iter.next());
            }
            throw new PropertyNotFoundException(p);

        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource getResource() {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return SecuredResourceImpl.getInstance(getModel(), holder.getBaseItem().getResource());
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public short getShort() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return holder.getBaseItem().getShort();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getStatementProperty(final Property p) {
        if (canRead()) {
            final Statement stmt = holder.getBaseItem().getStatementProperty(p);
            if (checkRead(stmt)) {
                return SecuredStatementImpl.getInstance(getModel(), stmt);
            }
        }
        throw new PropertyNotFoundException(p);
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getString() {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return getLiteral().getLexicalForm();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource getSubject() {
        checkRead();
        checkRead(holder.getBaseItem().asTriple());
        return SecuredResourceImpl.getInstance(getModel(), holder.getBaseItem().getSubject());
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement remove()
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkDelete(holder.getBaseItem());
        holder.getBaseItem().remove();
        return holder.getSecuredItem();
    }

}
