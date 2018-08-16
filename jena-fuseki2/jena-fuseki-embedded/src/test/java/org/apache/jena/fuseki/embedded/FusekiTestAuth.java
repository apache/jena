/**
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

package org.apache.jena.fuseki.embedded;

import java.util.Objects;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;
import org.junit.Assert;

/**
 * Testing HTTP athentication.
 * <p>
 * {@code FusekiTestAuth} provides helper code for before/after (any of suite/class/test).
 * The pattern of usage is:
 * <pre>
 * 
 * &#64;BeforeClass
 * public static void beforeClassAuth() {
 *     SecurityHandler sh = FusekiTestAuth.makeSimpleSecurityHandler("/*", "USER", "PASSWORD");
 *     FusekiTestAuth.setupServer(true, sh);
 * }
 * 
 * &#64;AfterClass
 * public static void afterClassAuth() {
 *     FusekiTestAuth.teardownServer();
 *     // Clear up any pooled connections.
 *     HttpOp.setDefaultHttpClient(HttpOp.createPoolingHttpClient());
 * }
 * 
 * &#64;Test
 * public void myAuthTest() {
 *     BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
 *     Credentials credentials = new UsernamePasswordCredentials("USER", "PASSWORD");
 *     credsProvider.setCredentials(AuthScope.ANY, credentials);
 *     HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
 *     try (TypedInputStream in = HttpOp.execHttpGet(ServerCtl.urlDataset(), "* /*", client, null)) {}
 * }
 * 
 * &#64;Test
 * public void myAuthTestRejected() {
 *     BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
 *     Credentials credentials = new UsernamePasswordCredentials("USER", "PASSWORD");
 *     credsProvider.setCredentials(AuthScope.ANY, credentials);
 *     HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
 *     try (TypedInputStream in = HttpOp.execHttpGet(ServerCtl.urlDataset(), "* /*", client, null)) {}
 *     catch (HttpException ex) {
 *         throw assertAuthHttpException(ex);
 *     }
 * }
 * </pre>
 * 
 * {@code @BeforeClass} can be {@code @Before} but server stop-start is expensive so a
 * large test suite may end up quite slow.
 */
public class FusekiTestAuth {
    private static int currentPort = FusekiLib.choosePort() ;
    
    public static int port() {
        return currentPort ;
    }
    
    static boolean CLEAR_DSG_DIRECTLY = true ;
    static private DatasetGraph dsgTesting ;
    
    // Abstraction that runs a SPARQL server for tests.
    public static final String urlRoot()            { return "http://localhost:"+port()+"/" ; }
    public static final String datasetPath()        { return "/dataset" ; }
    public static final String urlDataset()         { return "http://localhost:"+port()+datasetPath() ; }
    public static final DatasetGraph getDataset()   { return dsgTesting ; }
    
    public static final String serviceUpdate()      { return "http://localhost:"+port()+datasetPath()+"/update" ; } 
    public static final String serviceQuery()       { return "http://localhost:"+port()+datasetPath()+"/query" ; }
    public static final String serviceGSP()         { return "http://localhost:"+port()+datasetPath()+"/data" ; }
    
    private static FusekiServer server ;

    /** Setup a testing server, using the given  Jetty {@link SecurityHandler} for authentication. 
     * The server will have an empty, in-emory transactional dataset.
     */
    public static void setupServer(boolean updateable, SecurityHandler sh) {
        setupServer(updateable, sh, DatasetGraphFactory.createTxnMem());
    }
    
    /** Setup a testing server, using the given  Jetty {@link SecurityHandler} for authentication. 
     */
    public static void setupServer(boolean updateable, SecurityHandler sh, DatasetGraph dsg) {
        dsgTesting = dsg;
        server = FusekiServer.create()
            .add(datasetPath(), dsgTesting)
            .port(port())
            .securityHandler(sh)
            .build()
            .start();
    }
    
    /** Shutdown the server.*/
    public static void teardownServer() {
        if ( server != null ) {
            server.stop() ;
            server = null ;
            dsgTesting = null;
        }
    }

    /** Create a Jetty {@link SecurityHandler} for basic authentication, one user/password/role. */
    public static SecurityHandler makeSimpleSecurityHandler(String pathSpec, String user, String password) {
            return makeSimpleSecurityHandler(pathSpec, null, user, password, "FusekiTestRole");
    }

    /** Create a Jetty {@link SecurityHandler} for basic authentication, one user/password/role. */
    public static SecurityHandler makeSimpleSecurityHandler(String pathSpec, String realm, String user, String password, String role) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        Objects.requireNonNull(role);
        
        Constraint constraint = new Constraint() ;
        constraint.setName(Constraint.__BASIC_AUTH) ;
        String[] roles = new String[]{role};
        constraint.setRoles(roles) ;
        constraint.setAuthenticate(true) ;

        ConstraintMapping mapping = new ConstraintMapping() ;
        mapping.setConstraint(constraint) ;
        mapping.setPathSpec("/*") ;

        IdentityService identService = new DefaultIdentityService() ;
        
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler() ;
        securityHandler.addConstraintMapping(mapping) ;
        securityHandler.setIdentityService(identService) ;
        
        UserStore userStore = makeUserStore(user, password, role);
        
        HashLoginService loginService = new HashLoginService("Fuseki Authentication") ;
        loginService.setUserStore(userStore);
        loginService.setIdentityService(identService) ;
        
        securityHandler.setLoginService(loginService) ;
        securityHandler.setAuthenticator(new BasicAuthenticator()) ;
        if ( realm != null )
            securityHandler.setRealmName(realm);
        
        return securityHandler;
    }

    /** Very simple! */
    private static UserStore makeUserStore(String user, String password, String role) {
        Credential cred  = new Password(password);
        PropertyUserStore propertyUserStore = new PropertyUserStore();
        String[] roles = role == null ? null : new String[]{role};
        propertyUserStore.addUser(user, cred, roles);
        try { propertyUserStore.start(); }
        catch (Exception ex) { throw new FusekiException("UserStore", ex); }
        return propertyUserStore;
    }

    /** Assert that an {@code HttpException} ias an authorization failure.
     * This is normally 403.  401 indicates no retryu with credentials.
     */
    public static HttpException assertAuthHttpException(HttpException ex) {
        int rc = ex.getResponseCode();
        Assert.assertTrue(rc == HttpSC.FORBIDDEN_403 || rc == HttpSC.UNAUTHORIZED_401 );
        return ex;
    }
}
