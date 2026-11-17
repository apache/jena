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

# LDForge integration review

## Functional validation

- Production Vite build: passed.
- Unit tests: 52 passed.
- Mock-backed Cypress suite: 18 passed, including the resize-handle test.
- Real Fuseki 6.0.0 query suite: 7 passed; see
  [reproduction instructions](../tests/integration/README.md).
- Modified JavaScript/Vue test and source files pass ESLint.

The packages are installed from npm and pinned in the lockfile. No local-source
aliases or temporary environment settings remain. These results do not cover
all deployment/authentication configurations or external map/CDN availability.

## Licensing

LDForge is MIT-licensed. Include its copyright notice and MIT license text in
the distribution's third-party license material.

The existing distribution inventory in
`jena-fuseki2/apache-jena-fuseki/dist/LICENSE` still names the old query-editor
packages. The `dist/make-license` helper uses Yarn 1 `licenses` commands, while the
project now uses Yarn 4. These distribution metadata/generation paths need updating
to reflect the LDForge integration.

`sparql-formatter` 1.0.2 declares MIT in its package metadata, but its installed npm
package contains no standalone LICENSE file. Confirm the upstream copyright and
license text when preparing the distribution's complete third-party notices.

## Build dependency inventory

The following inventory was extracted from the Vite production source maps and
installed package metadata. It identifies packages represented
in the generated JavaScript, including lazy chunks; it is a metadata audit, not a
complete legal review or an inventory of remotely fetched map assets. Existing
Font Awesome icon licensing is shown as declared by that package.

| Package | Version | Declared license |
| --- | --- | --- |
| `@codemirror/autocomplete` | 6.20.4 | MIT |
| `@codemirror/commands` | 6.11.1 | MIT |
| `@codemirror/lang-json` | 6.0.2 | MIT |
| `@codemirror/language` | 6.13.1 | MIT |
| `@codemirror/legacy-modes` | 6.5.5 | MIT |
| `@codemirror/lint` | 6.9.8 | MIT |
| `@codemirror/search` | 6.6.0 | MIT |
| `@codemirror/state` | 6.7.6 | MIT |
| `@codemirror/streamparser` | 6.0.0 | MIT |
| `@codemirror/view` | 6.43.15 | MIT |
| `@fortawesome/fontawesome-svg-core` | 7.3.1 | MIT |
| `@fortawesome/free-solid-svg-icons` | 7.3.1 | (CC-BY-4.0 AND MIT) |
| `@fortawesome/vue-fontawesome` | 3.3.3 | MIT |
| `@ldforge/core` | 1.0.1 | MIT |
| `@ldforge/editor` | 1.2.1 | MIT |
| `@ldforge/geo` | 1.1.1 | MIT |
| `@ldforge/prefixes` | 1.0.1 | MIT |
| `@ldforge/renderers` | 1.1.1 | MIT |
| `@lezer/common` | 1.5.5 | MIT |
| `@lezer/highlight` | 1.2.5 | MIT |
| `@lezer/json` | 1.0.3 | MIT |
| `@lezer/lr` | 1.4.12 | MIT |
| `@marijn/find-cluster-break` | 1.0.2 | MIT |
| `@popperjs/core` | 2.11.8 | MIT |
| `@vue/reactivity` | 3.5.44 | MIT |
| `@vue/runtime-core` | 3.5.44 | MIT |
| `@vue/runtime-dom` | 3.5.44 | MIT |
| `@vue/shared` | 3.5.44 | MIT |
| `axios` | 1.21.0 | MIT |
| `bootstrap` | 5.3.8 | MIT |
| `call-bind-apply-helpers` | 1.0.2 | MIT |
| `call-bound` | 1.0.4 | MIT |
| `codemirror` | 6.0.2 | MIT |
| `codemirror-lang-sparql` | 2.1.0 | MIT |
| `crelt` | 1.0.6 | MIT |
| `dunder-proto` | 1.0.1 | MIT |
| `es-define-property` | 1.0.1 | MIT |
| `es-errors` | 1.3.0 | MIT |
| `es-object-atoms` | 1.1.1 | MIT |
| `function-bind` | 1.1.2 | MIT |
| `get-intrinsic` | 1.3.0 | MIT |
| `get-proto` | 1.0.1 | MIT |
| `gopd` | 1.2.0 | MIT |
| `has-symbols` | 1.1.0 | MIT |
| `hasown` | 2.0.2 | MIT |
| `math-intrinsics` | 1.1.0 | MIT |
| `mitt` | 3.0.1 | MIT |
| `n3` | 2.13.12 | MIT |
| `object-inspect` | 1.13.4 | MIT |
| `qs` | 6.17.1 | BSD-3-Clause |
| `side-channel` | 1.1.1 | MIT |
| `side-channel-list` | 1.0.1 | MIT |
| `side-channel-map` | 1.0.1 | MIT |
| `side-channel-weakmap` | 1.0.2 | MIT |
| `sparql-formatter` | 1.0.2 | MIT |
| `style-mod` | 4.1.3 | MIT |
| `vue-router` | 5.4.0 | MIT |
| `vue-upload-component` | 3.2.2 | Apache-2.0 |
| `w3c-keyname` | 2.2.8 | MIT |
