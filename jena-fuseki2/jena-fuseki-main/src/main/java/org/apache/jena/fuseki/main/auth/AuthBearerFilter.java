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

package org.apache.jena.fuseki.main.auth;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.http.auth.AuthHeader;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Process an {@code Authorization: Bearer <token>} header. If present, extract {@code <token>} as JWT and try to verify
 * with a configurable user verifier function.
 * <h3>User Verifier Function</h3>
 * <p>
 * The user verifier function has the signature of {@code Function<String, String>} where the input is the raw base64
 * encoded JWT as obtained from the {@code Authorization} header.  If your function considers the token valid then it
 * <strong>MUST</strong> return the username for the user, if it considers the token invalid then it
 * <strong>MUST</strong> return {@code null} to indicate this.  Your function <strong>MUST NOT</strong> throw any
 * exceptions, however you may wish to log the reason for considering a token invalid to the server logs.
 * </p>
 * <h3>Programmatic Usage</h3>
 * <p>
 * For programmatic server building the verification function can be provided directly to the constructor.
 * </p>
 * <h3>Servlet Configuration Usage</h3>
 * <p>
 * For use in {@code web.xml} or other Java servlet based environments the filter can configure itself automatically. It
 * first looks for a function from the servlet context attribute (defined by the static field
 * {@link AuthBearerFilter#USER_VERIFIER_FUNCTION_ATTRIBUTE}) and if that is a valid function uses that.  This allows
 * for deployments to use a {@link ServletContextListener} or similar servlet lifecycle mechanism to construct and
 * inject their desired function programmatically at runtime and have the filter honour it.
 * </p>
 * <p>
 * If the function to use can be statically configured then you can use the
 * {@link AuthBearerFilter#USER_VERIFIER_FUNCTION_ATTRIBUTE} parameters as an {@code <init-param>}'s to specify the
 * class and method of the function to use in the {@code <filter>} definition e.g.
 * </p>
 * <pre>
 * <filter>
 *     <filter-name>BearerAuthFilter</filter-name>
 *     <filter-class>org.apache.jena.fuseki.main.auth.AuthBearerFilter</filter-class>
 *     <init-param>
 *         <param-name>org.apache.jena.fuseki.main.auth.AuthBearerFilter.userVerifier</param-name>
 *         <param-value>your.package.SomeClass.SomeMethod</param-value>
 *     </init-param>
 * </filter>
 * </pre>
 * <p>
 * In this scenario {@code your.package.SomeClass} must be a valid Java class and {@code SomeMethod} a static method on
 * that class that conforms to the
 * </p>
 */
public class AuthBearerFilter implements Filter {
    private static Logger log = Fuseki.serverLog;
    private Function<String, String> userVerifier;

    /**
     * Servlet context attribute from which a user verifier function can be retrieved
     */
    public static final String USER_VERIFIER_FUNCTION_ATTRIBUTE =
            AuthBearerFilter.class.getCanonicalName() + ".userVerifier";

    @Override
    public void init(FilterConfig filterConfig) {
        // Don't reconfigure if we've been explicitly configured via our constructor
        if (this.userVerifier != null) {
            return;
        }

        // For general usage allow initializing in the following ways:
        // 1 - Via a servlet attribute i.e. allow some other code in the servlet container lifecycle to inject this
        // 2 - With the name of a class and a static method on that class

        Object rawVerifier = filterConfig.getServletContext().getAttribute(USER_VERIFIER_FUNCTION_ATTRIBUTE);
        if (rawVerifier != null) {
            if (rawVerifier instanceof Function<?, ?>) {
                try {
                    this.userVerifier = (Function<String, String>) rawVerifier;
                } catch (ClassCastException e) {
                    log.error(
                            "Failed to initialise AuthBearerFilter as servlet context attribute {} is not of type Function<String, String>",
                            USER_VERIFIER_FUNCTION_ATTRIBUTE);
                }
            }
        } else {
            String rawVerifierClass = filterConfig.getInitParameter(USER_VERIFIER_FUNCTION_ATTRIBUTE);
            if (StringUtils.isNotBlank(rawVerifierClass)) {
                String rawVerifierMethod = rawVerifierClass.substring(rawVerifierClass.lastIndexOf('.') + 1);
                rawVerifierClass = rawVerifierClass.substring(0, rawVerifierClass.lastIndexOf('.'));
                try {
                    Class<?> cls = Class.forName(rawVerifierClass);
                    if (StringUtils.isNotBlank(rawVerifierMethod)) {
                        Method method = cls.getMethod(rawVerifierMethod, String.class);
                        if (method.getReturnType().equals(String.class)) {
                            this.userVerifier = token -> {
                                try {
                                    return (String) method.invoke(null, token);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    // This will allow the invocation failure to bubble up the catch clause of the
                                    // doFilter() logic and be suitably logged in the server logs and turned into a
                                    // suitable HTTP 500 error
                                    throw new RuntimeException(e);
                                }
                            };
                        } else {
                            log.error(
                                    "Failed to initialise AuthBearerFilter as filter init parameter {} specifies method {} on class {} which does not have the correct return type String (found {})",
                                    USER_VERIFIER_FUNCTION_ATTRIBUTE, rawVerifierMethod, rawVerifierClass,
                                    method.getReturnType().getCanonicalName());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.error(
                            "Failed to initialise AuthBearerFilter as filter init parameter {} specifies class {} which was not found on the classpath",
                            USER_VERIFIER_FUNCTION_ATTRIBUTE, rawVerifierClass);
                } catch (NoSuchMethodException e) {
                    log.error(
                            "Failed to initialise AuthBearerFilter as filter init parameter {} specifies method {} which was not found on configured class {}",
                            USER_VERIFIER_FUNCTION_ATTRIBUTE, rawVerifierMethod, rawVerifierClass);
                }
            }
        }

        if (this.userVerifier == null) {
            log.warn(
                    "No user verifier function configured to verify Bearer authentication tokens, all requests that use this filter will be rejected as a result");
        }
    }

    /**
     * Creates a new filter
     * <p>
     * This constructor is typically used implicitly when you use this filter e.g via {@code web.xml}, or when you want
     * this filter to configure itself automatically from the servlet container lifecycle.
     * </p>
     */
    public AuthBearerFilter() {
        this(null);
    }

    /**
     * Creates a new filter with the provided user verifier function
     * <p>
     * This constructor is typically used explicitly when you are constructing an application directly e.g. Fuseki style
     * usage.
     * </p>
     *
     * @param userVerifier A function that verifies the provided bearer tokens, returning the username if the token is
     *                     valid
     */
    public AuthBearerFilter(Function<String, String> userVerifier) {
        this.userVerifier = userVerifier;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest withUser;
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            withUser = request;

            // Authorization: required.
            String auth = request.getHeader(HttpNames.hAuthorization);
            if (auth == null) {
                // No auth header - reject and ask for Authorization:
                response.setHeader(HttpNames.hWWWAuthenticate, "Bearer");   // No realm, no scope.
                response.sendError(HttpSC.UNAUTHORIZED_401);
                return;
            }

            AuthHeader authParser = AuthHeader.parse(auth);
            String bearerToken = authParser.getBearerToken();
            String x = authParser.getUnknown();

            switch (authParser.getAuthScheme()) {
                case BEARER: {
                    if (bearerToken == null) {
                        log.warn("Not a legal bearer token: " + authParser.getAuthArgs());
                        response.sendError(HttpSC.BAD_REQUEST_400);
                        return;
                    }
                    if (userVerifier == null) {
                        // No function to verify the token and extract the user.
                        response.sendError(HttpSC.BAD_REQUEST_400);
                        return;
                    }
                    String user = userVerifier.apply(bearerToken);
                    if (user == null) {
                        response.sendError(HttpSC.FORBIDDEN_403);
                        return;
                    }
                    withUser = new HttpServletRequestWithPrincipal(request, user);
                    break;
                }
                case UNKNOWN:
                    break;
                case BASIC:
                case DIGEST:
                default:
                    break;
            }
        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: " + ex.getMessage(), ex);
            ServletOps.error(500);
            return;
        }
        // Continue.
        chain.doFilter(withUser, servletResponse);
    }

    /**
     * Add a value for "getUserPrincipal"
     */
    private static class HttpServletRequestWithPrincipal extends HttpServletRequestWrapper {

        final String user;

        HttpServletRequestWithPrincipal(HttpServletRequest req, String user) {
            super(req);
            this.user = user;
        }

        @Override
        public java.security.Principal getUserPrincipal() {
            return new java.security.Principal() {
                @Override
                public String getName() {
                    return user;
                }
            };
        }
    }

    @Override
    public void destroy() {
    }

}
