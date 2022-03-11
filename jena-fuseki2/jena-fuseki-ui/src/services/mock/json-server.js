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

const data = require('./json/index')

const jsonServer = require('json-server')

const server = jsonServer.create()
const router = jsonServer.router(data)
const middlewares = jsonServer.defaults()

server.use(middlewares)

// Have all URLS prefixed with /$
server.use(
  jsonServer.rewriter({
    '/$/*': '/$1',
    '/stats/ds1': '/ds1'
  })
)

/**
 * Render a response. This can be used to customize outputs,
 * map response parameters to other values.
 * @see https://www.rahulpnath.com/blog/setting-up-a-fake-rest-api-using-json-server/
 * @param req - express HTTP request
 * @param res - express HTTP response
 */
// router.render = (req, res) => {
//   // This is the original response.
//   const responseData = res.locals.data || {}
//   res.status(200).jsonp(responseData)
// }

server.use(router)

server.listen(3030, () => {
  console.log('JSON Server is running')
})
