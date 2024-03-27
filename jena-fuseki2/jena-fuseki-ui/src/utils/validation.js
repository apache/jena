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
 * Utility functions for data validation in the UI.
 *
 * We must avoid repeating the validation when the backend server is handling this,
 * unless we really want to do that (avoid large payloads, for security, etc.).
 */

/**
 * Validates a Jena UI graph name.
 *
 * @param {string} graphName - The name of the graph provided by user or configuration file.
 * @return {boolean} - true iff the given name of the graph provided is valid for Jena.
 */
export function validateGraphName (graphName) {
  if (graphName === '' || graphName.trim() === '') {
    return false
  }
  // No spaces allowed in graph names.
  const pattern = /^\S+$/
  if (!pattern.test(graphName)) {
    return false
  }
  // Only valid URIs allowed.
  try {
    new URL(graphName)
  } catch {
    return false
  }
  // If it reached this part, then it's a valid graph name.
  return true
}
