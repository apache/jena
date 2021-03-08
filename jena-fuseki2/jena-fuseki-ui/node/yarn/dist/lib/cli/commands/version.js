'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.setVersion = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let setVersion = exports.setVersion = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args, required) {
    const pkg = yield config.readRootManifest();
    const pkgLoc = pkg._loc;
    let newVersion = flags.newVersion;
    invariant(pkgLoc, 'expected package location');

    if (args.length && !newVersion) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('invalidVersionArgument', NEW_VERSION_FLAG));
    }

    // get old version
    let oldVersion = pkg.version;
    if (oldVersion) {
      reporter.info(`${ reporter.lang('currentVersion') }: ${ oldVersion }`);
    } else {
      oldVersion = '0.0.0';
    }

    // get new version
    if (newVersion && !isValidNewVersion(oldVersion, newVersion, config.looseSemver)) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('invalidVersion'));
    }

    // wasn't passed a version arg so ask interactively
    while (!newVersion) {
      newVersion = yield reporter.question(reporter.lang('newVersion'));

      if (!required && !newVersion) {
        return function () {
          return Promise.resolve();
        };
      }

      if (isValidNewVersion(oldVersion, newVersion, config.looseSemver)) {
        break;
      } else {
        newVersion = null;
        reporter.error(reporter.lang('invalidSemver'));
      }
    }
    if (newVersion) {
      newVersion = semver.inc(oldVersion, newVersion, config.looseSemver) || newVersion;
    }
    invariant(newVersion, 'expected new version');

    if (newVersion === pkg.version) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('publishSame'));
    }

    yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'preversion');

    // update version
    reporter.info(`${ reporter.lang('newVersion') }: ${ newVersion }`);
    pkg.version = newVersion;

    // update versions in manifests
    const manifests = yield config.getRootManifests();
    for (const registryName of (_index || _load_index()).registryNames) {
      const manifest = manifests[registryName];
      if (manifest.exists) {
        manifest.object.version = newVersion;
      }
    }
    yield config.saveRootManifests(manifests);

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      invariant(newVersion, 'expected version');

      // add git commit and tag
      let isGit = false;
      const parts = config.cwd.split(path.sep);
      while (parts.length) {
        isGit = yield (_fs || _load_fs()).exists(path.join(parts.join(path.sep), '.git'));
        if (isGit) {
          break;
        } else {
          parts.pop();
        }
      }
      if (isGit && Boolean(config.getOption('version-git-tag'))) {
        const message = (flags.message || String(config.getOption('version-git-message'))).replace(/%s/g, newVersion);
        const sign = Boolean(config.getOption('version-sign-git-tag'));
        const flag = sign ? '-sm' : '-am';
        const prefix = String(config.getOption('version-tag-prefix'));

        // add manifest
        yield (0, (_child || _load_child()).spawn)('git', ['add', pkgLoc]);

        // create git commit
        yield (0, (_child || _load_child()).spawn)('git', ['commit', '-m', message]);

        // create git tag
        yield (0, (_child || _load_child()).spawn)('git', ['tag', `${ prefix }${ newVersion }`, flag, message]);
      }

      yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'postversion');
    });
  });

  return function setVersion(_x, _x2, _x3, _x4, _x5) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const commit = yield setVersion(config, reporter, flags, args, true);
    yield commit();
  });

  return function run(_x6, _x7, _x8, _x9) {
    return _ref3.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _index;

function _load_index() {
  return _index = require('../../registries/index.js');
}

var _executeLifecycleScript;

function _load_executeLifecycleScript() {
  return _executeLifecycleScript = _interopRequireDefault(require('./_execute-lifecycle-script.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _child;

function _load_child() {
  return _child = require('../../util/child.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

const semver = require('semver');
const path = require('path');

const NEW_VERSION_FLAG = '--new-version [version]';
function isValidNewVersion(oldVersion, newVersion, looseSemver) {
  return !!(semver.valid(newVersion, looseSemver) || semver.inc(oldVersion, newVersion, looseSemver));
}

function setFlags(commander) {
  commander.option(NEW_VERSION_FLAG, 'new version');
  commander.option('--message [message]', 'message');
}