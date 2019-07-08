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

package org.apache.jena.fuseki.mgt;

/**
 * Various constants used in the management API functions and JSON responses in the
 * webapp/full server.
 */
public class ServerMgtConst {
    public static final String  opDatasets      = "datasets";
    public static final String  opListBackups   = "backups-list";
    public static final String  opServer        = "server";

    public static final String uptime           = "uptime";
    public static final String startDT          = "startDateTime";
//    public static final String server           = "server";
//    public static final String port             = "port";
    public static final String hostname         = "hostname";
    public static final String admin            = "admin";
    public static final String version          = "version";
    public static final String built            = "built";
    public static final String services         = "services";
}
