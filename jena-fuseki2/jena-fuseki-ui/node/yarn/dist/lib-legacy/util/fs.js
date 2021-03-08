'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getFileSizeOnDisk = exports.walk = exports.symlink = exports.find = exports.readJsonAndFile = exports.readJson = exports.readFileAny = exports.copyBulk = exports.chmod = exports.lstat = exports.exists = exports.mkdirp = exports.unlink = exports.stat = exports.access = exports.rename = exports.readdir = exports.realpath = exports.readlink = exports.writeFile = exports.readFileBuffer = exports.lockQueue = undefined;

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _set;

function _load_set() {
  return _set = _interopRequireDefault(require('babel-runtime/core-js/set'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let buildActionsForCopy = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (queue, events, possibleExtraneousSeed) {

    //
    let build = (() => {
      var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (data) {
        const src = data.src;
        const dest = data.dest;

        const onFresh = data.onFresh || noop;
        const onDone = data.onDone || noop;
        files.add(dest);

        if (events.ignoreBasenames.indexOf(path.basename(src)) >= 0) {
          // ignored file
          return;
        }

        const srcStat = yield lstat(src);
        let srcFiles;

        if (srcStat.isDirectory()) {
          srcFiles = yield readdir(src);
        }

        if (yield exists(dest)) {
          const destStat = yield lstat(dest);

          const bothSymlinks = srcStat.isSymbolicLink() && destStat.isSymbolicLink();
          const bothFolders = srcStat.isDirectory() && destStat.isDirectory();
          const bothFiles = srcStat.isFile() && destStat.isFile();

          if (srcStat.mode !== destStat.mode) {
            try {
              yield access(dest, srcStat.mode);
            } catch (err) {
              // EINVAL access errors sometimes happen which shouldn't because node shouldn't be giving
              // us modes that aren't valid. investigate this, it's generally safe to proceed.
            }
          }

          if (bothFiles && srcStat.size === destStat.size && +srcStat.mtime === +destStat.mtime) {
            // we can safely assume this is the same file
            onDone();
            return;
          }

          if (bothSymlinks && (yield readlink(src)) === (yield readlink(dest))) {
            // if both symlinks are the same then we can continue on
            onDone();
            return;
          }

          if (bothFolders && !noExtraneous) {
            // mark files that aren't in this folder as possibly extraneous
            const destFiles = yield readdir(dest);
            invariant(srcFiles, 'src files not initialised');

            for (const file of destFiles) {
              if (srcFiles.indexOf(file) < 0) {
                const loc = path.join(dest, file);
                possibleExtraneous.add(loc);

                if ((yield lstat(loc)).isDirectory()) {
                  for (const file of yield readdir(loc)) {
                    possibleExtraneous.add(path.join(loc, file));
                  }
                }
              }
            }
          }
        }

        if (srcStat.isSymbolicLink()) {
          onFresh();
          const linkname = yield readlink(src);
          actions.push({
            type: 'symlink',
            dest: dest,
            linkname: linkname
          });
          onDone();
        } else if (srcStat.isDirectory()) {
          yield mkdirp(dest);

          const destParts = dest.split(path.sep);
          while (destParts.length) {
            files.add(destParts.join(path.sep));
            destParts.pop();
          }

          // push all files to queue
          invariant(srcFiles, 'src files not initialised');
          let remaining = srcFiles.length;
          if (!remaining) {
            onDone();
          }
          for (const file of srcFiles) {
            queue.push({
              onFresh: onFresh,
              src: path.join(src, file),
              dest: path.join(dest, file),
              onDone: function (_onDone) {
                function onDone() {
                  return _onDone.apply(this, arguments);
                }

                onDone.toString = function () {
                  return _onDone.toString();
                };

                return onDone;
              }(function () {
                if (--remaining === 0) {
                  onDone();
                }
              })
            });
          }
        } else if (srcStat.isFile()) {
          onFresh();
          actions.push({
            type: 'file',
            src: src,
            dest: dest,
            atime: srcStat.atime,
            mtime: srcStat.mtime,
            mode: srcStat.mode
          });
          onDone();
        } else {
          throw new Error(`unsure how to copy this: ${ src }`);
        }
      });

      return function build(_x4) {
        return _ref2.apply(this, arguments);
      };
    })();

    const possibleExtraneous = new (_set || _load_set()).default(possibleExtraneousSeed || []);
    const noExtraneous = possibleExtraneousSeed === false;
    const files = new (_set || _load_set()).default();

    // initialise events
    for (const item of queue) {
      const onDone = item.onDone;
      item.onDone = function () {
        events.onProgress(item.dest);
        if (onDone) {
          onDone();
        }
      };
    }
    events.onStart(queue.length);

    // start building actions
    const actions = [];

    // custom concurrency logic as we're always executing stacks of 4 queue items
    // at a time due to the requirement to push items onto the queue
    while (queue.length) {
      const items = queue.splice(0, 4);
      yield (_promise || _load_promise()).default.all(items.map(build));
    }

    // remove all extraneous files that weren't in the tree
    if (!noExtraneous) {
      for (const loc of possibleExtraneous) {
        if (!files.has(loc)) {
          yield unlink(loc);
        }
      }
    }

    return actions;
  });

  return function buildActionsForCopy(_x, _x2, _x3) {
    return _ref.apply(this, arguments);
  };
})();

let copyBulk = exports.copyBulk = (() => {
  var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (queue, _events) {
    const events = {
      onStart: _events && _events.onStart || noop,
      onProgress: _events && _events.onProgress || noop,
      possibleExtraneous: _events ? _events.possibleExtraneous : [],
      ignoreBasenames: _events && _events.ignoreBasenames || []
    };

    const actions = yield buildActionsForCopy(queue, events, events.possibleExtraneous);
    events.onStart(actions.length);

    const fileActions = actions.filter(function (action) {
      return action.type === 'file';
    });
    yield (_promise2 || _load_promise2()).queue(fileActions, function (data) {
      return new (_promise || _load_promise()).default(function (resolve, reject) {
        const readStream = fs.createReadStream(data.src);
        const writeStream = fs.createWriteStream(data.dest, { mode: data.mode });

        readStream.on('error', reject);
        writeStream.on('error', reject);

        writeStream.on('open', function () {
          readStream.pipe(writeStream);
        });

        writeStream.once('finish', function () {
          fs.utimes(data.dest, data.atime, data.mtime, function (err) {
            if (err) {
              reject(err);
            } else {
              events.onProgress(data.dest);
              resolve();
            }
          });
        });
      });
    }, 4);

    // we need to copy symlinks last as the could reference files we were copying
    const symlinkActions = actions.filter(function (action) {
      return action.type === 'symlink';
    });
    yield (_promise2 || _load_promise2()).queue(symlinkActions, function (data) {
      return symlink(path.resolve(path.dirname(data.dest), data.linkname), data.dest);
    });
  });

  return function copyBulk(_x5, _x6) {
    return _ref3.apply(this, arguments);
  };
})();

let readFileAny = exports.readFileAny = (() => {
  var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (files) {
    for (const file of files) {
      if (yield exists(file)) {
        return readFile(file);
      }
    }
    return null;
  });

  return function readFileAny(_x7) {
    return _ref4.apply(this, arguments);
  };
})();

let readJson = exports.readJson = (() => {
  var _ref5 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (loc) {
    return (yield readJsonAndFile(loc)).object;
  });

  return function readJson(_x8) {
    return _ref5.apply(this, arguments);
  };
})();

let readJsonAndFile = exports.readJsonAndFile = (() => {
  var _ref6 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (loc) {
    const file = yield readFile(loc);
    try {
      return {
        object: (0, (_map || _load_map()).default)(JSON.parse(stripBOM(file))),
        content: file
      };
    } catch (err) {
      err.message = `${ loc }: ${ err.message }`;
      throw err;
    }
  });

  return function readJsonAndFile(_x9) {
    return _ref6.apply(this, arguments);
  };
})();

let find = exports.find = (() => {
  var _ref7 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (filename, dir) {
    const parts = dir.split(path.sep);

    while (parts.length) {
      const loc = parts.concat(filename).join(path.sep);

      if (yield exists(loc)) {
        return loc;
      } else {
        parts.pop();
      }
    }

    return false;
  });

  return function find(_x10, _x11) {
    return _ref7.apply(this, arguments);
  };
})();

let symlink = exports.symlink = (() => {
  var _ref8 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (src, dest) {
    try {
      const stats = yield lstat(dest);

      if (stats.isSymbolicLink() && (yield exists(dest))) {
        const resolved = yield realpath(dest);
        if (resolved === src) {
          return;
        }
      }

      yield unlink(dest);
    } catch (err) {
      if (err.code !== 'ENOENT') {
        throw err;
      }
    }

    try {
      if (process.platform === 'win32') {
        // use directory junctions if possible on win32, this requires absolute paths
        yield fsSymlink(src, dest, 'junction');
      } else {
        // use relative paths otherwise which will be retained if the directory is moved
        const relative = path.relative(path.dirname(dest), src);
        yield fsSymlink(relative, dest);
      }
    } catch (err) {
      if (err.code === 'EEXIST') {
        // race condition
        yield symlink(src, dest);
      } else {
        throw err;
      }
    }
  });

  return function symlink(_x12, _x13) {
    return _ref8.apply(this, arguments);
  };
})();

let walk = exports.walk = (() => {
  var _ref9 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (dir, relativeDir) {
    let ignoreBasenames = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : new (_set || _load_set()).default();

    let files = [];

    let filenames = yield readdir(dir);
    if (ignoreBasenames.size) {
      filenames = filenames.filter(function (name) {
        return !ignoreBasenames.has(name);
      });
    }

    for (const name of filenames) {
      const relative = relativeDir ? path.join(relativeDir, name) : name;
      const loc = path.join(dir, name);
      const stat = yield lstat(loc);

      files.push({
        relative: relative,
        basename: name,
        absolute: loc,
        mtime: +stat.mtime
      });

      if (stat.isDirectory()) {
        files = files.concat((yield walk(loc, relative, ignoreBasenames)));
      }
    }

    return files;
  });

  return function walk(_x15, _x16) {
    return _ref9.apply(this, arguments);
  };
})();

let getFileSizeOnDisk = exports.getFileSizeOnDisk = (() => {
  var _ref10 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (loc) {
    const stat = yield lstat(loc);
    const size = stat.size;
    const blockSize = stat.blksize;


    return Math.ceil(size / blockSize) * blockSize;
  });

  return function getFileSizeOnDisk(_x17) {
    return _ref10.apply(this, arguments);
  };
})();

exports.copy = copy;
exports.readFile = readFile;
exports.readFileRaw = readFileRaw;
exports.normalizeOS = normalizeOS;

var _blockingQueue;

function _load_blockingQueue() {
  return _blockingQueue = _interopRequireDefault(require('./blocking-queue.js'));
}

var _promise2;

function _load_promise2() {
  return _promise2 = _interopRequireWildcard(require('./promise.js'));
}

var _promise3;

function _load_promise3() {
  return _promise3 = require('./promise.js');
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./map.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');
const fs = require('fs');

const lockQueue = exports.lockQueue = new (_blockingQueue || _load_blockingQueue()).default('fs lock');

const readFileBuffer = exports.readFileBuffer = (0, (_promise3 || _load_promise3()).promisify)(fs.readFile);
const writeFile = exports.writeFile = (0, (_promise3 || _load_promise3()).promisify)(fs.writeFile);
const readlink = exports.readlink = (0, (_promise3 || _load_promise3()).promisify)(fs.readlink);
const realpath = exports.realpath = (0, (_promise3 || _load_promise3()).promisify)(fs.realpath);
const readdir = exports.readdir = (0, (_promise3 || _load_promise3()).promisify)(fs.readdir);
const rename = exports.rename = (0, (_promise3 || _load_promise3()).promisify)(fs.rename);
const access = exports.access = (0, (_promise3 || _load_promise3()).promisify)(fs.access);
const stat = exports.stat = (0, (_promise3 || _load_promise3()).promisify)(fs.stat);
const unlink = exports.unlink = (0, (_promise3 || _load_promise3()).promisify)(require('rimraf'));
const mkdirp = exports.mkdirp = (0, (_promise3 || _load_promise3()).promisify)(require('mkdirp'));
const exists = exports.exists = (0, (_promise3 || _load_promise3()).promisify)(fs.exists, true);
const lstat = exports.lstat = (0, (_promise3 || _load_promise3()).promisify)(fs.lstat);
const chmod = exports.chmod = (0, (_promise3 || _load_promise3()).promisify)(fs.chmod);

const fsSymlink = (0, (_promise3 || _load_promise3()).promisify)(fs.symlink);
const invariant = require('invariant');
const stripBOM = require('strip-bom');

const noop = () => {};

function copy(src, dest) {
  return copyBulk([{ src: src, dest: dest }]);
}

function _readFile(loc, encoding) {
  return new (_promise || _load_promise()).default((resolve, reject) => {
    fs.readFile(loc, encoding, function (err, content) {
      if (err) {
        reject(err);
      } else {
        resolve(content);
      }
    });
  });
}

function readFile(loc) {
  return _readFile(loc, 'utf8').then(normalizeOS);
}

function readFileRaw(loc) {
  return _readFile(loc, 'binary');
}

function normalizeOS(body) {
  return body.replace(/\r\n/g, '\n');
}