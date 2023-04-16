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

const path = require('path')
const vite = require('vite')

function vitePreprocessor (userConfigPath) {
  return async (file) => {
    const { filePath, outputPath } = file
    const fileName = path.basename(outputPath)
    const filenameWithoutExtension = path.basename(
      outputPath,
      path.extname(outputPath)
    )

    const defaultConfig = vite.defineConfig({
      logLevel: 'warn',
      define: {
        'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
      },
      build: {
        emptyOutDir: false,
        minify: false,
        outDir: path.dirname(outputPath),
        sourcemap: true,
        write: true,
        rollupOptions: {
          output: {
            inlineDynamicImports: false
          }
        },
        lib: {
          entry: filePath,
          fileName: () => fileName,
          formats: ['es'],
          name: filenameWithoutExtension
        }
      }
    })

    await vite.build({
      configFile: userConfigPath,
      ...defaultConfig
    })

    return outputPath
  }
}

module.exports = vitePreprocessor
