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

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import istanbul from "vite-plugin-istanbul";
const path = require("path")

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    istanbul({
      include: "src/*",
      exclude: ["node_modules"],
      extension: [".js", ".jsx", ".ts", ".vue"],
      requireEnv: true,
      cypress: true
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "~codemirror": "codemirror",
      "~@triply/yasqe": "@triply/yasqe",
      "~@triply/yasr": "@triply/yasr",
    },
  },
  build: {
    // Our largest chunk: dist/assets/yasqe.min-ec8f4984.js 508.16 kB │ gzip: 130.97 kB
    chunkSizeWarningLimit: 550,
    // Change build paths to make them Maven compatible.
    outDir: 'target/webapp',
    assetsDir: 'static',
    sourcemap: 'inline',
  },
  server: {
    port: 8080,
    // The proxy is used for the `dev` target, for e2e tests, rapid development in the IDE, etc.
    proxy: {
      '/': {
        target: `http://localhost:${process.env.FUSEKI_PORT || 3030}`,
        changeOrigin: true,
        secure: false,
        ws: true,
        bypass: (req, res, options) => {
          const accept = req.headers.accept
          const contentType = req.headers['content-type']
          // webpack-dev-server automatically handled fall-through, as it was requested (and quickly
          // closed - rejected) on this issue: https://github.com/vitejs/vite/issues/10114
          // And might be available via a flag in the future after this PR: https://github.com/http-party/node-http-proxy/pull/1415
          // So we bypass requests from the proxy that do not contain the header Accept: application/json.*,
          // or that are requesting /node_modules/ (dev Vite/Vue/JS modules).
          const sendToUI =
            req.method !== 'POST' &&
            (
              (req.hasOwnProperty('originalUrl') && req.originalUrl.includes('node_modules')) ||
              (
                (accept !== undefined && accept !== null) &&
                !(accept.includes('application/json') || accept.includes('text/turtle') || accept.includes('application/sparql-results+json')
              )) ||
              ((contentType !== undefined && contentType !== null) && !contentType.startsWith('multipart/form-data'))
            )
          if (sendToUI) {
            // Send it to the UI.
            // console.log(`UI: request to ${req.originalUrl} accept [${accept}] content type [${contentType}]`)
            return req.originalUrl
          }
          // Send it to the backend.
          // console.log(`Fuseki: proxying request to ${req.originalUrl}`)
          return null
        }
      }
    },
  }
})
