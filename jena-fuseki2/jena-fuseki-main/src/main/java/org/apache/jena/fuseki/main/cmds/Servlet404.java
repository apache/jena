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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.cmds;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.web.HttpSC;

/** 404 for HEAD/GET/POST/PUT */
public class Servlet404 extends HttpServlet {
    public Servlet404() {}

    // service()?
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        err404(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        err404(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        err404(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        err404(req, resp);
    }

    // protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    // protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
    // protected void doOptions(HttpServletRequest req, HttpServletResponse resp)

    private static void err404(HttpServletRequest req, HttpServletResponse response) {
        try {
            response.sendError(HttpSC.NOT_FOUND_404, HttpSC.getMessage(HttpSC.NOT_FOUND_404));
        } catch (IOException ex) {}
    }
}
