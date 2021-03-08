'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.requireLockfile = exports.noArguments = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const currentVersion = flags.version();
    const latestVersion = yield config.requestManager.request({
      url: (_constants || _load_constants()).SELF_UPDATE_VERSION_URL,
      headers: {
        'Accept': 'text/plain'
      }
    });

    // Check if we already use the latest or a newer version
    if ((_semver || _load_semver()).default.compare(currentVersion, latestVersion) >= 0) {
      reporter.success(reporter.lang('selfUpdateNoNewer'));
      return;
    }

    reporter.info(reporter.lang('selfUpdateDownloading', latestVersion));

    const thisVersionRoot = (_path || _load_path()).default.resolve(__dirname, '..', '..', '..');
    let updatesFolder = (_path || _load_path()).default.resolve(thisVersionRoot, '..');
    const isCurrentVersionAnUpdate = (_path || _load_path()).default.basename(updatesFolder) === (_constants || _load_constants()).SELF_UPDATE_DOWNLOAD_FOLDER;

    if (!isCurrentVersionAnUpdate) {
      updatesFolder = (_path || _load_path()).default.resolve(thisVersionRoot, (_constants || _load_constants()).SELF_UPDATE_DOWNLOAD_FOLDER);
    }

    const locToUnzip = (_path || _load_path()).default.resolve(updatesFolder, latestVersion);

    yield (0, (_fs || _load_fs()).unlink)(locToUnzip);

    const fetcher = new (_tarballFetcher || _load_tarballFetcher()).default(locToUnzip, {
      type: 'tarball',
      registry: 'yarn',
      reference: (_constants || _load_constants()).SELF_UPDATE_TARBALL_URL,
      hash: null
    }, config, false);
    yield fetcher.fetch();

    // this links the downloaded release to bin/yarn.js
    yield (0, (_fs || _load_fs()).symlink)(locToUnzip, (_path || _load_path()).default.resolve(updatesFolder, 'current'));

    // clean garbage
    const pathToClean = (_path || _load_path()).default.resolve(updatesFolder, 'to_clean');
    if (yield (0, (_fs || _load_fs()).exists)(pathToClean)) {
      const previousVersionToCleanup = yield (0, (_fs || _load_fs()).realpath)(pathToClean);
      yield (0, (_fs || _load_fs()).unlink)(previousVersionToCleanup);
      yield (0, (_fs || _load_fs()).unlink)(pathToClean);
    }

    if (isCurrentVersionAnUpdate) {
      // current yarn installation is an update, let's clean it next time an update is run
      // because it may still be in use now
      yield (0, (_fs || _load_fs()).symlink)(thisVersionRoot, pathToClean);
    }

    // reset the roadrunner cache
    (_roadrunner || _load_roadrunner()).default.reset((_constants || _load_constants()).CACHE_FILENAME);

    reporter.success(reporter.lang('selfUpdateReleased', latestVersion));
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

var _roadrunner;

function _load_roadrunner() {
  return _roadrunner = _interopRequireDefault(require('roadrunner'));
}

var _semver;

function _load_semver() {
  return _semver = _interopRequireDefault(require('semver'));
}

var _path;

function _load_path() {
  return _path = _interopRequireDefault(require('path'));
}

var _constants;

function _load_constants() {
  return _constants = require('../../constants.js');
}

var _tarballFetcher;

function _load_tarballFetcher() {
  return _tarballFetcher = _interopRequireDefault(require('../../fetchers/tarball-fetcher.js'));
}

var _fs;

function _load_fs() {
  return _fs = require('../../util/fs.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const noArguments = exports.noArguments = true;
const requireLockfile = exports.requireLockfile = false;