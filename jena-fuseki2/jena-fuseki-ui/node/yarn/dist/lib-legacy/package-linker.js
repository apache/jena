'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.linkBin = undefined;

var _from;

function _load_from() {
  return _from = _interopRequireDefault(require('babel-runtime/core-js/array/from'));
}

var _set;

function _load_set() {
  return _set = _interopRequireDefault(require('babel-runtime/core-js/set'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('babel-runtime/core-js/map'));
}

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let linkBin = exports.linkBin = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (src, dest) {
    if (process.platform === 'win32') {
      yield cmdShim(src, dest);
    } else {
      yield (_fs || _load_fs()).mkdirp(path.dirname(dest));
      yield (_fs || _load_fs()).symlink(src, dest);
      yield (_fs || _load_fs()).chmod(dest, '755');
    }
  });

  return function linkBin(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

var _packageHoister;

function _load_packageHoister() {
  return _packageHoister = _interopRequireDefault(require('./package-hoister.js'));
}

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('./constants.js'));
}

var _promise2;

function _load_promise2() {
  return _promise2 = _interopRequireWildcard(require('./util/promise.js'));
}

var _misc;

function _load_misc() {
  return _misc = require('./util/misc.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('./util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

const cmdShim = (_promise2 || _load_promise2()).promisify(require('cmd-shim'));
const semver = require('semver');
const path = require('path');

class PackageLinker {
  constructor(config, resolver, ignoreOptional) {
    this.ignoreOptional = ignoreOptional;
    this.resolver = resolver;
    this.reporter = config.reporter;
    this.config = config;
  }

  linkSelfDependencies(pkg, pkgLoc, targetBinLoc) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      targetBinLoc = yield (_fs || _load_fs()).realpath(targetBinLoc);
      pkgLoc = yield (_fs || _load_fs()).realpath(pkgLoc);
      for (const _ref2 of (0, (_misc || _load_misc()).entries)(pkg.bin)) {
        var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

        const scriptName = _ref3[0];
        const scriptCmd = _ref3[1];

        const dest = path.join(targetBinLoc, scriptName);
        const src = path.join(pkgLoc, scriptCmd);
        if (!(yield (_fs || _load_fs()).exists(src))) {
          // TODO maybe throw an error
          continue;
        }
        yield linkBin(src, dest);
      }
    })();
  }

  linkBinDependencies(pkg, dir) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const deps = [];

      const ref = pkg._reference;
      invariant(ref, 'Package reference is missing');

      const remote = pkg._remote;
      invariant(remote, 'Package remote is missing');

      // link up `bin scripts` in `dependencies`
      for (const pattern of ref.dependencies) {
        const dep = _this.resolver.getStrictResolvedPattern(pattern);
        if (dep.bin && (0, (_keys || _load_keys()).default)(dep.bin).length) {
          deps.push({ dep: dep, loc: _this.config.generateHardModulePath(dep._reference) });
        }
      }

      // link up the `bin` scripts in bundled dependencies
      if (pkg.bundleDependencies) {
        for (const depName of pkg.bundleDependencies) {
          const loc = path.join(_this.config.generateHardModulePath(ref), _this.config.getFolder(pkg), depName);

          const dep = yield _this.config.readManifest(loc, remote.registry);

          if (dep.bin && (0, (_keys || _load_keys()).default)(dep.bin).length) {
            deps.push({ dep: dep, loc: loc });
          }
        }
      }

      // no deps to link
      if (!deps.length) {
        return;
      }

      // ensure our .bin file we're writing these to exists
      const binLoc = path.join(dir, '.bin');
      yield (_fs || _load_fs()).mkdirp(binLoc);

      // write the executables
      for (const _ref4 of deps) {
        const dep = _ref4.dep;
        const loc = _ref4.loc;

        yield _this.linkSelfDependencies(dep, loc, binLoc);
      }
    })();
  }

  getFlatHoistedTree(patterns) {
    const hoister = new (_packageHoister || _load_packageHoister()).default(this.config, this.resolver, this.ignoreOptional);
    hoister.seed(patterns);
    return (_promise || _load_promise()).default.resolve(hoister.init());
  }

  copyModules(patterns) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      let flatTree = yield _this2.getFlatHoistedTree(patterns);

      // sorted tree makes file creation and copying not to interfere with each other
      flatTree = flatTree.sort(function (dep1, dep2) {
        return dep1[0].localeCompare(dep2[0]);
      });

      //
      const queue = new (_map || _load_map()).default();
      for (const _ref5 of flatTree) {
        var _ref6 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref5, 2);

        const dest = _ref6[0];
        var _ref6$ = _ref6[1];
        const pkg = _ref6$.pkg;
        const src = _ref6$.loc;

        const ref = pkg._reference;
        invariant(ref, 'expected package reference');
        ref.setLocation(dest);

        queue.set(dest, {
          src: src,
          dest: dest,
          onFresh: function onFresh() {
            if (ref) {
              ref.setFresh(true);
            }
          }
        });
      }

      // register root packages as being possibly extraneous
      const possibleExtraneous = new (_set || _load_set()).default();
      for (const folder of _this2.config.registryFolders) {
        const loc = path.join(_this2.config.cwd, folder);

        if (yield (_fs || _load_fs()).exists(loc)) {
          const files = yield (_fs || _load_fs()).readdir(loc);
          for (const file of files) {
            possibleExtraneous.add(path.join(loc, file));
          }
        }
      }

      // linked modules
      for (const loc of possibleExtraneous) {
        const stat = yield (_fs || _load_fs()).lstat(loc);
        if (stat.isSymbolicLink()) {
          possibleExtraneous.delete(loc);
          queue.delete(loc);
        }
      }

      //
      let tick;
      yield (_fs || _load_fs()).copyBulk((0, (_from || _load_from()).default)(queue.values()), {
        possibleExtraneous: possibleExtraneous,

        ignoreBasenames: [(_constants || _load_constants()).METADATA_FILENAME, (_constants || _load_constants()).TARBALL_FILENAME],

        onStart: function onStart(num) {
          tick = _this2.reporter.progress(num);
        },

        onProgress: function onProgress(src) {
          if (tick) {
            tick(src);
          }
        }
      });

      //
      const tickBin = _this2.reporter.progress(flatTree.length);
      yield (_promise2 || _load_promise2()).queue(flatTree, (() => {
        var _ref8 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (_ref7) {
          var _ref9 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref7, 2);

          let dest = _ref9[0];
          let pkg = _ref9[1].pkg;

          const binLoc = path.join(dest, _this2.config.getFolder(pkg));
          yield _this2.linkBinDependencies(pkg, binLoc);
          tickBin(dest);
        });

        return function (_x3) {
          return _ref8.apply(this, arguments);
        };
      })(), 4);
    })();
  }

  resolvePeerModules() {
    for (const pkg of this.resolver.getManifests()) {
      this._resolvePeerModules(pkg);
    }
  }

  _resolvePeerModules(pkg) {
    const peerDeps = pkg.peerDependencies;
    if (!peerDeps) {
      return;
    }

    const ref = pkg._reference;
    invariant(ref, 'Package reference is missing');

    for (const name in peerDeps) {
      const range = peerDeps[name];

      // find a dependency in the tree above us that matches
      let searchPatterns = [];
      for (let request of ref.requests) {
        do {
          // get resolved pattern for this request
          const dep = this.resolver.getResolvedPattern(request.pattern);
          if (!dep) {
            continue;
          }

          //
          const ref = dep._reference;
          invariant(ref, 'expected reference');
          searchPatterns = searchPatterns.concat(ref.dependencies);
        } while (request = request.parentRequest);
      }

      // include root seed patterns last
      searchPatterns = searchPatterns.concat(this.resolver.seedPatterns);

      // find matching dep in search patterns
      let foundDep;
      for (const pattern of searchPatterns) {
        const dep = this.resolver.getResolvedPattern(pattern);
        if (dep && dep.name === name) {
          foundDep = { pattern: pattern, version: dep.version };
          break;
        }
      }

      // validate found peer dependency
      if (foundDep) {
        if (range === '*' || semver.satisfies(foundDep.version, range, this.config.looseSemver)) {
          ref.addDependencies([foundDep.pattern]);
        } else {
          this.reporter.warn(this.reporter.lang('incorrectPeer', `${ name }@${ range }`));
        }
      } else {
        this.reporter.warn(this.reporter.lang('unmetPeer', `${ name }@${ range }`));
      }
    }
  }

  init(patterns) {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      _this3.resolvePeerModules();
      yield _this3.copyModules(patterns);
      yield _this3.saveAll(patterns);
    })();
  }

  save(pattern) {
    var _this4 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const resolved = _this4.resolver.getResolvedPattern(pattern);
      invariant(resolved, `Couldn't find resolved name/version for ${ pattern }`);

      const ref = resolved._reference;
      invariant(ref, 'Missing reference');

      //
      const src = _this4.config.generateHardModulePath(ref);

      // link bins
      if (resolved.bin && (0, (_keys || _load_keys()).default)(resolved.bin).length) {
        const folder = _this4.config.modulesFolder || path.join(_this4.config.cwd, _this4.config.getFolder(resolved));
        const binLoc = path.join(folder, '.bin');
        yield (_fs || _load_fs()).mkdirp(binLoc);
        yield _this4.linkSelfDependencies(resolved, src, binLoc);
      }
    })();
  }

  saveAll(deps) {
    var _this5 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      deps = _this5.resolver.dedupePatterns(deps);
      yield (_promise2 || _load_promise2()).queue(deps, function (dep) {
        return _this5.save(dep);
      });
    })();
  }
}
exports.default = PackageLinker;