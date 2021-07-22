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

package org.apache.jena.test.conn;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.web.HttpSC;

/**
 * A simple servlet for testing client-side code;
 * maintains a string manipulated by all HTTP operations.
 */
public class StringHolderServlet extends HttpServlet {

    private AtomicReference<String> content = new AtomicReference<>("");

    // Direct calls.
    public void clear() { set(""); }
    public void set(String str) { content.set(str); }
    public String get() { return content.get(); }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ( "PATCH".equals(req.getMethod()) ) {
            doPatch(req, resp);
            return;
        }
        super.service(req, resp);
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String x = IO.readWholeFileAsUTF8(req.getInputStream());
        content.setOpaque(content.get() + x);
        resp.setStatus(HttpSC.OK_200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpSC.OK_200);
        resp.getOutputStream().print(get());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String x = IO.readWholeFileAsUTF8(req.getInputStream());
        content.setOpaque(content.get() + x);
        resp.setStatus(HttpSC.OK_200);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String x = IO.readWholeFileAsUTF8(req.getInputStream());
        content.setOpaque(x);
        resp.setStatus(HttpSC.OK_200);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        clear();
        resp.setStatus(HttpSC.OK_200);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpSC.OK_200);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpSC.OK_200);
    }
}