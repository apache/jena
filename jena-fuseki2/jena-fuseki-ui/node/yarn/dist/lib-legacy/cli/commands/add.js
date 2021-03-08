'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.Add = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

let run = exports.run = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    if (!args.length) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('missingAddDependencies'));
    }

    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd, reporter);
    const install = new Add(args, flags, config, reporter, lockfile);
    yield install.init();
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref2.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _packageReference;

function _load_packageReference() {
  return _packageReference = _interopRequireWildcard(require('../../package-reference.js'));
}

var _packageRequest;

function _load_packageRequest() {
  return _packageRequest = _interopRequireDefault(require('../../package-request.js'));
}

var _ls;

function _load_ls() {
  return _ls = require('./ls.js');
}

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

class Add extends (_install || _load_install()).Install {
  constructor(args, flags, config, reporter, lockfile) {
    super(flags, config, reporter, lockfile);
    this.args = args;
  }

  /**
   * TODO
   */

  prepare(patterns, requests) {
    const requestsWithArgs = requests.slice();

    for (const pattern of this.args) {
      requestsWithArgs.push({
        pattern: pattern,
        registry: 'npm',
        visibility: (_packageReference || _load_packageReference()).USED,
        optional: false
      });
    }

    return (_promise || _load_promise()).default.resolve({
      patterns: patterns.concat(this.args),
      requests: requestsWithArgs,
      skip: false
    });
  }

  /**
   * Description
   */

  init() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const patterns = yield (_install || _load_install()).Install.prototype.init.call(_this);
      yield _this.maybeOutputSaveTree(patterns);
      yield _this.savePackages();
      return patterns;
    })();
  }

  /**
   * Description
   */

  fetchRequestFromCwd() {
    return (_install || _load_install()).Install.prototype.fetchRequestFromCwd.call(this, this.args);
  }

  /**
   * Output a tree of any newly added dependencies.
   */

  maybeOutputSaveTree(patterns) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      var _ref = yield (0, (_ls || _load_ls()).buildTree)(_this2.resolver, _this2.linker, patterns, true, true);

      const trees = _ref.trees;
      const count = _ref.count;

      _this2.reporter.success(count === 1 ? _this2.reporter.lang('savedNewDependency') : _this2.reporter.lang('savedNewDependencies', count));
      _this2.reporter.tree('newDependencies', trees);
    })();
  }

  /**
   * Save added packages to manifest if any of the --save flags were used.
   */

  savePackages() {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      var _flags = _this3.flags;
      const dev = _flags.dev;
      const exact = _flags.exact;
      const tilde = _flags.tilde;
      const optional = _flags.optional;
      const peer = _flags.peer;

      // get all the different registry manifests in this folder

      const manifests = yield _this3.config.getRootManifests();

      // add new patterns to their appropriate registry manifest
      for (const pattern of _this3.resolver.dedupePatterns(_this3.args)) {
        const pkg = _this3.resolver.getResolvedPattern(pattern);
        invariant(pkg, `missing package ${ pattern }`);

        const ref = pkg._reference;
        invariant(ref, 'expected package reference');

        const parts = (_packageRequest || _load_packageRequest()).default.normalizePattern(pattern);
        let version;
        if (parts.hasVersion && parts.range) {
          // if the user specified a range then use it verbatim
          version = parts.range;
        } else if ((_packageRequest || _load_packageRequest()).default.getExoticResolver(pattern)) {
          // wasn't a name/range tuple so this is just a raw exotic pattern
          version = pattern;
        } else if (tilde) {
          // --save-tilde
          version = `~${ pkg.version }`;
        } else if (exact) {
          // --save-exact
          version = pkg.version;
        } else {
          // default to save prefix
          version = `${ String(_this3.config.getOption('save-prefix')) }${ pkg.version }`;
        }

        // build up list of objects to put ourselves into from the cli args
        const targetKeys = [];
        if (dev) {
          targetKeys.push('devDependencies');
        }
        if (peer) {
          targetKeys.push('peerDependencies');
        }
        if (optional) {
          targetKeys.push('optionalDependencies');
        }
        if (!targetKeys.length) {
          targetKeys.push('dependencies');
        }

        // add it to manifest
        const object = manifests[ref.registry].object;
        for (const key of targetKeys) {
          const target = object[key] = object[key] || {};
          target[pkg.name] = version;
        }

        // add pattern so it's aliased in the lockfile
        const newPattern = `${ pkg.name }@${ version }`;
        if (newPattern === pattern) {
          continue;
        }
        _this3.resolver.addPattern(newPattern, pkg);
        _this3.resolver.removePattern(pattern);
      }

      yield _this3.config.saveRootManifests(manifests);
    })();
  }
}

exports.Add = Add;
function setFlags(commander) {
  commander.usage('add [packages ...] [flags]');
  (0, (_install || _load_install())._setFlags)(commander);
  commander.option('--dev', 'save package to your `devDependencies`');
  commander.option('--peer', 'save package to your `peerDependencies`');
  commander.option('--optional', 'save package to your `optionalDependencies`');
  commander.option('--exact', '');
  commander.option('--tilde', '');
}