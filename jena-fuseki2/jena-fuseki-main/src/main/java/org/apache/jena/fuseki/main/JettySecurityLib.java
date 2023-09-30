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

import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.sys.JettyLib;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.Constraint.Authorization;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;

public class JettySecurityLib {
    /** Default setting. */
    public final static AuthScheme dftAuthMode = AuthScheme.BASIC;
    /** Current auth mode */
    public static AuthScheme authMode = dftAuthMode;

    /** Create a Jetty {@link SecurityHandler} for a specific pathSpace, e.g {@code /database}. */
    public static SecurityHandler makeSecurityHandlerForPathspec(String pathSpec, String realm, UserStore userStore) {
        ConstraintSecurityHandler sh = makeSecurityHandler(realm, userStore);
        addPathConstraint(sh, pathSpec);
        return sh;
    }

    /** Create a Jetty {@link SecurityHandler} for basic authentication.
     * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
     * for adding the {@code pathspec} to apply it to.
     */
    public static ConstraintSecurityHandler makeSecurityHandler(String realm, UserStore userStore) {
        return makeSecurityHandler(realm, userStore, authMode);
    }

    /** Create a Jetty {@link SecurityHandler} for basic authentication.
     * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
     * for adding the {@code pathspec} to apply it to.
     */
    public static ConstraintSecurityHandler makeSecurityHandler(String realm, UserStore userStore, AuthScheme authMode) {
        return makeSecurityHandler$(realm, userStore, authMode);
    }

    /** Create a Jetty {@link SecurityHandler} for basic authentication.
     * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
     * for adding the {@code pathspec} to apply it to.
     */
    public static ConstraintSecurityHandler makeSecurityHandler$(String realm, UserStore userStore, AuthScheme authMode) {
        Objects.requireNonNull(userStore);

        if ( authMode == null )
            authMode = dftAuthMode;

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

        IdentityService identService = new DefaultIdentityService();
        securityHandler.setIdentityService(identService);

        // ---- HashLoginService
        HashLoginService loginService = new HashLoginService(realm);
        loginService.setUserStore(userStore);
        loginService.setIdentityService(identService);
        securityHandler.setLoginService(loginService);

        Authenticator authenticator = ( authMode == AuthScheme.BASIC ) ? new BasicAuthenticator() : new DigestAuthenticator();
        // Intercept (development)
        if ( false ) {
            authenticator = new BasicAuthenticator() {
                @Override
                public UserIdentity login(String username, Object password, Request request, Response response) {
                    UserIdentity u =  super.login(username, password, request, response);
                    FmtLog.info(JettySecurityLib.class, "login(%s, %s) -> [%s]", username, password, u.getUserPrincipal());
                    return u ;
                }

                @Override
                public AuthenticationState validateRequest(Request req, Response res, Callback callback) throws ServerAuthException {
                    AuthenticationState s = super.validateRequest(req, res, callback);
                    if ( s != null )
                        FmtLog.info(JettySecurityLib.class, "validateRequest() -> %s %s", s, s.getUserPrincipal());
                    else
                        FmtLog.info(JettySecurityLib.class, "validateRequest() -> null");
                    return s;
                }
            };
        }
        securityHandler.setAuthenticator(authenticator);
        if ( realm != null )
            securityHandler.setRealmName(realm);
        return securityHandler;
    }

    public static void addPathConstraint(ConstraintSecurityHandler securityHandler, String pathSpec) {
        addPathConstraint(securityHandler, pathSpec, null);
    }

    private static void addPathConstraint(ConstraintSecurityHandler securityHandler, String pathSpec, String role) {
        Objects.requireNonNull(securityHandler);
        Objects.requireNonNull(pathSpec);

        ConstraintMapping mapping = new ConstraintMapping();
        Constraint.Builder constraintBuilder = new Constraint.Builder();
        if ( role != null ) {
            constraintBuilder.roles(role);
            constraintBuilder.authorization(Authorization.SPECIFIC_ROLE);
        } else {
            constraintBuilder.authorization(Authorization.ANY_USER);
        }

        String authName = securityHandler.getAuthenticator().getAuthenticationType();
        constraintBuilder.name(authName);

        Constraint constraint = constraintBuilder.build();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(pathSpec);
        securityHandler.addConstraintMapping(mapping);
    }

    /**
     * Make a {@link UserStore} from a password file.
     * {@link PropertyUserStore} for details.
     */
    public static UserStore makeUserStore(String passwordFile) {
        if ( ! FileOps.exists(passwordFile) )
            throw new FusekiConfigException("No such file: "+passwordFile);
        PropertyUserStore propertyUserStore = new PropertyUserStore();
        Resource pwResource = JettyLib.newResource(passwordFile);
        propertyUserStore.setConfig(pwResource);
        propertyUserStore.setReloadInterval(5); // Need directory access
        try { propertyUserStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }
        return propertyUserStore;
    }

    private static String ANY_USER = null;

    /** Make a {@link UserStore} for a single user, password in any role. */
    public static UserStore makeUserStore(String user, String password) {
        return makeUserStore(user, password, ANY_USER);
    }

    /** Make a {@link UserStore} for a single user,password[,role]. */
    private static UserStore makeUserStore(String user, String password, String role) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        UserStore userStore = new UserStore();
        addUser(userStore, user, password, role);
        try { userStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }
        return userStore;
    }

    public static UserStore addUser(UserStore userStore, String user, String password) {
        return addUser(userStore, user, password, ANY_USER);
    }

    /** Make a {@link UserStore} for a single user,password,role*/
    public static UserStore addUser(UserStore userStore, String user, String password, String role) {
        String[] roles = (role == null) ? null : new String[]{role};
        Credential cred  = new Password(password);
        userStore.addUser(user, cred, roles);
        return userStore;

    }
}
