# Jena Fuseki UI

Jena Fuseki UI is a Vue 3 application built with Vue Router, Vite, Bootstrap 5 (and Popper),
Vue components (Vue Upload component and custom components for Jena), FontAwesome (icons),
Axios (HTTP client), and LDForge for the SPARQL query editor and result views, with syntax
highlighting and auto-complete.

It interfaces with the backend application, Jena Fuseki servlets, via HTTP requests with
LDForge and Axios.

## Build

`package.json` contains the following scripts:

- `dev`
  - Serves the application with Vite using Hot Module Reload (ideal for rapid development in an IDE).
- `serve`
  - Runs Vite to preview the application from the build directory (useful to preview a production build, for example).
- `build`
  - Builds the application (you can choose the mode, for production, development, etc.).
- `test:unit`
  - Runs unit tests with Vitest.
- `test:e2e`
  - Runs e2e tests with Cypress.
- `lint`
  - Lints the code (`eslint`)
- `coverage:unit`
  - Runs unit tests with Vitest and tracks coverage.
- `coverage:e2e`
  - Runs e2e tests with Cypress and tracks coverage.
- `serve:fuseki`
  - Starts a fake Fuseki backend with responses saves from a real instance. Used to test the UI without the backend, and for e2e tests.
- `serve:offline`
  - Runs both the serve:offline and the serve scripts to serve the UI offline.

For every command above that starts Vite (default port `8080`) or Fuseki (default port `3030`), you
can customize the ports used. For example:

```bash
FUSEKI_PORT=9999 yarn run serve:fuseki
PORT=1313 FUSEKI_PORT=9999 yarn run serve:offline
FUSEKI_PORT=3031 PORT=8081 yarn run test:e2e
...
```

### Using Vitest UI

[Vitest UI](https://vitest.dev/guide/ui.html) is a user interface
provided by Vitest that can be used to visualize tests and coverage.

It is similar to the Maven Surefire Plug-in, but in a more interactive
web page, where you can visualize the test code, its coverage, and the
code related to that test in a visual graph.

```bash
$ # To run Vitest UI
$ npx vitest --ui
$ # If you want to look at the coverage
$ npx vitest --ui --coverage.enabled=true
```

> [!NOTE]
> This command is only available for the unit tests. For e2e tests
> you must still use `yarn run coverage:e2e`, as it uses Cypress
> with Istanbul to measure the coverage, instead of vitest only.


## SPARQL editor

The query view bundles the npm packages `@ldforge/editor`, `@ldforge/core`,
`@ldforge/renderers` and `@ldforge/geo`. The editor is pinned to 1.1.0 and the
other packages to 1.0.0 in `package.json` and `yarn.lock`. Development and
production use the same installed packages.
Vite includes their JavaScript and editor CSS in the application build.

Fuseki provides the dataset endpoint, example queries and prefix badges. LDForge
provides query tabs, completion, formatting, prefix editing, cancellation, result
views and the toolbar share action. Shared links carry the query and endpoint;
existing `#/dataset/<name>/query?query=...` links are still accepted. The active
query text is saved per dataset under `fuseki:ldforge:query:<dataset>`; query tabs
and results are session-only. Editor resources are released on navigation.

The editor uses the blue from the Fuseki logo through `themeMainColor`. Table,
Boolean, JSON and Geo result renderers are registered. The JSON view displays the
normalized result model. SELECT/ASK use SPARQL Results JSON by default, and graph
queries request Turtle/N-Triples. Geo uses LDForge geometry bindings and loads
external map assets only when needed. The editor height can be adjusted with the
resize handle or its keyboard controls. The previous response-format dropdowns are not part of this integration.

### Preview without Java

Run these commands in two terminals in this directory:

```sh
corepack yarn install --immutable
FUSEKI_PORT=3030 corepack yarn run serve:fuseki
```

```sh
PORT=8081 FUSEKI_PORT=3030 corepack yarn run dev
```

Open http://localhost:8081, create a dataset under Manage, then open Query.
The mock server returns fixtures; it does not evaluate SPARQL or store RDF.
For real queries, omit `serve:fuseki` and set `FUSEKI_PORT` to the port of a running
Fuseki server. Real-server ASK/graph execution and map rendering require separate
integration validation.

The LDForge packages declare EUPL-1.2. Dependency license/NOTICE review remains
necessary before proposing upstream distribution.
