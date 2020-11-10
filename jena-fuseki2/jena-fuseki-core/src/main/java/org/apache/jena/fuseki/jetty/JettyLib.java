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

package org.apache.jena.fuseki.jetty;

import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;

/** Helpers for working with Jetty.
 * <p>
 * <h3>SecurityHandler</h3>
 *  <pre>
 *     UserStore userStore = JettyLib.makeUserStore(...);
 *     SecurityHandler securityHandler = JettyLib.makeSecurityHandler(String pathSpec, String realm, UserStore userStore);
 *  </pre>
 */
public class JettyLib {

    /** Create a Jetty {@link SecurityHandler} for a specific pathSpace, e.g {@code /database}. */
    public static SecurityHandler makeSecurityHandlerForPathspec(String pathSpec, String realm, UserStore userStore) {
        ConstraintSecurityHandler sh = makeSecurityHandler(realm, userStore);
        addPathConstraint(sh, pathSpec);
        return sh;
    }

//    /** Create a Jetty {@link SecurityHandler} for basic authentication. */
//    @Deprecated
//    public static SecurityHandler makeSecurityHandlerForPathspec(String pathSpec, String realm, UserStore userStore, String role) {
//        ConstraintSecurityHandler securityHandler = makeSecurityHandler(realm, userStore, role);
//        // Pathspec based.
//        addDatasetConstraint(securityHandler, pathSpec);
//        return securityHandler;
//    }

    /**
     * Digest requires an extra round trip so it is unfriendly to API
     * or scripts that stream.
     */
    public final static AuthScheme dftAuthMode = AuthScheme.DIGEST;
    /** Current auth mode */
    public static AuthScheme authMode = dftAuthMode;

    /** Create a Jetty {@link SecurityHandler} for basic authentication.
     * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
     * for adding the {@code pathspec} to apply it to.
     */
     public static ConstraintSecurityHandler makeSecurityHandler(String realm, UserStore userStore) {
         return makeSecurityHandler(realm, userStore, "**", authMode);
     }

     /** Create a Jetty {@link SecurityHandler} for basic authentication.
      * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
      * for adding the {@code pathspec} to apply it to.
      */
      public static ConstraintSecurityHandler makeSecurityHandler(String realm, UserStore userStore, AuthScheme authMode) {
          return makeSecurityHandler(realm, userStore, "**", authMode);
      }

      /** Create a Jetty {@link SecurityHandler} for basic authentication.
     * See {@linkplain #addPathConstraint(ConstraintSecurityHandler, String)}
     * for adding the {@code pathspec} to apply it to.
     */
     public static ConstraintSecurityHandler makeSecurityHandler(String realm, UserStore userStore, String role, AuthScheme authMode) {
        // role can be "**" for any authenticated user.
        Objects.requireNonNull(userStore);
        Objects.requireNonNull(role);

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
        securityHandler.setAuthenticator( authMode == AuthScheme.BASIC ? new BasicAuthenticator() : new DigestAuthenticator() );
        if ( realm != null )
            securityHandler.setRealmName(realm);
        return securityHandler;
    }

     public static void addPathConstraint(ConstraintSecurityHandler securityHandler, String pathSpec) {
         addPathConstraint(securityHandler, pathSpec, "**");
     }

    public static void addPathConstraint(ConstraintSecurityHandler securityHandler, String pathSpec, String role) {
        Objects.requireNonNull(securityHandler);
        Objects.requireNonNull(pathSpec);

        ConstraintMapping mapping = new ConstraintMapping();
        Constraint constraint = new Constraint();
        String[] roles = new String[]{role};
        constraint.setRoles(roles);
        constraint.setName(securityHandler.getAuthenticator().getAuthMethod());
        constraint.setAuthenticate(true);
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
        propertyUserStore.setConfig(passwordFile);
        propertyUserStore.setHotReload(true);
        try { propertyUserStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }
        return propertyUserStore;
    }

    /** Make a {@link UserStore} for a single user,password in any role. */
    public static UserStore makeUserStore(String user, String password) {
        return makeUserStore(user, password, "**");
    }

    /** Make a {@link UserStore} for a single user,password,role*/
    public static UserStore makeUserStore(String user, String password, String role) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        Objects.requireNonNull(role);
        UserStore userStore = new UserStore();
        addUser(userStore, user, password, role);
        try { userStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }
        return userStore;
    }
    public static UserStore addUser(UserStore userStore, String user, String password) {
        return addUser(userStore, user, password, "**");
    }

    /** Make a {@link UserStore} for a single user,password,role*/
    public static UserStore addUser(UserStore userStore, String user, String password, String role) {
        String[] roles = role == null ? null : new String[]{role};
        Credential cred  = new Password(password);
        userStore.addUser(user, cred, roles);
        return userStore;

    }



    /** Add or append a {@link Handler} to a Jetty {@link Server}. */
    public static void addHandler(Server server, Handler handler) {
        final Handler currentHandler = server.getHandler();
        if (currentHandler == null) {
            server.setHandler(handler);
        } else {
            if (currentHandler instanceof HandlerList) {
                ((HandlerList) currentHandler).addHandler(handler);
            } else {
                // Singleton handler. Convert to list.
                final HandlerList handlerList = new HandlerList();
                handlerList.addHandler(currentHandler);
                handlerList.addHandler(handler);
                server.setHandler(handlerList);
            }
        }
    }

    /** Add the RDF MIME Type mappins */
    public static void setMimeTypes(ServletContextHandler context) {
        MimeTypes mimeTypes = new MimeTypes();
        // RDF syntax
        mimeTypes.addMimeMapping("nt",      WebContent.contentTypeNTriples);
        mimeTypes.addMimeMapping("nq",      WebContent.contentTypeNQuads);
        mimeTypes.addMimeMapping("ttl",     WebContent.contentTypeTurtle+";charset=utf-8");
        mimeTypes.addMimeMapping("trig",    WebContent.contentTypeTriG+";charset=utf-8");
        mimeTypes.addMimeMapping("rdf",     WebContent.contentTypeRDFXML);
        mimeTypes.addMimeMapping("jsonld",  WebContent.contentTypeJSONLD);
        mimeTypes.addMimeMapping("rj",      WebContent.contentTypeRDFJSON);
        mimeTypes.addMimeMapping("rt",      WebContent.contentTypeRDFThrift);
        mimeTypes.addMimeMapping("trdf",    WebContent.contentTypeRDFThrift);

        // SPARQL syntax
        mimeTypes.addMimeMapping("rq",      WebContent.contentTypeSPARQLQuery);
        mimeTypes.addMimeMapping("ru",      WebContent.contentTypeSPARQLUpdate);

        // SPARQL Result set
        mimeTypes.addMimeMapping("rsj",     WebContent.contentTypeResultsJSON);
        mimeTypes.addMimeMapping("rsx",     WebContent.contentTypeResultsXML);
        mimeTypes.addMimeMapping("srt",     WebContent.contentTypeResultsThrift);

        // Other
        mimeTypes.addMimeMapping("txt",     WebContent.contentTypeTextPlain);
        mimeTypes.addMimeMapping("csv",     WebContent.contentTypeTextCSV);
        mimeTypes.addMimeMapping("tsv",     WebContent.contentTypeTextTSV);
        context.setMimeTypes(mimeTypes);
    }
}
