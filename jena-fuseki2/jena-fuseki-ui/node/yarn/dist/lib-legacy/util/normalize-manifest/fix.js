'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _toArray2;

function _load_toArray() {
  return _toArray2 = _interopRequireDefault(require('babel-runtime/helpers/toArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _util;

function _load_util() {
  return _util = require('./util.js');
}

var _index;

function _load_index() {
  return _index = require('../../resolvers/index.js');
}

var _inferLicense;

function _load_inferLicense() {
  return _inferLicense = _interopRequireDefault(require('./infer-license.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const semver = require('semver');
const path = require('path');
const url = require('url');

const LICENSE_RENAMES = {
  'MIT/X11': 'MIT',
  X11: 'MIT'
};

exports.default = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (info, moduleLoc, reporter, looseSemver) {
    const files = yield (_fs || _load_fs()).readdir(moduleLoc);

    // clean info.version
    if (typeof info.version === 'string' && !semver.valid(info.version)) {
      info.version = semver.clean(info.version, looseSemver) || info.version;
    }

    // if name or version aren't set then set them to empty strings
    info.name = info.name || '';
    info.version = info.version || '';

    // if the man field is a string then coerce it to an array
    if (typeof info.man === 'string') {
      info.man = [info.man];
    }

    // if the keywords field is a string then split it on any whitespace
    if (typeof info.keywords === 'string') {
      info.keywords = info.keywords.split(/\s+/g);
    }

    // if there's no contributors field but an authors field then expand it
    if (!info.contributors && files.indexOf('AUTHORS') >= 0) {
      let authors = yield (_fs || _load_fs()).readFile(path.join(moduleLoc, 'AUTHORS'));
      authors = authors.split(/\r?\n/g) // split on lines
      .map(function (line) {
        return line.replace(/^\s*#.*$/, '').trim();
      }) // remove comments
      .filter(function (line) {
        return !!line;
      }); // remove empty lines
      info.contributors = authors;
    }

    // expand people fields to objects
    if (typeof info.author === 'string' || typeof info.author === 'object') {
      info.author = (0, (_util || _load_util()).normalizePerson)(info.author);
    }
    if (Array.isArray(info.contributors)) {
      info.contributors = info.contributors.map((_util || _load_util()).normalizePerson);
    }
    if (Array.isArray(info.maintainers)) {
      info.maintainers = info.maintainers.map((_util || _load_util()).normalizePerson);
    }

    // if there's no readme field then load the README file from the cwd
    if (!info.readme) {
      const readmeFilename = files.find(function (filename) {
        const lower = filename.toLowerCase();
        return lower === 'readme' || lower.indexOf('readme.') === 0;
      });

      if (readmeFilename) {
        info.readmeFilename = readmeFilename;
        info.readme = yield (_fs || _load_fs()).readFile(path.join(moduleLoc, readmeFilename));
      }
    }

    // if there's no description then take the first paragraph from the readme
    if (!info.description && info.readme) {
      const desc = (0, (_util || _load_util()).extractDescription)(info.readme);
      if (desc) {
        info.description = desc;
      }
    }

    // support array of engine keys
    if (Array.isArray(info.engines)) {
      const engines = {};
      for (const str of info.engines) {
        if (typeof str === 'string') {
          var _str$trim$split = str.trim().split(/ +/g);

          var _str$trim$split2 = (0, (_toArray2 || _load_toArray()).default)(_str$trim$split);

          const name = _str$trim$split2[0];

          const patternParts = _str$trim$split2.slice(1);

          engines[name] = patternParts.join(' ');
        }
      }
      info.engines = engines;
    }

    // if the repository field is a string then assume it's a git repo and expand it
    if (typeof info.repository === 'string') {
      info.repository = {
        type: 'git',
        url: info.repository
      };
    }

    const repo = info.repository;

    // explode info.repository.url if it's a hosted git shorthand
    if (repo && typeof repo === 'object' && typeof repo.url === 'string') {
      repo.url = (0, (_index || _load_index()).hostedGitFragmentToGitUrl)(repo.url, reporter);
    }

    // allow bugs to be specified as a string, expand it to an object with a single url prop
    if (typeof info.bugs === 'string') {
      info.bugs = { url: info.bugs };
    }

    // normalize homepage url to http
    if (typeof info.homepage === 'string') {
      const parts = url.parse(info.homepage);
      parts.protocol = parts.protocol || 'http:';
      if (parts.pathname && !parts.hostname) {
        parts.hostname = parts.pathname;
        parts.pathname = '';
      }
      // $FlowFixMe: https://github.com/facebook/flow/issues/908
      info.homepage = url.format(parts);
    }

    // if the `bin` field is as string then expand it to an object with a single property
    // based on the original `bin` field and `name field`
    // { name: "foo", bin: "cli.js" } -> { name: "foo", bin: { foo: "cli.js" } }
    if (typeof info.name === 'string' && typeof info.bin === 'string') {
      info.bin = { [info.name]: info.bin };
    }

    // bundleDependencies is an alias for bundledDependencies
    if (info.bundledDependencies) {
      info.bundleDependencies = info.bundledDependencies;
      delete info.bundledDependencies;
    }

    let scripts;

    // dummy script object to shove file inferred scripts onto
    if (info.scripts && typeof info.scripts === 'object') {
      scripts = info.scripts;
    } else {
      scripts = {};
    }

    // if there's a server.js file and no start script then set it to `node server.js`
    if (!scripts.start && files.indexOf('server.js') >= 0) {
      scripts.start = 'node server.js';
    }

    // if there's a binding.gyp file and no install script then set it to `node-gyp rebuild`
    if (!scripts.install && files.indexOf('binding.gyp') >= 0) {
      scripts.install = 'node-gyp rebuild';
    }

    // set scripts if we've polluted the empty object
    if ((0, (_keys || _load_keys()).default)(scripts).length) {
      info.scripts = scripts;
    }

    const dirs = info.directories;

    if (dirs && typeof dirs === 'object') {
      const binDir = dirs.bin;

      if (!info.bin && binDir && typeof binDir === 'string') {
        const bin = info.bin = {};

        for (const scriptName of yield (_fs || _load_fs()).readdir(path.join(moduleLoc, binDir))) {
          if (scriptName[0] === '.') {
            continue;
          }
          bin[scriptName] = path.join('.', binDir, scriptName);
        }
      }

      const manDir = dirs.man;

      if (!info.man && typeof manDir === 'string') {
        const man = info.man = [];

        for (const filename of yield (_fs || _load_fs()).readdir(path.join(moduleLoc, manDir))) {
          if (/^(.*?)\.[0-9]$/.test(filename)) {
            man.push(path.join('.', manDir, filename));
          }
        }
      }
    }

    delete info.directories;

    // normalize licenses field
    const licenses = info.licenses;
    if (Array.isArray(licenses) && !info.license) {
      let licenseTypes = [];

      for (let license of licenses) {
        if (license && typeof license === 'object') {
          license = license.type;
        }
        if (typeof license === 'string') {
          licenseTypes.push(license);
        }
      }

      licenseTypes = licenseTypes.filter((_util || _load_util()).isValidLicense);

      if (licenseTypes.length === 1) {
        info.license = licenseTypes[0];
      } else if (licenseTypes.length) {
        info.license = `(${ licenseTypes.join(' OR ') })`;
      }
    }

    const license = info.license;

    // normalize license
    if (license && typeof license === 'object') {
      info.license = license.type;
    }

    // get license file
    const licenseFile = files.find(function (filename) {
      const lower = filename.toLowerCase();
      return lower === 'license' || lower.startsWith('license.') || lower === 'unlicense' || lower.startsWith('unlicense.');
    });
    if (licenseFile) {
      const licenseContent = yield (_fs || _load_fs()).readFile(path.join(moduleLoc, licenseFile));
      const inferredLicense = (0, (_inferLicense || _load_inferLicense()).default)(licenseContent);
      info.licenseText = licenseContent;

      const license = info.license;

      if (typeof license === 'string') {
        if (inferredLicense && (0, (_util || _load_util()).isValidLicense)(inferredLicense) && !(0, (_util || _load_util()).isValidLicense)(license)) {
          // some packages don't specify their license version but we can infer it based on their license file
          const basicLicense = license.toLowerCase().replace(/(-like|\*)$/g, '');
          const expandedLicense = inferredLicense.toLowerCase();
          if (expandedLicense.startsWith(basicLicense)) {
            // TODO consider doing something to notify the user
            info.license = inferredLicense;
          }
        }
      } else if (inferredLicense) {
        // if there's no license then infer it based on the license file
        info.license = inferredLicense;
      } else {
        // valid expression to refer to a license in a file
        info.license = `SEE LICENSE IN ${ licenseFile }`;
      }
    }

    if (typeof info.license === 'string') {
      // sometimes licenses are known by different names, reduce them
      info.license = LICENSE_RENAMES[info.license] || info.license;
    } else if (typeof info.readme === 'string') {
      // the license might be at the bottom of the README
      const inferredLicense = (0, (_inferLicense || _load_inferLicense()).default)(info.readme);
      if (inferredLicense) {
        info.license = inferredLicense;
      }
    }
  });

  return function (_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();