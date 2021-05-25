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

import java.util.function.Supplier;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.permissions.model.SecuredStatement;
import org.apache.jena.permissions.utils.PermStatementFilter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

/**
 * Implementation of SecuredResource to be used by a SecuredItemInvoker proxy.
 */
public class SecuredResourceImpl extends SecuredRDFNodeImpl implements SecuredResource {
    /**
     * Get a SecuredResource.
     * 
     * @param securedModel the securedItem that provides the security context.
     * @param resource     The resource to secure.
     * @return The SecuredResource
     */
    public static SecuredResource getInstance(final SecuredModel securedModel, final Resource resource) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null");
        }
        if (resource.isLiteral()) {
            throw new IllegalArgumentException("Resource may not be a literal");
        }
        // check that resource has a securedModel.
        Resource goodResource = resource;
        if (goodResource.getModel() == null) {
            final Node n = resource.asNode();
            if (resource.isAnon()) {
                goodResource = securedModel.createResource(new AnonId(n.getBlankNodeId()));
            } else {
                goodResource = securedModel.createResource(n.getURI());
            }
        }

        final ItemHolder<Resource, SecuredResource> holder = new ItemHolder<>(goodResource);

        final SecuredResourceImpl checker = new SecuredResourceImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (goodResource instanceof SecuredResource) {
            if (checker.isEquivalent((SecuredResource) goodResource)) {
                return (SecuredResource) goodResource;
            }
        }

        return holder.setSecuredItem(new SecuredItemInvoker(resource.getClass(), checker));

    }

    // the item holder that contains this SecuredResource
    private final ItemHolder<? extends Resource, ? extends SecuredResource> holder;

    /**
     * Constructor.
     * 
     * @param securedModel The secured model to use
     * @param holder       the item holder that will contain this SecuredResource.
     */
    protected SecuredResourceImpl(final SecuredModel securedModel,
            final ItemHolder<? extends Resource, ? extends SecuredResource> holder) {
        super(securedModel, holder);
        this.holder = holder;
    }

    /**
     * Abort the transaction in the associated securedModel.
     * 
     * @return This resource to permit cascading.
     */
    @Override
    public SecuredResource abort() {
        holder.getBaseItem().abort();
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property p, final boolean o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property p, final char o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property value, final double d)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(value, ResourceFactory.createTypedLiteral(d));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property value, final float d)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(value, ResourceFactory.createTypedLiteral(d));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property p, final Literal o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, o);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property p, final long o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addLiteral(final Property p, final Object o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addProperty(final Property p, final RDFNode o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(), o.asNode()));
        holder.getBaseItem().addProperty(p, o);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addProperty(final Property p, final String o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return addProperty(p, o, "");
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addProperty(final Property p, final String lexicalForm, final RDFDatatype datatype)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Literal l = ResourceFactory.createTypedLiteral(lexicalForm, datatype);
        checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(), l.asNode()));
        holder.getBaseItem().addProperty(p, l);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create (this, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource addProperty(final Property p, final String o, final String l)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(), NodeFactory.createLiteral(o, l, false)));
        holder.getBaseItem().addProperty(p, o, l);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Literal asLiteral() {
        checkRead();
        throw new LiteralRequiredException(asNode());
    }

    @Override
    public SecuredResource asResource() {
        return holder.getSecuredItem();
    }

    @Override
    public SecuredResource begin() {
        holder.getBaseItem().begin();
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read( this, p, ANY );
     * @param p The property to test.
     * @return true if p can be read as a property with an ANY value.
     * @throws AuthenticationRequiredException
     */
    public boolean canReadProperty(final Node p) throws AuthenticationRequiredException {
        return canRead() && canRead(new Triple(holder.getBaseItem().asNode(), p, Node.ANY));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read( this, p, ANY );
     * @param p
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException
     */
    protected void checkReadProperty(final Node p) throws ReadDeniedException, AuthenticationRequiredException {
        if (!canReadProperty(p)) {
            throw new ReadDeniedException(SecuredItem.Util.triplePermissionMsg(getModelNode()),
                    new Triple(holder.getBaseItem().asNode(), p, Node.ANY));
        }
    }

    @Override
    public SecuredResource commit() {
        holder.getBaseItem().commit();
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public AnonId getId() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().getId();

    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getLocalName() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().getLocalName();
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getNameSpace() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().getNameSpace();
    }

    /**
     * Gets the first statement in the iterator that the user can read, null
     * otherwise.
     * 
     * @param supplier the supplier of the Statement iterator.
     * @return the statement or null.
     */
    private SecuredStatement getProperty(Supplier<ExtendedIterator<Statement>> supplier) {
        if (checkSoftRead()) {
            final ExtendedIterator<Statement> iter = supplier.get()
                    .filterKeep(new PermStatementFilter(Action.Read, this));
            try {
                if (iter.hasNext()) {
                    return SecuredStatementImpl.getInstance(getModel(), iter.next());
                }
            } finally {
                iter.close();
            }
        }
        return null;
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then {@code null} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getProperty(final Property p) throws ReadDeniedException, AuthenticationRequiredException {
        return getProperty(() -> holder.getBaseItem().listProperties(p));
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then {@code null} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getProperty(Property p, String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        return getProperty(() -> holder.getBaseItem().listProperties(p, lang));
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then {@code null} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource getPropertyResourceValue(final Property p) throws AuthenticationRequiredException {
        final SecuredStatementIterator iter = listProperties(p);
        try {
            while (iter.hasNext()) {
                final Statement s = iter.next();
                if (s.getObject().isResource()) {
                    return SecuredResourceImpl.getInstance(getModel(), s.getObject().asResource());
                }
            }
        } finally {
            iter.close();
        }
        return null;
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then PropertyNotFoundException is
     *            thrown.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getRequiredProperty(final Property p)
            throws PropertyNotFoundException, ReadDeniedException, AuthenticationRequiredException {
        SecuredStatement stmt = getProperty(() -> holder.getBaseItem().listProperties(p));
        if (stmt == null) {
            throw new PropertyNotFoundException(p);
        }
        return stmt;
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then PropertyNotFoundException is
     *            thrown.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getRequiredProperty(Property p, String lang)
            throws PropertyNotFoundException, ReadDeniedException, AuthenticationRequiredException {
        SecuredStatement stmt = getProperty(() -> holder.getBaseItem().listProperties(p, lang));
        if (stmt == null) {
            throw new PropertyNotFoundException(p);
        }
        return stmt;
    }

    /**
     * @sec.graph Read
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getURI() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().getURI();
    }

    /**
     * @sec.graph Read
     * 
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code null} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Statement getStmtTerm() {
        if (checkSoftRead()) {
            Statement stmt = holder.getBaseItem().getStmtTerm();
            if (stmt != null && canRead(stmt)) {
                return SecuredStatementImpl.getInstance(getModel(), stmt);
            }
        }
        return null;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final boolean o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final char o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final double o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final float o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final long o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasLiteral(final Property p, final Object o)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
                ResourceFactory.createTypedLiteral(o).asNode()))) {
            return holder.getBaseItem().hasLiteral(p, o);
        }
        return false;
    }

    private boolean hasProperty(Supplier<ExtendedIterator<Statement>> supplier) {
        if (checkSoftRead()) {
            final ExtendedIterator<Statement> iter = supplier.get()
                    .filterKeep(new PermStatementFilter(Action.Read, this));
            try {
                return iter.hasNext();
            } finally {
                iter.close();
            }
        }
        return false;

    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasProperty(final Property p) throws ReadDeniedException, AuthenticationRequiredException {
        return hasProperty(() -> holder.getBaseItem().listProperties(p));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasProperty(final Property p, final RDFNode o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return hasProperty(() -> holder.getBaseItem().getModel().listStatements(this, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasProperty(final Property p, final String o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return hasProperty(() -> holder.getBaseItem().getModel().listStatements(this, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read SecTriple(this,p,o)
     * 
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code false} is returned.
     * 
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasProperty(final Property p, final String o, final String l)
            throws ReadDeniedException, AuthenticationRequiredException {
        final Literal ll = holder.getBaseItem().getModel().createLiteral(o, l);
        return hasProperty(() -> holder.getBaseItem().getModel().listStatements(this, p, ll));
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasURI(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().hasURI(uri);
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Resource inModel(final Model m) {
        checkRead();
        return holder.getBaseItem().inModel(m);
    }

    private SecuredStatementIterator listProperties(Supplier<StmtIterator> supplier) {
        ExtendedIterator<Statement> iter = checkSoftRead() ? supplier.get() : NiceIterator.emptyIterator();
        return new SecuredStatementIterator(getModel(), iter);
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on returned Statements
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
    public SecuredStatementIterator listProperties() throws ReadDeniedException, AuthenticationRequiredException {
        return listProperties(() -> holder.getBaseItem().listProperties());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on returned Statements
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
    public SecuredStatementIterator listProperties(final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return listProperties(() -> holder.getBaseItem().listProperties(p));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on returned Statements
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
    public SecuredStatementIterator listProperties(final Property p, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        return listProperties(() -> holder.getBaseItem().listProperties(p, lang));
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on associated Statements
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource removeAll(final Property p) throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(new Triple(holder.getBaseItem().asNode(), p.asNode(), Node.ANY))) {
            final StmtIterator iter = holder.getBaseItem().listProperties(p);
            try {
                if (!iter.hasNext()) {
                    // thre aren't any to delete -- so return
                    return holder.getSecuredItem();
                }
                while (iter.hasNext()) {
                    checkDelete(iter.next().asTriple());
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().removeAll(p);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on associated Statements
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource removeProperties() throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(new Triple(holder.getBaseItem().asNode(), Node.ANY, Node.ANY))) {
            final StmtIterator iter = holder.getBaseItem().listProperties();
            try {
                if (!iter.hasNext()) {
                    // thre arn't any to delete -- so return
                    return holder.getSecuredItem();
                }
                while (iter.hasNext()) {
                    checkDelete(iter.next().asTriple());
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().removeProperties();
        return holder.getSecuredItem();
    }

    @Override
    public Object visitWith(final RDFVisitor rv) {
        return isAnon() ? rv.visitBlank(this, getId()) : rv.visitURI(this, getURI());
    }
}
