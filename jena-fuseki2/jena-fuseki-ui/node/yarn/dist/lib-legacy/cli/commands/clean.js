'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.clean = exports.noArguments = exports.requireLockfile = undefined;

var _set;

function _load_set() {
  return _set = _interopRequireDefault(require('babel-runtime/core-js/set'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let clean = exports.clean = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter) {
    const loc = path.join(config.cwd, (_constants || _load_constants()).CLEAN_FILENAME);
    const file = yield (_fs || _load_fs()).readFile(loc);
    const lines = file.split('\n');
    const filters = DEFAULT_FILTERS.concat((0, (_filter || _load_filter()).ignoreLinesToRegex)(lines));

    let removedFiles = 0;
    let removedSize = 0;

    // build list of possible module folders
    const locs = new (_set || _load_set()).default();
    if (config.modulesFolder) {
      locs.add(config.modulesFolder);
    }
    for (const name of (_index || _load_index()).registryNames) {
      const registry = config.registries[name];
      locs.add(path.join(config.cwd, registry.folder));
    }

    for (const folder of locs) {
      if (!(yield (_fs || _load_fs()).exists(folder))) {
        continue;
      }

      const spinner = reporter.activity();
      const files = yield (_fs || _load_fs()).walk(folder);

      var _sortFilter = (0, (_filter || _load_filter()).sortFilter)(files, filters);

      const ignoreFiles = _sortFilter.ignoreFiles;

      spinner.end();

      const tick = reporter.progress(ignoreFiles.size);
      // TODO make sure `main` field of all modules isn't ignored

      for (const file of ignoreFiles) {
        const loc = path.join(folder, file);
        const stat = yield (_fs || _load_fs()).lstat(loc);
        removedSize += stat.size;
        removedFiles++;
      }

      for (const file of ignoreFiles) {
        const loc = path.join(folder, file);
        yield (_fs || _load_fs()).unlink(loc);
        tick();
      }
    }

    return { removedFiles: removedFiles, removedSize: removedSize };
  });

  return function clean(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    reporter.step(1, 2, reporter.lang('cleanCreatingFile', (_constants || _load_constants()).CLEAN_FILENAME));
    yield (_fs || _load_fs()).writeFile(path.join(config.cwd, (_constants || _load_constants()).CLEAN_FILENAME), '\n', { flag: 'wx' });

    reporter.step(2, 2, reporter.lang('cleaning'));

    var _ref3 = yield clean(config, reporter);

    const removedFiles = _ref3.removedFiles;
    const removedSize = _ref3.removedSize;

    reporter.info(reporter.lang('cleanRemovedFiles', removedFiles));
    reporter.info(reporter.lang('cleanSavedSize', Number((removedSize / 1024 / 1024).toFixed(2))));
  });

  return function run(_x3, _x4, _x5, _x6) {
    return _ref2.apply(this, arguments);
  };
})();

var _index;

function _load_index() {
  return _index = require('../../registries/index.js');
}

var _filter;

function _load_filter() {
  return _filter = require('../../util/filter.js');
}

var _constants;

function _load_constants() {
  return _constants = require('../../constants.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

const requireLockfile = exports.requireLockfile = true;
const noArguments = exports.noArguments = true;

const DEFAULT_FILTERS = (0, (_filter || _load_filter()).ignoreLinesToRegex)([
// test directories
'__tests__', 'test', 'tests', 'powered-test',

// asset directories
'docs', 'doc', 'website', 'images', 'assets',

// examples
'example', 'examples',

// code coverage/test data
'coverage', '.nyc_output',

// build scripts
'Makefile', 'Gulpfile.js', 'Gruntfile.js',

// configs
'.tern-project', '.gitattributes', '.editorconfig', '.*ignore', '.eslintrc', '.jshintrc', '.flowconfig', '.documentup.json', '.yarn-metadata.json', '.*.yml', '*.yml',

//
'*.gz', '*.md',

//
'CHANGES', 'HISTORY']);