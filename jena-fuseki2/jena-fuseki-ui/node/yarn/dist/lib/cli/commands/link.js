'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.getRegistryFolder = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let getRegistryFolder = exports.getRegistryFolder = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, name) {
    if (config.modulesFolder) {
      return config.modulesFolder;
    }

    const src = path.join(config.linkFolder, name);

    var _ref2 = yield config.readManifest(src);

    const _registry = _ref2._registry;

    invariant(_registry, 'expected registry');

    const registryFolder = config.registries[_registry].folder;
    return path.join(config.cwd, registryFolder);
  });

  return function getRegistryFolder(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    if (args.length) {
      for (const name of args) {
        const src = path.join(config.linkFolder, name);

        if (yield (_fs || _load_fs()).exists(src)) {
          const folder = yield getRegistryFolder(config, name);
          const dest = path.join(folder, name);

          yield (_fs || _load_fs()).unlink(dest);
          yield (_fs || _load_fs()).mkdirp(path.dirname(dest));
          yield (_fs || _load_fs()).symlink(src, dest);
          reporter.success(reporter.lang('linkRegistered', name));
        } else {
          throw new (_errors || _load_errors()).MessageError(reporter.lang('linkMissing', name));
        }
      }
    } else {
      // add cwd module to the global registry
      const manifest = yield config.readRootManifest();
      const name = manifest.name;
      if (!name) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('unknownPackageName'));
      }

      const linkLoc = path.join(config.linkFolder, name);
      if (yield (_fs || _load_fs()).exists(linkLoc)) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('linkCollision', name));
      } else {
        yield (_fs || _load_fs()).mkdirp(path.dirname(linkLoc));
        yield (_fs || _load_fs()).symlink(config.cwd, linkLoc);
        reporter.success(reporter.lang('linkRegistered', name));
        reporter.info(reporter.lang('linkInstallMessage', name));
      }
    }
  });

  return function run(_x3, _x4, _x5, _x6) {
    return _ref3.apply(this, arguments);
  };
})();

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const path = require('path');