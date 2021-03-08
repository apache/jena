'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.requireLockfile = undefined;

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let cleanQuery = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, query) {
    // if a location was passed then turn it into a hash query
    if (path.isAbsolute(query) && (yield (_fs || _load_fs()).exists(query))) {
      // absolute path
      query = path.relative(config.cwd, query);
    }

    // remove references to node_modules with hashes
    query = query.replace(/([\\/]|^)node_modules[\\/]/g, '#');

    // remove trailing hashes
    query = query.replace(/^#+/g, '');

    // remove path after last hash
    query = query.replace(/[\\/](.*?)$/g, '');

    return query;
  });

  return function cleanQuery(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let getPackageSize = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (_ref3) {
    var _ref4 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref3, 2);

    let loc = _ref4[0];
    let info = _ref4[1];

    const files = yield (_fs || _load_fs()).walk(loc, null, new Set([(_constants || _load_constants()).METADATA_FILENAME, (_constants || _load_constants()).TARBALL_FILENAME]));
    const sizes = yield Promise.all(files.map(function (walkFile) {
      return (_fs || _load_fs()).getFileSizeOnDisk(walkFile.absolute);
    }));

    return sum(sizes);
  });

  return function getPackageSize(_x3) {
    return _ref2.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref8 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    if (!args.length) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('missingWhyDependency'));
    }
    if (args.length > 1) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('tooManyArguments', 1));
    }

    const query = yield cleanQuery(config, args[0]);

    reporter.step(1, 4, reporter.lang('whyStart', args[0]), emoji.get('thinking_face'));

    // init
    reporter.step(2, 4, reporter.lang('whyInitGraph'), emoji.get('truck'));
    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd, reporter);
    const install = new (_install || _load_install()).Install(flags, config, reporter, lockfile);

    var _ref9 = yield install.fetchRequestFromCwd();

    var _ref10 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref9, 2);

    const depRequests = _ref10[0];
    const patterns = _ref10[1];

    yield install.resolver.init(depRequests, install.flags.flat);
    const hoisted = yield install.linker.getFlatHoistedTree(patterns);

    // finding
    reporter.step(3, 4, reporter.lang('whyFinding'), emoji.get('mag'));

    let match;
    for (const _ref11 of hoisted) {
      var _ref12 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref11, 2);

      const loc = _ref12[0];
      const info = _ref12[1];

      if (info.key === query || info.previousKeys.indexOf(query) >= 0) {
        match = [loc, info];
        break;
      }
    }

    if (!match) {
      reporter.error(reporter.lang('whyUnknownMatch'));
      return;
    }

    var _match = match;

    var _match2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_match, 2);

    const matchInfo = _match2[1];

    const matchRef = matchInfo.pkg._reference;
    invariant(matchRef, 'expected reference');

    const matchPatterns = matchRef.patterns;
    const matchRequests = matchRef.requests;

    const reasons = [];
    // reason: dependency of these modules
    for (const request of matchRequests) {
      const parentRequest = request.parentRequest;
      if (!parentRequest) {
        continue;
      }

      const dependent = install.resolver.getResolvedPattern(parentRequest.pattern);
      if (!dependent) {
        continue;
      }

      const chain = [];

      let delegator = parentRequest;
      do {
        chain.push(install.resolver.getStrictResolvedPattern(delegator.pattern).name);
      } while (delegator = delegator.parentRequest);

      reasons.push({
        type: 'whyDependedOn',
        typeSimple: 'whyDependedOnSimple',
        value: chain.reverse().join('#')
      });
    }

    // reason: exists in manifest
    let rootType;
    for (const pattern of matchPatterns) {
      rootType = install.rootPatternsToOrigin[pattern];
      if (rootType) {
        reasons.push({
          type: 'whySpecified',
          typeSimple: 'whySpecifiedSimple',
          value: rootType
        });
      }
    }

    // reason: this is hoisted from these modules
    for (const pattern of matchInfo.previousKeys) {
      if (pattern !== matchInfo.key) {
        reasons.push({
          type: 'whyHoistedFrom',
          typeSimple: 'whyHoistedFromSimple',
          value: pattern
        });
      }
    }

    // package sizes
    reporter.step(4, 4, reporter.lang('whyCalculating'), emoji.get('aerial_tramway'));

    let packageSize = 0;
    let directSizes = [];
    let transitiveSizes = [];
    try {
      packageSize = yield getPackageSize(match);
    } catch (e) {}

    const dependencies = Array.from(collect(hoisted, new Set(), match));
    const transitiveDependencies = Array.from(collect(hoisted, new Set(), match, { recursive: true }));

    try {
      directSizes = yield Promise.all(dependencies.map(getPackageSize));
      transitiveSizes = yield Promise.all(transitiveDependencies.map(getPackageSize));
    } catch (e) {}

    const transitiveKeys = new Set(transitiveDependencies.map(function (_ref13) {
      var _ref14 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref13, 2);

      let info = _ref14[1];
      return info.key;
    }));
    const sharedDependencies = getSharedDependencies(hoisted, transitiveKeys);

    //
    // reason: hoisted
    if (query === matchInfo.originalKey) {
      reporter.info(reporter.lang('whyHoistedTo', matchInfo.key));
    }

    if (reasons.length === 1) {
      reporter.info(reporter.lang(reasons[0].typeSimple, reasons[0].value));
    } else if (reasons.length > 1) {
      reporter.info(reporter.lang('whyReasons'));
      reporter.list('reasons', reasons.map(function (reason) {
        return reporter.lang(reason.type, reason.value);
      }));
    } else {
      reporter.error(reporter.lang('whyWhoKnows'));
    }

    if (packageSize) {
      // stats: file size of this dependency without any dependencies
      reporter.info(reporter.lang('whyDiskSizeWithout', bytes(packageSize)));

      // stats: file size of this dependency including dependencies that aren't shared
      reporter.info(reporter.lang('whyDiskSizeUnique', bytes(packageSize + sum(directSizes))));

      // stats: file size of this dependency including dependencies
      reporter.info(reporter.lang('whyDiskSizeTransitive', bytes(packageSize + sum(transitiveSizes))));

      // stats: shared transitive dependencies
      reporter.info(reporter.lang('whySharedDependencies', sharedDependencies.size));
    }
  });

  return function run(_x5, _x6, _x7, _x8) {
    return _ref8.apply(this, arguments);
  };
})();

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _constants;

function _load_constants() {
  return _constants = require('../../constants.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const requireLockfile = exports.requireLockfile = true;

const bytes = require('bytes');
const emoji = require('node-emoji');
const invariant = require('invariant');
const path = require('path');

const sum = array => array.length ? array.reduce((a, b) => a + b, 0) : 0;
const collect = function (hoistManifests, allDependencies, dependency) {
  var _ref5 = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : { recursive: false };

  let recursive = _ref5.recursive;

  var _dependency = (0, (_slicedToArray2 || _load_slicedToArray()).default)(dependency, 2);

  const depInfo = _dependency[1];

  const deps = depInfo.pkg.dependencies;

  if (!deps) {
    return allDependencies;
  }

  const dependencyKeys = new Set(Object.keys(deps));
  const directDependencies = [];

  for (const dep of hoistManifests) {
    var _dep = (0, (_slicedToArray2 || _load_slicedToArray()).default)(dep, 2);

    const info = _dep[1];


    if (!allDependencies.has(dep) && dependencyKeys.has(info.key)) {
      allDependencies.add(dep);
      directDependencies.push(dep);
    }
  }

  if (recursive) {
    directDependencies.forEach(dependency => collect(hoistManifests, allDependencies, dependency, { recursive: true }));
  }

  return allDependencies;
};
const getSharedDependencies = (hoistManifests, transitiveKeys) => {
  const sharedDependencies = new Set();
  for (const _ref6 of hoistManifests) {
    var _ref7 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref6, 2);

    const info = _ref7[1];

    if (!transitiveKeys.has(info.key) && info.pkg.dependencies) {
      Object.keys(info.pkg.dependencies).forEach(dependency => {
        if (transitiveKeys.has(dependency) && !sharedDependencies.has(dependency)) {
          sharedDependencies.add(dependency);
        }
      });
    }
  }
  return sharedDependencies;
};