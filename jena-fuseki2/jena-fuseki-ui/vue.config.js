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

module.exports = {
  publicPath: '',
  chainWebpack: config => {
    config
      .plugin('html')
      .tap(args => {
        args[0].title = 'Apache Jena Fuseki'
        return args
      })

    if (process.env.NODE_ENV !== 'production') {
      // devtool for test and other modes
      // https://webpack.js.org/configuration/devtool/
      if (process.env.NODE_ENV === 'test') {
        // NOTE: if you need to debug the project with WebStorm (or another IDE) and it fails, try
        //       change this value for config.devtool('eval-source-map')
        config.devtool('eval')
      } else {
        config.devtool('eval-source-map')
      }

      // coverage
      if (process.env.NODE_ENV === 'test') {
        config.module.rule('istanbul')
          .test(/\.js$/)
          .include.add(path.resolve('src')).end()
          .use('istanbul-instrumenter-loader')
          .loader('istanbul-instrumenter-loader')
          .options({ esModules: true })
          .after('cache-loader')
      }

      // resolve modules in devtool
      config.output
        .devtoolModuleFilenameTemplate('[absolute-resource-path]')
        .devtoolFallbackModuleFilenameTemplate('[absolute-resource-path]?[hash]')
    }
  },
  // see https://cli.vuejs.org/config/#devserver-proxy
  devServer: {
    proxy: {
      '/': {
        target: 'http://localhost:3030',
        ws: true,
        changeOrigin: true
      }
    },
    client: false,
    webSocketServer: false
  },
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: 'target/dist',
  assetsDir: 'static',
  css: {
    loaderOptions: {
      sass: {
        sassOptions: {
          quietDeps: [
            'node_modules/bootstrap/**/*.scss'
          ]
        }
      }
    }
  }
}
