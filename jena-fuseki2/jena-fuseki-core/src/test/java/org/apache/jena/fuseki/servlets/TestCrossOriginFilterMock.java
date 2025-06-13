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
package org.apache.jena.fuseki.servlets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.apache.jena.fuseki.servlets.CrossOriginFilter.*;
import static org.mockito.Mockito.*;

public class TestCrossOriginFilterMock {
    private static class TestFilterConfig implements FilterConfig {
        Map<String,String> parameterMap;

        public TestFilterConfig() {
            this(emptyMap());
        }

        public TestFilterConfig(Map<String,String> initMap) {
            parameterMap = initMap;
        }

        @Override
        public String getFilterName() {
            return "TestFilter";
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public String getInitParameter(String s) {
            return parameterMap.get(s);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(parameterMap.keySet());
        }
    }

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    @BeforeEach
    public void setUpTest() {
        when(httpServletRequest.getHeader("Origin")).thenReturn("http://localhost:12335");
        when(httpServletRequest.getHeaders("Connection")).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getRequestURI()).thenReturn("/relevant-url");
        when(httpServletRequest.getHeader("Access-Control-Request-Method")).thenReturn("POST");
        when(httpServletRequest.getMethod()).thenReturn("OPTIONS");
    }

    @Test
    public void test_AccessControlRequestHeaders() throws ServletException, IOException {
        // given
        CrossOriginFilter cut = new CrossOriginFilter();

        String allowedList = "content-type, accept, origin";
        String oneWrong  = "content-type, unrecognised, accept, origin";

        when(httpServletRequest.getHeader("Access-Control-Request-Headers")).thenReturn(oneWrong);

        cut.init(new TestFilterConfig());

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(0)).setHeader(eq("Access-Control-Allow-Headers"), any());

        // when
        when(httpServletRequest.getHeader("Access-Control-Request-Headers")).thenReturn(allowedList);
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Accept,Origin");
    }

    @Test
    public void test_SimpleRequest() throws ServletException, IOException {
        // given
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getHeader("Access-Control-Request-Method")).thenReturn(null);

        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(
                Map.of(ALLOW_CREDENTIALS_PARAM, "true",
                       EXPOSED_HEADERS_PARAM, "exposedHeader"))
        );

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, "exposedHeader");
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader("Access-Control-Allow-Origin", "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader("Access-Control-Allow-Credentials", "true");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowAllHeaders() throws ServletException, IOException {
        // given
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(
                Map.of(ALLOWED_HEADERS_PARAM, "*")
        ));
        String anyHeaders  = "content-type, unrecognised, accept, origin";
        when(httpServletRequest.getHeader("Access-Control-Request-Headers")).thenReturn(anyHeaders);

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "content-type,unrecognised,accept,origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_ignoreInvalidAge() throws ServletException, IOException {
        // given
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(
                Map.of(PREFLIGHT_MAX_AGE_PARAM, "INVALID")
        ));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_handleOldChainPreflightParam() throws ServletException, IOException {
        // given
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(
                Map.of(OLD_CHAIN_PREFLIGHT_PARAM, "false")
        ));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_destroy_clearsConfig() throws ServletException, IOException {
        // given
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(
                Map.of(ALLOW_CREDENTIALS_PARAM, "true",
                       EXPOSED_HEADERS_PARAM, "exposedHeader"))
        );

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verifyNoMoreInteractions(httpServletResponse);
        // then
        cut.destroy();
        // then
        cut.doFilter(httpServletRequest, httpServletResponse, chain);
        verify(httpServletResponse, times(2)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_complexNonFlightRequest_treatedAsSimple() throws ServletException, IOException {
        // given
        when(httpServletRequest.getMethod()).thenReturn("PUT");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig());

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "http://localhost:12335");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_emptyOrigin() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn(null);
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig());

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedOrigins_specific() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:12345");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_ORIGINS_PARAM, "localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "localhost:12345");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");

        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedOrigins_regex() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:12345");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_ORIGINS_PARAM, "localhost:*"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "localhost:12345");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");

        verifyNoMoreInteractions(httpServletResponse);
    }


    @Test
    public void test_allowedOrigins_noMatch() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:999999");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_ORIGINS_PARAM, "   ,localhost:54321, , localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedOrigins_emptyString() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("  ");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_ORIGINS_PARAM, " localhost:54321, ,localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedOrigins_noMatchWithEmptyString() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn(" x xx xxx   ");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_ORIGINS_PARAM, " localhost:54321, ,localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedTimedOrigins_specific() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:12345");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_TIMING_ORIGINS_PARAM, "localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "localhost:12345");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verify(httpServletResponse, times(1)).setHeader(TIMING_ALLOW_ORIGIN_HEADER,"localhost:12345");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_allowedTimedOrigins_regex() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:12345");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_TIMING_ORIGINS_PARAM, "localhost:*"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "localhost:12345");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verify(httpServletResponse, times(1)).setHeader(TIMING_ALLOW_ORIGIN_HEADER,"localhost:12345");
        verifyNoMoreInteractions(httpServletResponse);
    }


    @Test
    public void test_allowedTimedOrigins_noMatch() throws ServletException, IOException {
        // given
        when(httpServletRequest.getHeader("Origin")).thenReturn("localhost:999999");
        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig(Map.of(
                ALLOWED_TIMING_ORIGINS_PARAM, "localhost:54321, localhost:12345"
        )));

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "localhost:999999");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, "1800");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,POST,HEAD");
        verify(httpServletResponse, times(1)).setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "X-Requested-With,Content-Type,Accept,Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    public void test_requestDisabled() throws ServletException, IOException {
        // given
        Enumeration<String> connections = Collections.enumeration(List.of("Random", "Upgrade"));
        when(httpServletRequest.getHeaders("Connection")).thenReturn(connections);
        Enumeration<String> upgrades = Collections.enumeration(List.of("SomethingElse", "WebSocket"));
        when(httpServletRequest.getHeaders("Upgrade")).thenReturn(upgrades);

        CrossOriginFilter cut = new CrossOriginFilter();
        cut.init(new TestFilterConfig());

        // when
        cut.doFilter(httpServletRequest, httpServletResponse, chain);

        // then
        verify(httpServletResponse, times(1)).addHeader("Vary", "Origin");
        verifyNoMoreInteractions(httpServletResponse);
    }


}
