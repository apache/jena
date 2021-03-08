'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.noArguments = exports.requireLockfile = undefined;

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd);
    const install = new (_install || _load_install()).Install(flags, config, reporter, lockfile);

    function humaniseLocation(loc) {
      const relative = path.relative(path.join(config.cwd, 'node_modules'), loc);
      const normalized = path.normalize(relative).split(path.sep);
      return normalized.filter(p => p !== 'node_modules');
    }

    let warningCount = 0;
    let errCount = 0;
    function reportError(msg) {
      reporter.error(msg);
      errCount++;
    }

    // get patterns that are installed when running `yarn install`

    var _ref2 = yield install.hydrate();

    var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

    const rawPatterns = _ref3[1];

    // check if patterns exist in lockfile

    for (const pattern of rawPatterns) {
      if (!lockfile.getLocked(pattern)) {
        reportError(`Lockfile does not contain pattern: ${ pattern }`);
      }
    }

    if (flags.integrity) {
      // just check the integrity hash for validity
      const integrityLoc = yield install.getIntegrityHashLocation();

      if (integrityLoc && (yield (_fs || _load_fs()).exists(integrityLoc))) {
        const match = yield install.matchesIntegrityHash(rawPatterns);
        if (match.matches === false) {
          reportError(`Integrity hashes don't match, expected ${ match.expected } but got ${ match.actual }`);
        }
      } else {
        reportError("Couldn't find an integrity hash file");
      }
    } else {
      // check if any of the node_modules are out of sync
      const res = yield install.linker.getFlatHoistedTree(rawPatterns);
      for (const _ref4 of res) {
        var _ref5 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref4, 2);

        const loc = _ref5[0];
        var _ref5$ = _ref5[1];
        const originalKey = _ref5$.originalKey;
        const pkg = _ref5$.pkg;

        const parts = humaniseLocation(loc);

        // grey out hoisted portions of key
        let human = originalKey;
        const hoistedParts = parts.slice();
        const hoistedKey = parts.join('#');
        if (human !== hoistedKey) {
          const humanParts = human.split('#');

          for (let i = 0; i < humanParts.length; i++) {
            const humanPart = humanParts[i];

            if (hoistedParts[0] === humanPart) {
              hoistedParts.shift();

              if (i < humanParts.length - 1) {
                humanParts[i] += '#';
              }
            } else {
              humanParts[i] = reporter.format.dim(`${ humanPart }#`);
            }
          }

          human = humanParts.join('');
        }

        const pkgLoc = path.join(loc, 'package.json');
        if (!(yield (_fs || _load_fs()).exists(loc)) || !(yield (_fs || _load_fs()).exists(pkgLoc))) {
          reportError(`${ human } not installed`);
          continue;
        }

        const packageJson = yield (_fs || _load_fs()).readJson(pkgLoc);
        if (pkg.version !== packageJson.version) {
          // node_modules contains wrong version
          reportError(`${ human } is wrong version: expected ${ pkg.version }, got ${ packageJson.version }`);
        }

        const deps = (0, (_assign || _load_assign()).default)({}, packageJson.dependencies, packageJson.peerDependencies);

        for (const name in deps) {
          const range = deps[name];
          if (!semver.validRange(range, config.looseSemver)) {
            continue; // exotic
          }

          const subHuman = `${ human }#${ name }@${ range }`;

          // find the package that this will resolve to, factoring in hoisting
          const possibles = [];
          let depPkgLoc;
          for (let i = parts.length; i >= 0; i--) {
            const myParts = parts.slice(0, i).concat(name);

            // build package.json location for this position
            const myDepPkgLoc = path.join(config.cwd, 'node_modules', myParts.join(`${ path.sep }node_modules${ path.sep }`), 'package.json');

            possibles.push(myDepPkgLoc);
          }
          while (possibles.length) {
            const myDepPkgLoc = possibles.shift();
            if (yield (_fs || _load_fs()).exists(myDepPkgLoc)) {
              depPkgLoc = myDepPkgLoc;
              break;
            }
          }
          if (!depPkgLoc) {
            // we'll hit the module not install error above when this module is hit
            continue;
          }

          //
          const depPkg = yield (_fs || _load_fs()).readJson(depPkgLoc);
          const foundHuman = `${ humaniseLocation(path.dirname(depPkgLoc)).join('#') }@${ depPkg.version }`;
          if (!semver.satisfies(depPkg.version, range, config.looseSemver)) {
            // module isn't correct semver
            reportError(`${ subHuman } doesn't satisfy found match of ${ foundHuman }`);
            continue;
          }

          // check for modules above us that this could be deduped to
          for (const loc of possibles) {
            if (!(yield (_fs || _load_fs()).exists(loc))) {
              continue;
            }

            const packageJson = yield (_fs || _load_fs()).readJson(loc);
            if (packageJson.version === depPkg.version || semver.satisfies(packageJson.version, range, config.looseSemver) && semver.gt(packageJson.version, depPkg.version, config.looseSemver)) {
              reporter.warn(`${ subHuman } could be deduped from ${ packageJson.version } to ` + `${ humaniseLocation(path.dirname(loc)).join('#') }@${ packageJson.version }`);
              warningCount++;
            }
            break;
          }
        }
      }
    }

    if (warningCount > 1) {
      reporter.info(reporter.lang('foundWarnings', warningCount));
    }

    if (errCount > 0) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('foundErrors', errCount));
    } else {
      reporter.success(reporter.lang('folderInSync'));
    }
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const semver = require('semver');
const path = require('path');

const requireLockfile = exports.requireLockfile = true;
const noArguments = exports.noArguments = true;

function setFlags(commander) {
  commander.option('--integrity');
}