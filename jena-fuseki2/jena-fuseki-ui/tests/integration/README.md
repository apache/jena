<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Real Fuseki query integration tests

These opt-in tests run the Fuseki UI against an actual Fuseki server. They are
outside the normal mock-backed Cypress spec pattern and do not run during the
default `test:e2e` command.

## Reproduce

Use a separate Fuseki instance and an empty in-memory dataset called `validation`.
Do not run the fixture upload against a dataset containing valuable data.
For an installed Fuseki distribution, start the server in its own terminal:

```sh
/path/to/fuseki-server --port=3034 --mem --update /validation
```

Load the small test graph:

```sh
curl --fail --silent --show-error \
  -X POST 'http://localhost:3034/validation/data?default' \
  -H 'Content-Type: text/turtle' \
  --data-binary @tests/integration/fixtures/data.ttl
```

Start the UI in another terminal:

```sh
PORT=8084 FUSEKI_PORT=3034 corepack yarn run dev
```

Run the real-server suite:

```sh
PORT=8084 corepack yarn exec cypress run \
  --config specPattern=tests/integration/query-real.cy.js
```

Stop the separate UI/server processes afterwards. The in-memory graph is discarded
when that Fuseki instance exits.

## Recorded result

On 16 November 2026, all seven tests passed against Apache Jena Fuseki 6.0.0,
with `@ldforge/editor` 1.2.1, core 1.0.1 and renderers/geo 1.1.1 installed from npm.
The test server ran in a separate container using the locally available
`mlindeman/jena` image, bound to `127.0.0.1:3034`; the existing Fuseki instance and
its data were not modified.

Coverage: SELECT result rendering and language/datatype metadata, true and false
ASK results, CONSTRUCT and DESCRIBE graph parsing/rendering, empty results and a
real HTTP 400 syntax error. Graph responses negotiated Turtle/N-Triples through
the published executor. External map/CDN loading and authentication configurations
are outside this suite. Administrative endpoints such as stats may require
additional server permissions; the query tests do not bypass those permissions.
