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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * Represents form login credentials
 * 
 */
public class FormLogin {

    private String loginForm, loginUserField, loginPasswordField, username;
    private char[] password;
    private CookieStore cookies;

    /**
     * Creates new form login credentials
     * 
     * @param loginFormURL
     *            Login Form URL
     * @param loginUserField
     *            Login Form User field name
     * @param loginPasswordField
     *            Login Form Password field name
     * @param username
     *            User name
     * @param password
     *            Password
     */
    public FormLogin(String loginFormURL, String loginUserField, String loginPasswordField, String username, char[] password) {
        this.loginForm = loginFormURL;
        this.loginUserField = loginUserField;
        this.loginPasswordField = loginPasswordField;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the login form URL
     * 
     * @return Login Form URL
     */
    public String getLoginFormURL() {
        return this.loginForm;
    }

    /**
     * Gets the HTTP Entity for the Login request
     * 
     * @return Login request entity
     * @throws UnsupportedEncodingException
     *             Thrown if the platform does not support UTF-8
     */
    public HttpEntity getLoginEntity() throws UnsupportedEncodingException {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(this.loginUserField, this.username));
        nvps.add(new BasicNameValuePair(this.loginPasswordField, new String(this.password)));

        return new UrlEncodedFormEntity(nvps, "UTF-8");
    }

    /**
     * Gets whether any cookies are associated with this login
     * 
     * @return True if there are cookies, false otherwise
     */
    public boolean hasCookies() {
        return this.cookies != null;
    }

    /**
     * Gets cookies associated with this login
     * 
     * @return Cookies
     */
    public CookieStore getCookies() {
        return this.cookies;
    }

    /**
     * Sets cookies associated with this login
     * 
     * @param cookies
     */
    public void setCookies(CookieStore cookies) {
        this.cookies = cookies;
    }

    /**
     * Clears cookies associated with login, may be useful if you need to force
     * a fresh login attempt for any reason.
     */
    public void clearCookies() {
        this.cookies = null;
    }
}
