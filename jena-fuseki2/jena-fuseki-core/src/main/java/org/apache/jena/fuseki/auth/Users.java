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

package org.apache.jena.fuseki.auth;

public class Users {
    /**
     * Reserved user role name: Name of the user role for that has no access.
     */
    public static String UserDenyAll = "!";

    /**
     * Reserved user role name: Name of the user role for any authenticated user of the system.
     * In the servlet API, this equates to {@code getRemoteUser() != null}.
     */
    public static String UserAnyLoggedIn = "*";

    /**
     * Reserved user role name: Name of the user role for any authenticated user of the system
     * In the servlet API, this includes {@code getRemoteUser() == null}
     */
    public static String UserAnyAnon = "_";
}
