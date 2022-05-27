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

package org.apache.jena.fuseki.main;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.fuseki.servlets.ActionErrorException;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestAuthBearerFilter {

    public static String UnitTestTokenVerifier(String token) {
        return token;
    }

    public static boolean WrongReturnType(String token) {
        return StringUtils.isNotBlank(token);
    }

    @Test
    public void auth_bearer_no_header() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(eq(HttpNames.hWWWAuthenticate), eq("Bearer"));
        verify(response).sendError(eq(HttpSC.UNAUTHORIZED_401));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_missing_token() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpSC.BAD_REQUEST_400));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_invalid_token() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer not&base$64");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpSC.BAD_REQUEST_400));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_not_configured() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpSC.BAD_REQUEST_400));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_forbidden() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter(token -> null);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpSC.FORBIDDEN_403));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test(expected = ActionErrorException.class)
    public void auth_bearer_verification_error() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter(token -> {throw new RuntimeException();});
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);
    }

    @Test
    public void auth_bearer_verified() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter(token -> token);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        Assert.assertEquals("foo", captor.getValue().getUserPrincipal().getName());
    }

    @Test
    public void auth_bearer_basic_passthrough() throws ServletException, IOException {
        // Basic Auth handled elsewhere
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Basic foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_digest_passthrough() throws ServletException, IOException {
        // Digest Auth handled elsewhere
        AuthBearerFilter filter = new AuthBearerFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Digest foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_init_no_configuration() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpSC.BAD_REQUEST_400);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_init_via_servlet_attribute() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(context.getAttribute(eq(AuthBearerFilter.USER_VERIFIER_FUNCTION_ATTRIBUTE))).thenReturn(
                (Function<String, String>) s -> s);
        when(config.getServletContext()).thenReturn(context);
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        Assert.assertEquals("foo", captor.getValue().getUserPrincipal().getName());
    }

    @Test
    public void auth_bearer_init_via_init_parameters() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(eq(AuthBearerFilter.USER_VERIFIER_FUNCTION_ATTRIBUTE))).thenReturn(
                this.getClass().getCanonicalName() + ".UnitTestTokenVerifier");
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        Assert.assertEquals("foo", captor.getValue().getUserPrincipal().getName());
    }

    @Test
    public void auth_bearer_init_via_init_parameters_wrong_return_type() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(eq(AuthBearerFilter.USER_VERIFIER_FUNCTION_ATTRIBUTE))).thenReturn(
                this.getClass().getCanonicalName() + ".WrongReturnType");
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpSC.BAD_REQUEST_400);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_init_via_init_parameters_no_such_method() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(eq(AuthBearerFilter.USER_VERIFIER_FUNCTION_ATTRIBUTE))).thenReturn(
                this.getClass().getCanonicalName() + ".NoSuchMethod");
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpSC.BAD_REQUEST_400);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void auth_bearer_init_via_init_parameters_no_such_class() throws ServletException, IOException {
        AuthBearerFilter filter = new AuthBearerFilter();
        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(eq(AuthBearerFilter.USER_VERIFIER_FUNCTION_ATTRIBUTE))).thenReturn(
                "NoSuchClass.NoSuchMethod");
        filter.init(config);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(eq(HttpNames.hAuthorization))).thenReturn("Bearer foo");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpSC.BAD_REQUEST_400);
        verify(chain, never()).doFilter(any(), any());
    }
}
