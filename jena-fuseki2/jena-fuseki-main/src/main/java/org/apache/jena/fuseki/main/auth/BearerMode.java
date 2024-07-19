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

/**
 * Variations for processing Bearer Authentication.
 * <ul>
 * <li>
 *  REQUIRED -- requests must have a bearer token. There must be a
 *  {@code Authorization: Bearer ...} header
 * </li>
 * <li>OPTIONAL -- requests may have a bearer token. Otherwise the request passes
 *     through and may be handled with another authentication mechanism. e.g. password.
 * <li>
 *   NONE -- requests must not have a {@code Authorization:} header.
 * </li>
 * </ul>
 */
public enum BearerMode { REQUIRED, OPTIONAL, NONE }