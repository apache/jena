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

import java.util.Map;

import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * The interface for secured PrefixMapping instances.
 *
 * Use the SecuredPrefixMapping.Factory to create instances
 */
public interface SecuredPrefixMapping extends PrefixMapping, SecuredItem {
    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String expandPrefix(final String prefixed) throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Map<String, String> getNsPrefixMap() throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getNsPrefixURI(final String prefix) throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getNsURIPrefix(final String uri) throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping lock() throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String qnameFor(final String uri) throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping removeNsPrefix(final String prefix)
            throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean samePrefixMappingAs(final PrefixMapping other)
            throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping setNsPrefix(final String prefix, final String uri)
            throws UpdateDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping setNsPrefixes(final Map<String, String> map)
            throws UpdateDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping setNsPrefixes(final PrefixMapping other)
            throws UpdateDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String shortForm(final String uri) throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public PrefixMapping withDefaultMappings(final PrefixMapping map)
            throws UpdateDeniedException, AuthenticationRequiredException;

}
