'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _errors;

function _load_errors() {
  return _errors = require('../errors.js');
}

var _misc;

function _load_misc() {
  return _misc = require('./misc.js');
}

var _crypto;

function _load_crypto() {
  return _crypto = _interopRequireWildcard(require('./crypto.js'));
}

var _child;

function _load_child() {
  return _child = _interopRequireWildcard(require('./child.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('./fs.js'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./map.js'));
}

var _fs2;

function _load_fs2() {
  return _fs2 = require('fs');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

const semver = require('semver');
const url = require('url');
const tar = require('tar');


const supportsArchiveCache = (0, (_map || _load_map()).default)({
  'github.com': false });

class Git {
  constructor(config, url, hash) {
    Git.assertUrl(url, hash);

    this.supportsArchive = false;
    this.fetched = false;
    this.config = config;
    this.hash = hash;
    this.ref = hash;
    this.url = Git.cleanUrl(url);
    this.cwd = this.config.getTemp((_crypto || _load_crypto()).hash(this.url));
  }

  static cleanUrl(url) {
    return url.replace(/^git\+/, '');
  }

  /**
   * Check if the host specified in the input `gitUrl` has archive capability.
   */

  static hasArchiveCapability(gitUrl) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // USER@HOSTNAME:PATHNAME
      const match = gitUrl.match(/^(.*?)@(.*?):(.*?)$/);
      if (!match) {
        return false;
      }

      var _match = (0, (_slicedToArray2 || _load_slicedToArray()).default)(match, 3);

      const hostname = _match[2];

      const cached = supportsArchiveCache[hostname];
      if (cached != null) {
        return cached;
      }

      try {
        yield (_child || _load_child()).spawn('git', ['archive', `--remote=${ gitUrl }`, 'HEAD', Date.now() + '']);
        throw new Error();
      } catch (err) {
        const supports = err.message.indexOf('did not match any files') >= 0;
        return supportsArchiveCache[hostname] = supports;
      }
    })();
  }

  /**
   * Check if the input `target` is a 40 character hex commit hash.
   */

  static isCommitHash(target) {
    return !!target && /^[a-f0-9]{5,40}$/.test(target);
  }

  /**
   * Assert that a URL is safe to fetch from. Forbid insecure URLs like plain HTTP with no
   * hash.
   */

  static assertUrl(ref, hash) {
    if (Git.isCommitHash(hash)) {
      // this is cryptographically secure
      return;
    }

    const parts = url.parse(ref);

    if (parts.protocol === 'git') {
      throw new (_errors || _load_errors()).SecurityError(`Refusing to download the git repo ${ ref } over plain git without a commit hash`);
    }

    if (parts.protocol === 'http:') {
      throw new (_errors || _load_errors()).SecurityError(`Refusing to download the git repo ${ ref } over HTTP without a commit hash`);
    }
  }

  /**
   * Archive a repo to destination
   */

  archive(dest) {
    if (this.supportsArchive) {
      return this._archiveViaRemoteArchive(dest);
    } else {
      return this._archiveViaLocalFetched(dest);
    }
  }

  _archiveViaRemoteArchive(dest) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const hashStream = new (_crypto || _load_crypto()).HashStream();
      yield (_child || _load_child()).spawn('git', ['archive', `--remote=${ _this.url }`, _this.ref], {
        process(proc, resolve, reject, done) {
          const writeStream = (0, (_fs2 || _load_fs2()).createWriteStream)(dest);
          proc.on('error', reject);
          writeStream.on('error', reject);
          writeStream.on('end', done);
          writeStream.on('open', function () {
            proc.stdout.pipe(hashStream).pipe(writeStream);
          });
          writeStream.once('finish', done);
        }
      });
      return hashStream.getHash();
    })();
  }

  _archiveViaLocalFetched(dest) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const hashStream = new (_crypto || _load_crypto()).HashStream();
      yield (_child || _load_child()).spawn('git', ['archive', _this2.hash], {
        cwd: _this2.cwd,
        process(proc, resolve, reject, done) {
          const writeStream = (0, (_fs2 || _load_fs2()).createWriteStream)(dest);
          proc.on('error', reject);
          writeStream.on('error', reject);
          writeStream.on('open', function () {
            proc.stdout.pipe(hashStream).pipe(writeStream);
          });
          writeStream.once('finish', done);
        }
      });
      return hashStream.getHash();
    })();
  }

  /**
   * Clone a repo to the input `dest`. Use `git archive` if it's available, otherwise fall
   * back to `git clone`.
   */

  clone(dest) {
    if (this.supportsArchive) {
      return this._cloneViaRemoteArchive(dest);
    } else {
      return this._cloneViaLocalFetched(dest);
    }
  }

  _cloneViaRemoteArchive(dest) {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      yield (_child || _load_child()).spawn('git', ['archive', `--remote=${ _this3.url }`, _this3.ref], {
        process(proc, update, reject, done) {
          const extractor = tar.Extract({ path: dest });
          extractor.on('error', reject);
          extractor.on('end', done);

          proc.stdout.pipe(extractor);
          proc.on('error', reject);
        }
      });
    })();
  }

  _cloneViaLocalFetched(dest) {
    var _this4 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      yield (_child || _load_child()).spawn('git', ['archive', _this4.hash], {
        cwd: _this4.cwd,
        process(proc, resolve, reject, done) {
          const extractor = tar.Extract({ path: dest });
          extractor.on('error', reject);
          extractor.on('end', done);

          proc.stdout.pipe(extractor);
        }
      });
    })();
  }

  /**
   * Clone this repo.
   */

  fetch() {
    var _this5 = this;

    const url = this.url;
    const cwd = this.cwd;


    return (_fs || _load_fs()).lockQueue.push(url, (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (yield (_fs || _load_fs()).exists(cwd)) {
        yield (_child || _load_child()).spawn('git', ['pull'], { cwd });
      } else {
        yield (_child || _load_child()).spawn('git', ['clone', url, cwd]);
      }

      _this5.fetched = true;
    }));
  }

  /**
   * Given a list of tags/branches from git, check if they match an input range.
   */

  findResolution(range, tags) {
    var _this6 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // If there are no tags and target is *, fallback to the latest commit on master
      // or if we have no target.
      if (!range || !tags.length && range === '*') {
        return 'master';
      }

      return (yield _this6.config.resolveConstraints(tags.filter(function (tag) {
        return !!semver.valid(tag, _this6.config.looseSemver);
      }), range)) || range;
    })();
  }

  /**
   * Fetch the file by cloning the repo and reading it.
   */

  getFile(filename) {
    if (this.supportsArchive) {
      return this._getFileFromArchive(filename);
    } else {
      return this._getFileFromClone(filename);
    }
  }

  _getFileFromArchive(filename) {
    var _this7 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      try {
        return yield (_child || _load_child()).spawn('git', ['archive', `--remote=${ _this7.url }`, _this7.ref, filename], {
          process(proc, update, reject, done) {
            const parser = tar.Parse();

            parser.on('error', reject);
            parser.on('end', done);

            parser.on('data', entry => {
              update(entry.toString());
            });

            proc.stdout.pipe(parser);
          }
        });
      } catch (err) {
        if (err.message.indexOf('did not match any files') >= 0) {
          return false;
        } else {
          throw err;
        }
      }
    })();
  }

  _getFileFromClone(filename) {
    var _this8 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      invariant(_this8.fetched, 'Repo not fetched');

      try {
        return yield (_child || _load_child()).spawn('git', ['show', `${ _this8.hash }:${ filename }`], { cwd: _this8.cwd });
      } catch (err) {
        // file doesn't exist
        return false;
      }
    })();
  }

  /**
   * Try and find a ref from this repo that matches an input `target`.
   */

  initRemote() {
    var _this9 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // check capabilities
      if (yield Git.hasArchiveCapability(_this9.url)) {
        _this9.supportsArchive = true;
      } else {
        yield _this9.fetch();
      }

      return yield _this9.setRefRemote();
    })();
  }

  setRefRemote() {
    var _this10 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const stdout = yield (_child || _load_child()).spawn('git', ['ls-remote', '--tags', '--heads', _this10.url]);
      const refs = Git.parseRefs(stdout);
      return yield _this10.setRef(refs);
    })();
  }

  /**
   * TODO description
   */

  setRef(refs) {
    var _this11 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // get commit ref
      const hash = _this11.hash;


      const names = Object.keys(refs);

      if (Git.isCommitHash(hash)) {
        for (const name in refs) {
          if (refs[name] === hash) {
            _this11.ref = name;
            return hash;
          }
        }

        // `git archive` only accepts a treeish and we have no ref to this commit
        _this11.supportsArchive = false;
        return _this11.ref = _this11.hash = hash;
      }

      const ref = yield _this11.findResolution(hash, names);
      const commit = refs[ref];
      if (commit) {
        _this11.ref = ref;
        return _this11.hash = commit;
      } else {
        throw new (_errors || _load_errors()).MessageError(_this11.config.reporter.lang('couldntFindMatch', ref, names.join(','), _this11.url));
      }
    })();
  }

  /**
   * TODO description
   */

  static parseRefs(stdout) {
    // store references
    const refs = {};

    // line delimited
    const refLines = stdout.split('\n');

    for (const line of refLines) {
      // line example: 64b2c0cee9e829f73c5ad32b8cc8cb6f3bec65bb refs/tags/v4.2.2
      var _line$split = line.split(/\s+/g);

      var _line$split2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_line$split, 2);

      const sha = _line$split2[0];
      const id = _line$split2[1];

      let name = id.split('/').slice(2).join('/');

      // TODO: find out why this is necessary. idk it makes it work...
      name = (0, (_misc || _load_misc()).removeSuffix)(name, '^{}');

      refs[name] = sha;
    }

    return refs;
  }
}
exports.default = Git;