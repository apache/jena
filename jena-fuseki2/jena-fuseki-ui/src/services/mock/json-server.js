/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the 'License'); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const PORT = 3030

const data = {}

const jsonServer = require('json-server')

const server = jsonServer.create()
const router = jsonServer.router(data)
const middlewares = jsonServer.defaults()

server.use(middlewares)
// To handle POST, PUT and PATCH you need to use a body-parser
// You can use the one used by JSON Server
server.use(jsonServer.bodyParser)

const DATASETS = {}

const builtTime = new Date()
const startTime = new Date()

// Add custom routes before JSON Server router

// GET PING STATUS
server.get('/\\$/ping', (req, res) => {
  res.jsonp(new Date().toISOString())
})

// GET SERVER INFO
server.get('/\\$/server', (req, res) => {
  res.jsonp({
    version: '3.14.0',
    built: builtTime.toISOString(),
    startDateTime: startTime.toISOString(),
    uptime: parseInt(`${(new Date() - startTime) / 1000}`),
    datasets: Object.values(DATASETS)
  })
})

// GET TASKS
server.get('/\\$/tasks', (req, res) => {
  res.jsonp([])
})

// CREATE DATASET
server.post('/\\$/datasets', (req, res) => {
  const datasetName = req.body.dbName
  if (DATASETS[datasetName]) {
    res.status(409).send('Error 409: Name already registered /a')
    return
  }
  DATASETS[datasetName] = {
    'ds.name': `/${datasetName}`,
    'ds.state': true,
    'ds.services': [
      {
        'srv.type': 'gsp-rw',
        'srv.description': 'Graph Store Protocol',
        'srv.endpoints': ['data']
      },
      {
        'srv.type': 'query',
        'srv.description': 'SPARQL Query',
        'srv.endpoints': [
          'sparql',
          '',
          'query'
        ]
      },
      {
        'srv.type': 'gsp-r',
        'srv.description': 'Graph Store Protocol (Read)',
        'srv.endpoints': ['get']
      },
      {
        'srv.type': 'update',
        'srv.description': 'SPARQL Update',
        'srv.endpoints': [
          'update',
          ''
        ]
      },
      {
        'srv.type': 'upload',
        'srv.description': 'File Upload',
        'srv.endpoints': ['upload']
      }
    ]
  }
  res.sendStatus(200)
})

// GET DATASET DATA
server.get('/\\$/stats/:datasetName', (req, res) => {
  const datasetName = req.params.datasetName
  res
    .status(200)
    .jsonp({
      datasets: {
        [`/${datasetName}`]: {
          Requests: 2,
          RequestsGood: 1,
          RequestsBad: 1,
          endpoints: {
            data: {
              RequestsBad: 1,
              Requests: 1,
              RequestsGood: 0,
              operation: 'gsp-rw',
              description: 'Graph Store Protocol'
            },
            sparql: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'query',
              description: 'SPARQL Query'
            },
            query: {
              RequestsBad: 0,
              Requests: 1,
              RequestsGood: 1,
              operation: 'query',
              description: 'SPARQL Query'
            },
            _1: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'query',
              description: 'SPARQL Query'
            },
            get: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'gsp-r',
              description: 'Graph Store Protocol (Read)'
            },
            _2: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'update',
              description: 'SPARQL Update'
            },
            update: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'update',
              description: 'SPARQL Update'
            },
            upload: {
              RequestsBad: 0,
              Requests: 0,
              RequestsGood: 0,
              operation: 'upload',
              description: 'File Upload'
            }
          }
        }
      }
    })
})

// SPARQL QUERY
const sparqlCallback = (req, res) => {
  const count = 42
  const bindings = []
  const vars = []
  const query = req.query.query
  // If a query was provided in the URL as ?query=..., then it's either
  // counting the triples (Info), or listing existing graphs (Edit).
  if (query) {
    if (query.startsWith('select ?g')) {
      vars.push('count', 'g')
    } else {
      vars.push('count')
      bindings.push({
        count: {
          type: 'literal',
          datatype: 'http://www.w3.org/2001/XMLSchema#integer',
          value: count
        }
      })
    }
  } else {
    vars.push('subject', 'predicate', 'object')
    bindings.push({
      subject: {
        type: 'uri',
        value: 'https://jena.apache.org/#/tests/id/1'
      },
      predicate: {
        type: 'uri',
        value: 'https://jena.apache.org/#/Topic'
      },
      object: {
        type: 'literal',
        'xml:lang': 'en',
        value: 'Semantic Web'
      }
    },
    {
      subject: {
        type: 'uri',
        value: 'https://jena.apache.org/#/tests/id/2'
      },
      predicate: {
        type: 'uri',
        value: 'https://jena.apache.org/#/Topic'
      },
      object: {
        type: 'literal',
        'xml:lang': 'en',
        value: 'Knowledge Graph'
      }
    })
  }
  res
    .status(200)
    .jsonp({
      head: {
        vars
      },
      results: {
        bindings
      }
    })
}
server.get('/:datasetName/sparql', sparqlCallback)
server.post('/:datasetName/sparql', sparqlCallback)

// PING
server.get('/\\$/ping', (req, res) => {
  res.sendStatus(200)
})

// RESET TEST DATA
server.get('/tests/reset', (req, res) => {
  // Just delete the datasets to clean up for other tests to have a
  // brand new environment.
  for (const dataset in DATASETS) {
    delete DATASETS[dataset]
  }
  res.sendStatus(200)
})

server.use(router)

server.listen(PORT, () => {
  console.log('JSON Server is running')
})
