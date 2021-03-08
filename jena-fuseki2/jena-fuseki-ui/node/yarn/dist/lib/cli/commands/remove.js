'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.requireLockfile = undefined;

var _extends2;

function _load_extends() {
  return _extends2 = _interopRequireDefault(require('babel-runtime/helpers/extends'));
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
    if (!args.length) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('tooFewArguments', 1));
    }

    const totalSteps = args.length + 1;
    let step = 0;

    // load manifests
    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd);
    const rootManifests = yield config.getRootManifests();
    const manifests = [];

    for (const name of args) {
      reporter.step(++step, totalSteps, `Removing module ${ name }`);

      let found = false;

      for (const registryName of Object.keys((_index || _load_index()).registries)) {
        const registry = config.registries[registryName];
        const object = rootManifests[registryName].object;

        for (const type of (_constants || _load_constants()).DEPENDENCY_TYPES) {
          const deps = object[type];
          if (deps) {
            found = true;
            delete deps[name];
          }
        }

        const possibleManifestLoc = path.join(config.cwd, registry.folder, name);
        if (yield (_fs || _load_fs()).exists(possibleManifestLoc)) {
          manifests.push([possibleManifestLoc, yield config.readManifest(possibleManifestLoc, registryName)]);
        }
      }

      if (!found) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('moduleNotInManifest'));
      }
    }

    // save manifests
    yield config.saveRootManifests(rootManifests);

    // run hooks - npm runs these one after another
    for (const action of ['preuninstall', 'uninstall', 'postuninstall']) {
      for (const _ref2 of manifests) {
        var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

        const loc = _ref3[0];
        const manifest = _ref3[1];

        yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).execFromManifest)(config, action, manifest, loc);
      }
    }

    // reinstall so we can get the updated lockfile
    reporter.step(++step, totalSteps, reporter.lang('uninstallRegenerate'));
    const reinstall = new (_install || _load_install()).Install((0, (_extends2 || _load_extends()).default)({ force: true }, flags), config, new (_index2 || _load_index2()).NoopReporter(), lockfile);
    yield reinstall.init();

    //
    reporter.success(reporter.lang('uninstalledPackages'));
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

var _executeLifecycleScript;

function _load_executeLifecycleScript() {
  return _executeLifecycleScript = require('./_execute-lifecycle-script.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _index;

function _load_index() {
  return _index = require('../../registries/index.js');
}

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _index2;

function _load_index2() {
  return _index2 = require('../../reporters/index.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('../../constants.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

const requireLockfile = exports.requireLockfile = true;