'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let updateCwd = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config) {
    yield config.init({ cwd: config.globalFolder });
  });

  return function updateCwd(_x) {
    return _ref.apply(this, arguments);
  };
})();

let getBins = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config) {
    // build up list of registry folders to search for binaries
    const dirs = [];
    for (const registryName of Object.keys((_index || _load_index()).registries)) {
      const registry = config.registries[registryName];
      dirs.push(registry.loc);
    }

    // build up list of binary files
    const paths = new Set();
    for (const dir of dirs) {
      const binDir = path.join(dir, '.bin');
      if (!(yield (_fs || _load_fs()).exists(binDir))) {
        continue;
      }

      for (const name of yield (_fs || _load_fs()).readdir(binDir)) {
        paths.add(path.join(binDir, name));
      }
    }
    return paths;
  });

  return function getBins(_x2) {
    return _ref2.apply(this, arguments);
  };
})();

let initUpdateBins = (() => {
  var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter) {
    const beforeBins = yield getBins(config);
    const binFolder = getBinFolder();

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const afterBins = yield getBins(config);

      // remove old bins
      for (const src of beforeBins) {
        if (afterBins.has(src)) {
          // not old
          continue;
        }

        // remove old bin
        const dest = path.join(binFolder, path.basename(src));
        yield (_fs || _load_fs()).unlink(dest);
      }

      // add new bins
      for (const src of afterBins) {
        if (beforeBins.has(src)) {
          // already inserted
          continue;
        }

        // insert new bin
        const dest = path.join(binFolder, path.basename(src));
        yield (_fs || _load_fs()).unlink(dest);
        yield (0, (_packageLinker || _load_packageLinker()).linkBin)(src, dest);
      }
    });
  });

  return function initUpdateBins(_x3, _x4) {
    return _ref3.apply(this, arguments);
  };
})();

exports.hasWrapper = hasWrapper;

var _index;

function _load_index() {
  return _index = require('../../registries/index.js');
}

var _baseReporter;

function _load_baseReporter() {
  return _baseReporter = _interopRequireDefault(require('../../reporters/base-reporter.js'));
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _add;

function _load_add() {
  return _add = require('./add.js');
}

var _remove;

function _load_remove() {
  return _remove = require('./remove.js');
}

var _packageLinker;

function _load_packageLinker() {
  return _packageLinker = require('../../package-linker.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class GlobalAdd extends (_add || _load_add()).Add {
  maybeOutputSaveTree() {
    for (const pattern of this.args) {
      const manifest = this.resolver.getStrictResolvedPattern(pattern);
      ls(manifest, this.reporter, true);
    }
    return Promise.resolve();
  }

  _logSuccessSaveLockfile() {
    // noop
  }
}

const path = require('path');

function getGlobalPrefix() {
  if (process.env.PREFIX) {
    return process.env.PREFIX;
  } else if (process.platform === 'win32') {
    // c:\node\node.exe --> prefix=c:\node\
    return path.dirname(process.execPath);
  } else {
    // /usr/local/bin/node --> prefix=/usr/local
    let prefix = path.dirname(path.dirname(process.execPath));

    // destdir only is respected on Unix
    if (process.env.DESTDIR) {
      prefix = path.join(process.env.DESTDIR, prefix);
    }

    return prefix;
  }
}

function getBinFolder() {
  const prefix = getGlobalPrefix();
  if (process.platform === 'win32') {
    return prefix;
  } else {
    return path.resolve(prefix, 'bin');
  }
}

function ls(manifest, reporter, saved) {
  const bins = manifest.bin ? Object.keys(manifest.bin) : [];
  const human = `${ manifest.name }@${ manifest.version }`;
  if (bins.length) {
    if (saved) {
      reporter.success(`Installed ${ human } with binaries:`);
    } else {
      reporter.info(`${ human } has binaries:`);
    }
    reporter.list(`bins-${ manifest.name }`, bins);
  } else if (saved) {
    reporter.warn(`${ human } has no binaries`);
  }
}

function hasWrapper(flags, args) {
  return args[0] !== 'bin';
}

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('global', {
  add(config, reporter, flags, args) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      yield updateCwd(config);

      const updateBins = yield initUpdateBins(config, reporter);

      // install module
      const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd);
      const install = new GlobalAdd(args, flags, config, reporter, lockfile);
      yield install.init();

      // link binaries
      yield updateBins();
    })();
  },

  bin(config, reporter, flags, args) {
    console.log(getBinFolder());
  },

  ls(config, reporter, flags, args) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      yield updateCwd(config);

      // install so we get hard file paths
      const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd);
      const install = new (_install || _load_install()).Install({ skipIntegrity: true }, config, new (_baseReporter || _load_baseReporter()).default(), lockfile);
      const patterns = yield install.init();

      // dump global modules
      for (const pattern of patterns) {
        const manifest = install.resolver.getStrictResolvedPattern(pattern);
        ls(manifest, reporter, false);
      }
    })();
  },

  remove(config, reporter, flags, args) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      yield updateCwd(config);

      const updateBins = yield initUpdateBins(config, reporter);

      // remove module
      yield (0, (_remove || _load_remove()).run)(config, reporter, flags, args);

      // remove binaries
      yield updateBins();
    })();
  }
});

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;