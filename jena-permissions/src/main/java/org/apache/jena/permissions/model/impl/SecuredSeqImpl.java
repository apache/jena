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

import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredAlt;
import org.apache.jena.permissions.model.SecuredBag;
import org.apache.jena.permissions.model.SecuredLiteral;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.permissions.model.SecuredSeq;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.SeqIndexBoundsException;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Implementation of SecuredSeq to be used by a SecuredItemInvoker proxy.
 *
 * Sequence may have breaks in the order.
 * http://www.w3.org/TR/2004/REC-rdf-mt-20040210/#Containers
 *
 */
public class SecuredSeqImpl extends SecuredContainerImpl implements SecuredSeq {

    /**
     * A filter that returns objects that have an ordinal predicate and match the
     * node in the constructor.
     *
     */
    private class RDFNodeFilter implements Predicate<Statement> {
        private final RDFNode n;

        /**
         * Constructor.
         *
         * @param n the node to match.
         */
        public RDFNodeFilter(final RDFNode n) {
            this.n = n;
        }

        @Override
        public boolean test(final Statement o) {
            return (o.getPredicate().getOrdinal() != 0) && n.equals(o.getObject());
        }

    }

    /**
     * get a SecuredSeq.
     *
     * @param securedModel The secured model that provides the security context
     * @param seq          The Seq to secure.
     * @return the SecuredSeq
     */
    public static SecuredSeq getInstance(final SecuredModel securedModel, final Seq seq) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (seq == null) {
            throw new IllegalArgumentException("Seq may not be null");
        }
        final ItemHolder<Seq, SecuredSeq> holder = new ItemHolder<>(seq);
        final SecuredSeqImpl checker = new SecuredSeqImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (seq instanceof SecuredSeq) {
            if (checker.isEquivalent((SecuredSeq) seq)) {
                return (SecuredSeq) seq;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(seq.getClass(), checker));
    }

    // the item holder that contains this SecuredSeq.
    private final ItemHolder<? extends Seq, ? extends SecuredSeq> holder;

    /**
     * Constructor.
     *
     * @param securedModel The secured model that provides the security context
     * @param holder       The item holder that will contain this SecuredSeq.
     */
    protected SecuredSeqImpl(final SecuredModel securedModel,
            final ItemHolder<? extends Seq, ? extends SecuredSeq> holder) {
        super(securedModel, holder);
        this.holder = holder;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, boolean o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, long o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, char o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, float o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, double o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, Object o) {
        return add(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(int index, String o) {
        return add(index, o, "");
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(final int index, final RDFNode o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(index, o);
        holder.getBaseItem().add(index, o);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( this, RDF.li(1), o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Seq add(final int index, final String o, final String l)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(index, holder.getBaseItem().getModel().createLiteral(o, l));
    }

    /**
     * Verifies that a node with the specified index can be created.
     *
     * @param index the index to check
     * @param n     the RDFNode to to check.
     */
    private void checkCreate(final int index, final RDFNode n) {
        checkCreate(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), n.asNode()));
    }

    /**
     * Gets the index of the node in the container.
     *
     * @param n the node to look for
     * @return the statement containing the node or {@code null} if none was found.
     */
    private Statement containerIndexOf(final RDFNode n) {
        final ExtendedIterator<Statement> iter = listProperties().filterKeep(new RDFNodeFilter(n));
        try {
            if (iter.hasNext()) {
                return iter.next();
            }
            return null;
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredAlt getAlt(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Alt a = holder.getBaseItem().getAlt(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), a.asNode()));
            return SecuredAltImpl.getInstance(getModel(), a);
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag getBag(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Bag b = holder.getBaseItem().getBag(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), b.asNode()));
            return SecuredBagImpl.getInstance(getModel(), b);
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean getBoolean(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final boolean retval = holder.getBaseItem().getBoolean(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public byte getByte(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final byte retval = holder.getBaseItem().getByte(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public char getChar(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final char retval = holder.getBaseItem().getChar(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public double getDouble(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final double retval = holder.getBaseItem().getDouble(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public float getFloat(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final float retval = holder.getBaseItem().getFloat(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int getInt(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final int retval = holder.getBaseItem().getInt(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getLanguage(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Literal literal = holder.getBaseItem().getLiteral(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), literal.asNode()));
            return literal.getLanguage();
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredLiteral getLiteral(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Literal literal = holder.getBaseItem().getLiteral(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), literal.asNode()));
            return SecuredLiteralImpl.getInstance(getModel(), literal);
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public long getLong(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final long retval = holder.getBaseItem().getLong(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), asObject(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFNode getObject(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final RDFNode retval = holder.getBaseItem().getObject(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), retval.asNode()));
            return SecuredRDFNodeImpl.getInstance(getModel(), retval);
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource getResource(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Resource retval = holder.getBaseItem().getResource(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), retval.asNode()));
            return SecuredResourceImpl.getInstance(getModel(), retval);
        }
        throw new SeqIndexBoundsException(index, 0);
    }


    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq getSeq(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Seq retval = holder.getBaseItem().getSeq(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), retval.asNode()));
            return SecuredSeqImpl.getInstance(getModel(), retval);
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public short getShort(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final short retval = holder.getBaseItem().getShort(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(),
                    ResourceFactory.createTypedLiteral(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then SeqIndexBoundsException is
     *            thrown
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getString(final int index) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final String retval = holder.getBaseItem().getString(index);
            checkRead(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(),
                    ResourceFactory.createTypedLiteral(retval).asNode()));
            return retval;
        }
        throw new SeqIndexBoundsException(index, 0);
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final boolean o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final char o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final double o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final float o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final long o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final Object o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asObject(o));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final RDFNode o) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Statement stmt = containerIndexOf(o);
            if (stmt != null) {
                checkRead(stmt);
                return stmt.getPredicate().getOrdinal();
            }
        }
        return 0;
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final String o) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asLiteral(o, ""));
    }

    /**
     * @sec.graph Read if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then 0 is returned
     * @sec.triple Read SecTriple( this, RDF.li(1), o )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final String o, final String l) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(asLiteral(o, l));
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete SecTriple( this, RDF.li(1), o )
     * @sec.triple Update Triples after index
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq remove(final int index)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final RDFNode rdfNode = holder.getBaseItem().getObject(index);
        if (rdfNode != null) {
            checkDelete(Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), rdfNode.asNode()));
            holder.getBaseItem().remove(index);
        }
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final boolean o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final char o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final double o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final float o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final long o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final Object o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, asObject(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final RDFNode o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple t2 = Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), o.asNode());
        final RDFNode rdfNode = holder.getBaseItem().getObject(index);
        final Triple t1 = Triple.create(holder.getBaseItem().asNode(), RDF.li(index).asNode(), rdfNode.asNode());
        checkUpdate(t1, t2);
        holder.getBaseItem().set(index, o);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final String o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, asLiteral(o, ""));
    }

    /**
     * @sec.graph Update
     * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
     *             RDF.li(index), o )
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq set(final int index, final String o, final String l)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return set(index, asLiteral(o, l));
    }
}
