# Jena Fuseki UI

Jena Fuseki UI is a Vue 3 application built with Vue Router, Vite, Bootstrap 5 (and Popper),
Vue components (Vue Upload component and custom components for Jena), FontAwesome (icons),
Axios (HTTP client), and YASGUI (YASQE and YASR) for SPARQL query editor with code syntax
highlighting and auto-complete.

It interfaces with the backend application, Jena Fuseki servlets, via HTTP requests with
YASGUI and Axios.

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

