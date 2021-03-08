'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _registryResolver;

function _load_registryResolver() {
  return _registryResolver = _interopRequireDefault(require('./registry-resolver.js'));
}

var _npmRegistry;

function _load_npmRegistry() {
  return _npmRegistry = _interopRequireDefault(require('../../registries/npm-registry.js'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('../../util/map.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _constants;

function _load_constants() {
  return _constants = require('../../constants.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const path = require('path');

const NPM_REGISTRY = /http[s]:\/\/registry.npmjs.org/g;

class NpmResolver extends (_registryResolver || _load_registryResolver()).default {

  static findVersionInRegistryResponse(config, range, body) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (range in body['dist-tags']) {
        range = body['dist-tags'][range];
      }

      const satisfied = yield config.resolveConstraints((0, (_keys || _load_keys()).default)(body.versions), range);
      if (satisfied) {
        return body.versions[satisfied];
      } else {
        throw new (_errors || _load_errors()).MessageError(`Couldn't find any versions for ${ body.name } that matches ${ range }. ` + `Possible versions: ${ (0, (_keys || _load_keys()).default)(body.versions).join(', ') }`);
      }
    })();
  }

  resolveRequest() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (_this.config.offline) {
        const res = _this.resolveRequestOffline();
        if (res != null) {
          return res;
        }
      }

      const body = yield _this.config.registries.npm.request((_npmRegistry || _load_npmRegistry()).default.escapeName(_this.name));

      if (body) {
        return yield NpmResolver.findVersionInRegistryResponse(_this.config, _this.range, body);
      } else {
        return null;
      }
    })();
  }

  resolveRequestOffline() {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // find modules of this name
      const prefix = `npm-${ _this2.name }-`;

      const cacheFolder = _this2.config.cacheFolder;
      invariant(cacheFolder, 'expected packages root');

      const files = yield _this2.config.getCache('cachedPackages', (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
        const files = yield (_fs || _load_fs()).readdir(cacheFolder);
        const validFiles = [];

        for (const name of files) {
          // no hidden files
          if (name[0] === '.') {
            continue;
          }

          // ensure valid module cache
          const dir = path.join(cacheFolder, name);
          if (yield _this2.config.isValidModuleDest(dir)) {
            validFiles.push(name);
          }
        }

        return validFiles;
      }));

      const versions = (0, (_map || _load_map()).default)();

      for (const name of files) {
        // check if folder starts with our prefix
        if (name.indexOf(prefix) !== 0) {
          continue;
        }

        const dir = path.join(cacheFolder, name);

        // read manifest and validate correct name
        const pkg = yield _this2.config.readManifest(dir, 'npm');
        if (pkg.name !== _this2.name) {
          continue;
        }

        // read package metadata
        const metadata = yield _this2.config.readPackageMetadata(dir);
        if (!metadata._remote) {
          continue; // old yarn metadata
        }

        versions[pkg.version] = (0, (_assign || _load_assign()).default)({}, pkg, { _remote: metadata.remote });
      }

      const satisfied = yield _this2.config.resolveConstraints((0, (_keys || _load_keys()).default)(versions), _this2.range);
      if (satisfied) {
        return versions[satisfied];
      } else if (!_this2.config.preferOffline) {
        throw new (_errors || _load_errors()).MessageError(_this2.reporter.lang('couldntFindPackageInCache', _this2.name, _this2.range, (0, (_keys || _load_keys()).default)(versions).join(', ')));
      } else {
        return null;
      }
    })();
  }

  cleanRegistry(url) {
    if (this.config.getOption('registry') === (_constants || _load_constants()).YARN_REGISTRY) {
      return url.replace(NPM_REGISTRY, (_constants || _load_constants()).YARN_REGISTRY);
    } else {
      return url;
    }
  }

  resolve() {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // lockfile
      const shrunk = _this3.request.getLocked('tarball');
      if (shrunk) {
        return shrunk;
      }

      const info = yield _this3.resolveRequest();
      if (info == null) {
        throw new (_errors || _load_errors()).MessageError(_this3.reporter.lang('packageNotFoundRegistry', _this3.name, 'npm'));
      }

      const deprecated = info.deprecated;
      const dist = info.dist;

      if (typeof deprecated === 'string') {
        let human = `${ info.name }@${ info.version }`;
        const parentNames = _this3.request.getParentNames();
        if (parentNames.length) {
          human = parentNames.concat(human).join(' > ');
        }
        _this3.reporter.warn(`${ human }: ${ deprecated }`);
      }

      if (dist != null && dist.tarball) {
        info._remote = {
          resolved: `${ _this3.cleanRegistry(dist.tarball) }#${ dist.shasum }`,
          type: 'tarball',
          reference: _this3.cleanRegistry(dist.tarball),
          hash: dist.shasum,
          registry: 'npm'
        };
      }

      info._uid = info.version;

      return info;
    })();
  }
}
exports.default = NpmResolver;
NpmResolver.registry = 'npm';