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

package org.apache.jena.fuseki.mod.shiro;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.jena.fuseki.FusekiException;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.servlet.ShiroFilter;

/**
 * FusekiShiroFilter, includes Shiro initialization. Fuseki is a
 * not a webapp so it needs to trigger off servlet initialization.
 */
/*package*/ class FusekiShiroFilter extends ShiroFilter {

    private final String shiroInitializationResource;
    private final boolean disableShiroSessions;

    /*package*/ FusekiShiroFilter(String shiroResourceName, boolean disableShiroSessions) {
        // Shiro file -- URLs are "file:<no encoding>"
        this.shiroInitializationResource = shiroResourceName;
        this.disableShiroSessions = disableShiroSessions;
    }

    @Override
    public void init() throws Exception {
        try {
            // Intercept Shiro initialization.
            List<String> locations = List.of();
            if ( shiroInitializationResource != null ) {
                locations = List.of(shiroInitializationResource);
            }
            FusekiShiro.shiroEnvironment(getServletContext(), locations);
        } catch (FusekiException ex) {
            // Wrap so ShiroFilter does not log it.
            throw new ServletException(ex);
        }
        super.init();
    }

    @Override
    protected ServletRequest prepareServletRequest(ServletRequest request, ServletResponse response, FilterChain chain) {
        if ( disableShiroSessions ) {
            // GH-4033 : https://github.com/apache/jena/issues/4033
            // See NoSessionCreationFilter
            request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
        }
        return super.prepareServletRequest(request, response, chain);
    }
}
