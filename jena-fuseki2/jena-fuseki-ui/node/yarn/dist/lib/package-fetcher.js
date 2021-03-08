'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _errors;

function _load_errors() {
  return _errors = require('./errors.js');
}

var _index;

function _load_index() {
  return _index = _interopRequireWildcard(require('./fetchers/index.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('./util/fs.js'));
}

var _promise;

function _load_promise() {
  return _promise = _interopRequireWildcard(require('./util/promise.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class PackageFetcher {
  constructor(config, resolver) {
    this.reporter = config.reporter;
    this.resolver = resolver;
    this.config = config;
  }

  fetchCache(dest, fetcher) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      var _ref = yield _this.config.readPackageMetadata(dest);

      const hash = _ref.hash;
      const pkg = _ref.package;

      return {
        package: pkg,
        resolved: yield fetcher.getResolvedFromCached(hash),
        hash,
        dest
      };
    })();
  }

  fetch(ref) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const dest = _this2.config.generateHardModulePath(ref);

      const remote = ref.remote;
      const Fetcher = (_index || _load_index())[remote.type];
      if (!Fetcher) {
        throw new (_errors || _load_errors()).MessageError(`Unknown fetcher for ${ remote.type }`);
      }

      const fetcher = new Fetcher(dest, remote, _this2.config);

      if (yield _this2.config.isValidModuleDest(dest)) {
        return _this2.fetchCache(dest, fetcher);
      }

      // remove as the module may be invalid
      yield (_fs || _load_fs()).unlink(dest);

      try {
        return yield fetcher.fetch();
      } catch (err) {
        try {
          yield (_fs || _load_fs()).unlink(dest);
        } catch (err2) {
          // what do?
        }
        throw err;
      }
    })();
  }

  maybeFetch(ref) {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      try {
        return yield _this3.fetch(ref);
      } catch (err) {
        if (ref.optional) {
          _this3.reporter.error(err.message);
          return null;
        } else {
          throw err;
        }
      }
    })();
  }

  init() {
    var _this4 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const pkgs = _this4.resolver.getPackageReferences();
      const tick = _this4.reporter.progress(pkgs.length);

      yield (_promise || _load_promise()).queue(pkgs, (() => {
        var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (ref) {
          const res = yield _this4.maybeFetch(ref);
          let newPkg;

          if (res) {
            newPkg = res.package;

            // update with new remote
            ref.remote.hash = res.hash;
            if (res.resolved) {
              ref.remote.resolved = res.resolved;
            }
          }

          if (newPkg) {
            // update with fresh manifest
            yield _this4.resolver.updateManifest(ref, newPkg);
          }

          if (tick) {
            tick(ref.name);
          }
        });

        return function (_x) {
          return _ref2.apply(this, arguments);
        };
      })());
    })();
  }
}
exports.default = PackageFetcher;