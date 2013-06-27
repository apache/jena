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
package org.apache.jena.atlas.web.auth;

/**
 * Represents form login credentials where form logins are against Apache
 * mod_auth_form secured servers using default form field configuration
 * 
 */
public class ApacheModAuthFormLogin extends FormLogin {

    private static final String APACHE_MOD_AUTH_FORM_USER_FIELD = "httpd_username";
    private static final String APACHE_MOD_AUTH_FORM_PASSWORD_FIELD = "httpd_password";

    /**
     * Creates new form login credentials
     * 
     * @param loginFormURL
     *            Login Form URL
     * @param username
     *            User name
     * @param password
     *            Password
     */
    public ApacheModAuthFormLogin(String loginFormURL, String username, char[] password) {
        super(loginFormURL, APACHE_MOD_AUTH_FORM_USER_FIELD, APACHE_MOD_AUTH_FORM_PASSWORD_FIELD, username, password);
    }
}
