'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.pack = undefined;

var _set;

function _load_set() {
  return _set = _interopRequireDefault(require('babel-runtime/core-js/set'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

let pack = exports.pack = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, dir) {
    const pkg = yield config.readRootManifest();

    //
    let filters = DEFAULT_IGNORE.slice();

    // include bundledDependencies
    const bundledDependencies = pkg.bundledDependencies;

    if (bundledDependencies) {
      const folder = config.getFolder(pkg);
      filters = (0, (_filter || _load_filter()).ignoreLinesToRegex)(bundledDependencies.map(function (name) {
        return `!${ folder }/${ name }`;
      }), '.');
    }

    // `files` field
    const onlyFiles = pkg.files;

    if (onlyFiles) {
      let lines = ['*'];
      lines = lines.concat(onlyFiles.map(function (filename) {
        return `!${ filename }`;
      }));
      filters = (0, (_filter || _load_filter()).ignoreLinesToRegex)(lines, '.');
    }

    //
    const files = yield (_fs || _load_fs()).walk(config.cwd);

    // create ignores
    for (const file of files) {
      if (IGNORE_FILENAMES.indexOf(path.basename(file.relative)) >= 0) {
        const raw = yield (_fs || _load_fs()).readFile(file.absolute);
        const lines = raw.split('\n');

        const regexes = (0, (_filter || _load_filter()).ignoreLinesToRegex)(lines, path.dirname(file.relative));
        filters = filters.concat(regexes);
      }
    }

    // files to definently keep, takes precedence over ignore filter
    const keepFiles = new (_set || _load_set()).default();

    // files to definently ignore
    const ignoredFiles = new (_set || _load_set()).default();

    // list of files that didn't match any of our patterns, if a directory in the chain above was matched
    // then we should inherit it
    const possibleKeepFiles = new (_set || _load_set()).default();

    // apply filters
    (0, (_filter || _load_filter()).sortFilter)(files, filters, keepFiles, possibleKeepFiles, ignoredFiles);

    const packer = tar.pack();
    const compressor = packer.pipe(new zlib.Gzip());

    yield addEntry(packer, {
      name: 'package',
      type: 'directory'
    });

    for (const name of keepFiles) {
      const loc = path.join(config.cwd, name);
      const stat = yield (_fs || _load_fs()).lstat(loc);

      let type;
      let buffer;
      let linkname;
      if (stat.isDirectory()) {
        type = 'directory';
      } else if (stat.isFile()) {
        buffer = yield (_fs || _load_fs()).readFileRaw(loc);
        type = 'file';
      } else if (stat.isSymbolicLink()) {
        type = 'symlink';
        linkname = yield (_fs || _load_fs()).readlink(loc);
      } else {
        throw new Error();
      }

      const entry = {
        name: `package/${ name }`,
        size: stat.size,
        mode: stat.mode,
        mtime: stat.mtime,
        type: type,
        linkname: linkname
      };

      yield addEntry(packer, entry, buffer);
    }

    packer.finalize();

    return compressor;
  });

  return function pack(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const pkg = yield config.readRootManifest();
    if (!pkg.name) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('noName'));
    }
    if (!pkg.version) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('noVersion'));
    }

    const filename = flags.filename || path.join(config.cwd, `${ pkg.name }-v${ pkg.version }.tgz`);

    const stream = yield pack(config, config.cwd);

    yield new (_promise || _load_promise()).default(function (resolve, reject) {
      stream.pipe(fs2.createWriteStream(filename));
      stream.on('error', reject);
      stream.on('close', resolve);
    });

    reporter.success(reporter.lang('packWroteTarball', filename));
  });

  return function run(_x3, _x4, _x5, _x6) {
    return _ref2.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _filter;

function _load_filter() {
  return _filter = require('../../util/filter.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const zlib = require('zlib');
const path = require('path');
const tar = require('tar-stream');
const fs2 = require('fs');

const IGNORE_FILENAMES = ['.yarnignore', '.npmignore', '.gitignore'];

const DEFAULT_IGNORE = (0, (_filter || _load_filter()).ignoreLinesToRegex)([
// never allow version control folders
'.git', 'CVS', '.svn', '.hg',

// ignore cruft
'yarn.lock', '.lock-wscript', '.wafpickle-{0..9}', '*.swp', '._*', 'npm-debug.log', 'yarn-error.log', '.npmrc', '.yarnrc', '.npmignore', '.gitignore', '.DS_Store', 'node_modules',

// never ignore these files
'!package.json', '!readme*', '!+(license|licence)*', '!+(changes|changelog|history)*']);

function addEntry(packer, entry, buffer) {
  return new (_promise || _load_promise()).default((resolve, reject) => {
    packer.entry(entry, buffer, function (err) {
      if (err) {
        reject(err);
      } else {
        resolve();
      }
    });
  });
}

function setFlags(commander) {
  commander.option('-f, --filename [filename]', 'filename');
}