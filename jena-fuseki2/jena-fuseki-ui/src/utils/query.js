/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Utility functions for query components and views.
 *
 * Useful to separate code that can be easily tested without having to
 * instantiate a complete component or view.
 */

import qs from 'qs'

/**
 * Create a shareable link using the value of the SPARQL query in the
 * editor and compose a safe link to be used in the browser.
 *
 * Uses the Vue Route path instead of `document.location.hash`, which may
 * already contain a ?query=... parameter.
 *
 * Query parameters are encoded with qs.
 *
 * @param {string} query
 * @param {string} path
 * @return {string}
 */
export function createShareableLink (query, path) {
  return (
    document.location.protocol +
    '//' +
    document.location.host +
    document.location.pathname +
    document.location.search +
    '#' +
    path +
    '?' +
    // Encode the query as a URL parameter.
    qs.stringify({ query })
  )
}
