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

import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
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
    
    /** Create a Jetty {@link SecurityHandler} for basic authentication. */
    public static SecurityHandler makeSecurityHandler(String pathSpec, String realm, UserStore userStore) {
        return makeSecurityHandler(pathSpec, realm, userStore, "**");
    }
    
    /** Create a Jetty {@link SecurityHandler} for basic authentication. */
    public static SecurityHandler makeSecurityHandler(String pathSpec, String realm, UserStore userStore, String role) {
        // role can be "**" for any authenticated user.
        Objects.requireNonNull(pathSpec);
        Objects.requireNonNull(userStore);
        Objects.requireNonNull(role);
        
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        String[] roles = new String[]{role};
        constraint.setRoles(roles);
        constraint.setAuthenticate(true);

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(pathSpec);

        IdentityService identService = new DefaultIdentityService();
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(mapping);
        securityHandler.setIdentityService(identService);
        
        // ---- HashLoginService
        
        HashLoginService loginService = new HashLoginService("Authentication");
        loginService.setUserStore(userStore);
        loginService.setIdentityService(identService);
        
        // ----
        securityHandler.setLoginService(loginService);
        securityHandler.setAuthenticator(new BasicAuthenticator());
        if ( realm != null )
            securityHandler.setRealmName(realm);
        
        return securityHandler;
    }

    /**
     * Make a {@link UserStore} from a password file.
     * {@link PropertyUserStore} for details.  
     */
    public static UserStore makeUserStore(String passwordFile) {
        PropertyUserStore propertyUserStore = new PropertyUserStore();
        propertyUserStore.setConfig(passwordFile);
        propertyUserStore.setHotReload(false);
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
        String[] roles = role == null ? null : new String[]{role};
        Credential cred  = new Password(password);
        userStore.addUser(user, cred, roles);
        try { userStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }
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
