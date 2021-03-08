'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.prune = exports.dedupe = exports.lockfile = exports.why = exports.version = exports.upgrade = exports.unlink = exports.team = exports.tag = exports.selfUpdate = exports.run = exports.remove = exports.publish = exports.pack = exports.owner = exports.outdated = exports.ls = exports.logout = exports.login = exports.link = exports.licenses = exports.install = exports.init = exports.info = exports.global = exports.generateLockEntry = exports.config = exports.clean = exports.check = exports.cache = exports.bin = exports.add = exports.access = undefined;

var _access;

function _load_access() {
  return _access = _interopRequireWildcard(require('./access.js'));
}

var _add;

function _load_add() {
  return _add = _interopRequireWildcard(require('./add.js'));
}

var _bin;

function _load_bin() {
  return _bin = _interopRequireWildcard(require('./bin.js'));
}

var _cache;

function _load_cache() {
  return _cache = _interopRequireWildcard(require('./cache.js'));
}

var _check;

function _load_check() {
  return _check = _interopRequireWildcard(require('./check.js'));
}

var _clean;

function _load_clean() {
  return _clean = _interopRequireWildcard(require('./clean.js'));
}

var _config;

function _load_config() {
  return _config = _interopRequireWildcard(require('./config.js'));
}

var _generateLockEntry;

function _load_generateLockEntry() {
  return _generateLockEntry = _interopRequireWildcard(require('./generate-lock-entry.js'));
}

var _global;

function _load_global() {
  return _global = _interopRequireWildcard(require('./global.js'));
}

var _info;

function _load_info() {
  return _info = _interopRequireWildcard(require('./info.js'));
}

var _init;

function _load_init() {
  return _init = _interopRequireWildcard(require('./init.js'));
}

var _install;

function _load_install() {
  return _install = _interopRequireWildcard(require('./install.js'));
}

var _licenses;

function _load_licenses() {
  return _licenses = _interopRequireWildcard(require('./licenses.js'));
}

var _link;

function _load_link() {
  return _link = _interopRequireWildcard(require('./link.js'));
}

var _login;

function _load_login() {
  return _login = _interopRequireWildcard(require('./login.js'));
}

var _logout;

function _load_logout() {
  return _logout = _interopRequireWildcard(require('./logout.js'));
}

var _ls;

function _load_ls() {
  return _ls = _interopRequireWildcard(require('./ls.js'));
}

var _outdated;

function _load_outdated() {
  return _outdated = _interopRequireWildcard(require('./outdated.js'));
}

var _owner;

function _load_owner() {
  return _owner = _interopRequireWildcard(require('./owner.js'));
}

var _pack;

function _load_pack() {
  return _pack = _interopRequireWildcard(require('./pack.js'));
}

var _publish;

function _load_publish() {
  return _publish = _interopRequireWildcard(require('./publish.js'));
}

var _remove;

function _load_remove() {
  return _remove = _interopRequireWildcard(require('./remove.js'));
}

var _run;

function _load_run() {
  return _run = _interopRequireWildcard(require('./run.js'));
}

var _selfUpdate;

function _load_selfUpdate() {
  return _selfUpdate = _interopRequireWildcard(require('./self-update.js'));
}

var _tag;

function _load_tag() {
  return _tag = _interopRequireWildcard(require('./tag.js'));
}

var _team;

function _load_team() {
  return _team = _interopRequireWildcard(require('./team.js'));
}

var _unlink;

function _load_unlink() {
  return _unlink = _interopRequireWildcard(require('./unlink.js'));
}

var _upgrade;

function _load_upgrade() {
  return _upgrade = _interopRequireWildcard(require('./upgrade.js'));
}

var _version;

function _load_version() {
  return _version = _interopRequireWildcard(require('./version.js'));
}

var _why;

function _load_why() {
  return _why = _interopRequireWildcard(require('./why.js'));
}

var _useless;

function _load_useless() {
  return _useless = _interopRequireDefault(require('./_useless.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

exports.access = _access || _load_access();
exports.add = _add || _load_add();
exports.bin = _bin || _load_bin();
exports.cache = _cache || _load_cache();
exports.check = _check || _load_check();
exports.clean = _clean || _load_clean();
exports.config = _config || _load_config();
exports.generateLockEntry = _generateLockEntry || _load_generateLockEntry();
exports.global = _global || _load_global();
exports.info = _info || _load_info();
exports.init = _init || _load_init();
exports.install = _install || _load_install();
exports.licenses = _licenses || _load_licenses();
exports.link = _link || _load_link();
exports.login = _login || _load_login();
exports.logout = _logout || _load_logout();
exports.ls = _ls || _load_ls();
exports.outdated = _outdated || _load_outdated();
exports.owner = _owner || _load_owner();
exports.pack = _pack || _load_pack();
exports.publish = _publish || _load_publish();
exports.remove = _remove || _load_remove();
exports.run = _run || _load_run();
exports.selfUpdate = _selfUpdate || _load_selfUpdate();
exports.tag = _tag || _load_tag();
exports.team = _team || _load_team();
exports.unlink = _unlink || _load_unlink();
exports.upgrade = _upgrade || _load_upgrade();
exports.version = _version || _load_version();
exports.why = _why || _load_why();
const lockfile = exports.lockfile = (0, (_useless || _load_useless()).default)("The lockfile command isn't necessary. `yarn install` will produce a lockfile.");

const dedupe = exports.dedupe = (0, (_useless || _load_useless()).default)("The dedupe command isn't necessary. `yarn install` will already dedupe.");

const prune = exports.prune = (0, (_useless || _load_useless()).default)("The prune command isn't necessary. `yarn install` will prune extraneous packages.");