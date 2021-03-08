'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _validate;

function _load_validate() {
  return _validate = require('./util/normalize-manifest/validate.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('./lockfile/wrapper.js'));
}

var _packageReference;

function _load_packageReference() {
  return _packageReference = require('./package-reference.js');
}

var _packageReference2;

function _load_packageReference2() {
  return _packageReference2 = _interopRequireDefault(require('./package-reference.js'));
}

var _index;

function _load_index() {
  return _index = _interopRequireWildcard(require('./resolvers/index.js'));
}

var _index2;

function _load_index2() {
  return _index2 = require('./resolvers/index.js');
}

var _errors;

function _load_errors() {
  return _errors = require('./errors.js');
}

var _misc;

function _load_misc() {
  return _misc = require('./util/misc.js');
}

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('./constants.js'));
}

var _version;

function _load_version() {
  return _version = _interopRequireWildcard(require('./util/version.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

class PackageRequest {
  constructor(req, resolver) {
    this.parentRequest = req.parentRequest;
    this.visibility = req.visibility;
    this.lockfile = resolver.lockfile;
    this.registry = req.registry;
    this.reporter = resolver.reporter;
    this.resolver = resolver;
    this.optional = req.optional;
    this.pattern = req.pattern;
    this.config = resolver.config;

    resolver.usedRegistries.add(req.registry);
  }

  static getExoticResolver(pattern) {
    // TODO make this type more refined
    for (const _ref of (0, (_misc || _load_misc()).entries)((_index || _load_index()).exotics)) {
      var _ref2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref, 2);

      const Resolver = _ref2[1];

      if (Resolver.isVersion(pattern)) {
        return Resolver;
      }
    }
    return null;
  }

  getParentNames() {
    const chain = [];

    let request = this.parentRequest;
    while (request) {
      const info = this.resolver.getStrictResolvedPattern(request.pattern);
      chain.unshift(info.name);

      request = request.parentRequest;
    }

    return chain;
  }

  getLocked(remoteType) {
    // always prioritise root lockfile
    const shrunk = this.lockfile.getLocked(this.pattern);

    if (shrunk) {
      const resolvedParts = (_version || _load_version()).explodeHashedUrl(shrunk.resolved);

      return {
        name: shrunk.name,
        version: shrunk.version,
        _uid: shrunk.uid,
        _remote: {
          resolved: shrunk.resolved,
          type: remoteType,
          reference: resolvedParts.url,
          hash: resolvedParts.hash,
          registry: shrunk.registry
        },
        optionalDependencies: shrunk.optionalDependencies,
        dependencies: shrunk.dependencies
      };
    } else {
      return null;
    }
  }

  /**
   * If the input pattern matches a registry one then attempt to find it on the registry.
   * Otherwise fork off to an exotic resolver if one matches.
   */

  findVersionOnRegistry(pattern) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      var _PackageRequest$norma = PackageRequest.normalizePattern(pattern);

      const range = _PackageRequest$norma.range;
      const name = _PackageRequest$norma.name;


      const exoticResolver = PackageRequest.getExoticResolver(range);
      if (exoticResolver) {
        let data = yield _this.findExoticVersionInfo(exoticResolver, range);

        // clone data as we're manipulating it in place and this could be resolved multiple
        // times
        data = Object.assign({}, data);

        // this is so the returned package response uses the overridden name. ie. if the
        // package's actual name is `bar`, but it's been specified in the manifest like:
        //   "foo": "http://foo.com/bar.tar.gz"
        // then we use the foo name
        data.name = name;

        return data;
      }

      const Resolver = _this.getRegistryResolver();
      const resolver = new Resolver(_this, name, range);
      return resolver.resolve();
    })();
  }

  /**
   * Get the registry resolver associated with this package request.
   */

  getRegistryResolver() {
    const Resolver = (_index2 || _load_index2()).registries[this.registry];
    if (Resolver) {
      return Resolver;
    } else {
      throw new (_errors || _load_errors()).MessageError(`Unknown registry resolver ${ this.registry }`);
    }
  }

  /**
   * Explode and normalize a pattern into it's name and range.
   */

  static normalizePattern(pattern) {
    let hasVersion = false;
    let range = 'latest';
    let name = pattern;

    // if we're a scope then remove the @ and add it back later
    let isScoped = false;
    if (name[0] === '@') {
      isScoped = true;
      name = name.slice(1);
    }

    // take first part as the name
    const parts = name.split('@');
    if (parts.length > 1) {
      name = parts.shift();
      range = parts.join('@');

      if (range) {
        hasVersion = true;
      } else {
        range = '*';
      }
    }

    // add back @ scope suffix
    if (isScoped) {
      name = `@${ name }`;
    }

    return { name, range, hasVersion };
  }

  /**
   * Construct an exotic resolver instance with the input `ExoticResolver` and `range`.
   */

  findExoticVersionInfo(ExoticResolver, range) {
    const resolver = new ExoticResolver(this, range);
    return resolver.resolve();
  }

  /**
   * If the current pattern matches an exotic resolver then delegate to it or else try
   * the registry.
   */

  findVersionInfo() {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const exoticResolver = PackageRequest.getExoticResolver(_this2.pattern);
      if (exoticResolver) {
        return yield _this2.findExoticVersionInfo(exoticResolver, _this2.pattern);
      } else {
        return yield _this2.findVersionOnRegistry(_this2.pattern);
      }
    })();
  }

  /**
   * TODO description
   */

  find() {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // find version info for this package pattern
      const info = yield _this3.findVersionInfo();
      if (!info) {
        throw new (_errors || _load_errors()).MessageError(_this3.reporter.lang('unknownPackage', _this3.pattern));
      }

      (0, (_validate || _load_validate()).cleanDependencies)(info, false, _this3.reporter, function () {
        // swallow warnings
      });

      // check if while we were resolving this dep we've already resolved one that satisfies
      // the same range
      const resolved = _this3.resolver.getExactVersionMatch(info.name, info.version);
      if (resolved) {
        const ref = resolved._reference;
        invariant(ref, 'Resolved package info has no package reference');
        ref.addRequest(_this3);
        ref.addPattern(_this3.pattern, resolved);
        ref.addOptional(_this3.optional);
        ref.addVisibility(_this3.visibility);
        return;
      }

      if (info.flat && !_this3.resolver.flat) {
        throw new (_errors || _load_errors()).MessageError(_this3.reporter.lang('flatGlobalError'));
      }

      // validate version info
      PackageRequest.validateVersionInfo(info, _this3.reporter);

      //
      const remote = info._remote;
      invariant(remote, 'Missing remote');

      // set package reference
      const ref = new (_packageReference2 || _load_packageReference2()).default(_this3, info, remote);
      ref.addPattern(_this3.pattern, info);
      ref.addOptional(_this3.optional);
      ref.addVisibility(_this3.visibility);
      info._reference = ref;
      info._remote = remote;

      // start installation of dependencies
      const promises = [];
      const deps = [];

      // normal deps
      for (const depName in info.dependencies) {
        const depPattern = depName + '@' + info.dependencies[depName];
        deps.push(depPattern);
        promises.push(_this3.resolver.find({
          pattern: depPattern,
          registry: remote.registry,
          visibility: (_packageReference || _load_packageReference()).USED,
          optional: false,
          parentRequest: _this3
        }));
      }

      // optional deps
      for (const depName in info.optionalDependencies) {
        const depPattern = depName + '@' + info.optionalDependencies[depName];
        deps.push(depPattern);
        promises.push(_this3.resolver.find({
          pattern: depPattern,
          registry: remote.registry,
          visibility: (_packageReference || _load_packageReference()).USED,
          optional: true,
          parentRequest: _this3
        }));
      }

      yield Promise.all(promises);
      ref.addDependencies(deps);
    })();
  }

  /**
   * TODO description
   */

  static validateVersionInfo(info, reporter) {
    // human readable name to use in errors
    const human = `${ info.name }@${ info.version }`;

    info.version = PackageRequest.getPackageVersion(info);

    for (const key of (_constants || _load_constants()).REQUIRED_PACKAGE_KEYS) {
      if (!info[key]) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('missingRequiredPackageKey', human, key));
      }
    }
  }

  /**
   * Returns the package version if present, else defaults to the uid
   */

  static getPackageVersion(info) {
    // TODO possibly reconsider this behaviour
    return info.version === undefined ? info._uid : info.version;
  }
}
exports.default = PackageRequest;