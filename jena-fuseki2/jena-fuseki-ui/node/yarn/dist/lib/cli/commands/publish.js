'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let publish = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, pkg, flags, dir) {
    // validate access argument
    const access = flags.access;
    if (access && access !== 'public' && access !== 'restricted') {
      throw new (_errors || _load_errors()).MessageError(config.reporter.lang('invalidAccess'));
    }

    // get tarball stream
    const stat = yield (_fs || _load_fs()).lstat(dir);
    let stream;
    if (stat.isDirectory()) {
      stream = yield (0, (_pack || _load_pack()).pack)(config, dir);
    } else if (stat.isFile()) {
      stream = fs2.createReadStream(dir);
    } else {
      throw new Error("Don't know how to handle this file type");
    }
    invariant(stream, 'expected stream');
    const buffer = yield new Promise(function (resolve, reject) {
      stream.pipe(new (_stream || _load_stream()).ConcatStream(resolve)).on('error', reject);
    });

    // copy normalized package and remove internal keys as they may be sensitive or yarn specific
    pkg = Object.assign({}, pkg);
    for (const key in pkg) {
      if (key[0] === '_') {
        delete pkg[key];
      }
    }

    const tag = flags.tag || 'latest';
    const tbName = `${ pkg.name }-${ pkg.version }.tgz`;
    const tbURI = `${ pkg.name }/-/${ tbName }`;

    // TODO this might modify package.json, do we need to reload it?
    yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'prepublish');

    // create body
    const root = {
      _id: pkg.name,
      access: flags.access,
      name: pkg.name,
      description: pkg.description,
      'dist-tags': {
        [tag]: pkg.version
      },
      versions: {
        [pkg.version]: pkg
      },
      readme: pkg.readme || '',
      _attachments: {
        [tbName]: {
          'content_type': 'application/octet-stream',
          data: buffer.toString('base64'),
          length: buffer.length
        }
      }
    };

    pkg._id = `${ pkg.name }@${ pkg.version }`;
    pkg.dist = pkg.dist || {};
    pkg.dist.shasum = crypto.createHash('sha1').update(buffer).digest('hex');

    const registry = String(config.getOption('registry'));
    pkg.dist.tarball = url.resolve(registry, tbURI).replace(/^https:\/\//, 'http://');

    // publish package
    const res = yield config.registries.npm.request((_npmRegistry || _load_npmRegistry()).default.escapeName(pkg.name), {
      method: 'PUT',
      body: root
    });

    if (res != null && res.success) {
      yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'publish');
      yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'postpublish');
    } else {
      throw new (_errors || _load_errors()).MessageError(config.reporter.lang('publishFail'));
    }
  });

  return function publish(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    // validate package fields that are required for publishing
    const pkg = yield config.readRootManifest();
    if (pkg.private) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('publishPrivate'));
    }
    if (!pkg.name) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('noName'));
    }

    // validate arguments
    const dir = args[0] || config.cwd;
    if (args.length > 1) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('tooManyArguments', 1));
    }
    if (!(yield (_fs || _load_fs()).exists(dir))) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('unknownFolderOrTarball'));
    }

    //
    reporter.step(1, 4, reporter.lang('bumpingVersion'));
    const commitVersion = yield (0, (_version || _load_version()).setVersion)(config, reporter, flags, args, false);

    //
    reporter.step(2, 4, reporter.lang('loggingIn'));
    const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, pkg.name);

    //
    reporter.step(3, 4, reporter.lang('publishing'));
    yield publish(config, pkg, flags, dir);
    yield commitVersion();
    reporter.success(reporter.lang('published'));

    //
    reporter.step(4, 4, reporter.lang('revokingToken'));
    yield revoke();
  });

  return function run(_x5, _x6, _x7, _x8) {
    return _ref2.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _executeLifecycleScript;

function _load_executeLifecycleScript() {
  return _executeLifecycleScript = _interopRequireDefault(require('./_execute-lifecycle-script.js'));
}

var _npmRegistry;

function _load_npmRegistry() {
  return _npmRegistry = _interopRequireDefault(require('../../registries/npm-registry.js'));
}

var _stream;

function _load_stream() {
  return _stream = require('../../util/stream.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _version;

function _load_version() {
  return _version = require('./version.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _pack;

function _load_pack() {
  return _pack = require('./pack.js');
}

var _login;

function _load_login() {
  return _login = require('./login.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const crypto = require('crypto');
const url = require('url');
const fs2 = require('fs');

function setFlags(commander) {
  (0, (_version || _load_version()).setFlags)(commander);
  commander.usage('publish [<tarball>|<folder>] [--tag <tag>] [--access <public|restricted>]');
  commander.option('--access [access]', 'access');
  commander.option('--tag [tag]', 'tag');
}