'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getPathKey = getPathKey;
const userHome = require('user-home');
const path = require('path');

const DEPENDENCY_TYPES = exports.DEPENDENCY_TYPES = ['devDependencies', 'dependencies', 'optionalDependencies', 'peerDependencies'];

const YARN_REGISTRY = exports.YARN_REGISTRY = 'https://registry.yarnpkg.com';

// lockfile version, bump whenever we make backwards incompatible changes
const LOCKFILE_VERSION = exports.LOCKFILE_VERSION = 1;

// max amount of network requests to perform concurrently
const NETWORK_CONCURRENCY = exports.NETWORK_CONCURRENCY = 15;

// max amount of child processes to execute concurrently
const CHILD_CONCURRENCY = exports.CHILD_CONCURRENCY = 5;

const REQUIRED_PACKAGE_KEYS = exports.REQUIRED_PACKAGE_KEYS = ['name', 'version', '_uid'];

function getDirectory(type) {
  // use %LOCALAPPDATA%/Yarn on Windows
  if (process.platform === 'win32' && process.env.LOCALAPPDATA) {
    return path.join(process.env.LOCALAPPDATA, 'Yarn', type);
  }

  // otherwise use ~/.yarn
  return path.join(userHome, `.yarn-${ type }`);
}

const GLOBAL_INSTALL_DIRECTORY = exports.GLOBAL_INSTALL_DIRECTORY = path.join(userHome, '.yarn');
const MODULE_CACHE_DIRECTORY = exports.MODULE_CACHE_DIRECTORY = getDirectory('cache');
const LINK_REGISTRY_DIRECTORY = exports.LINK_REGISTRY_DIRECTORY = getDirectory('config/link');
const GLOBAL_MODULE_DIRECTORY = exports.GLOBAL_MODULE_DIRECTORY = getDirectory('config/global');
const CACHE_FILENAME = exports.CACHE_FILENAME = path.join(GLOBAL_INSTALL_DIRECTORY, '.roadrunner.json');

const INTEGRITY_FILENAME = exports.INTEGRITY_FILENAME = '.yarn-integrity';
const LOCKFILE_FILENAME = exports.LOCKFILE_FILENAME = 'yarn.lock';
const METADATA_FILENAME = exports.METADATA_FILENAME = '.yarn-metadata.json';
const TARBALL_FILENAME = exports.TARBALL_FILENAME = '.yarn-tarball.tgz';
const CLEAN_FILENAME = exports.CLEAN_FILENAME = '.yarnclean';

const DEFAULT_INDENT = exports.DEFAULT_INDENT = '  ';
const SINGLE_INSTANCE_PORT = exports.SINGLE_INSTANCE_PORT = 31997;
const SINGLE_INSTANCE_FILENAME = exports.SINGLE_INSTANCE_FILENAME = '.yarn-single-instance';

const SELF_UPDATE_VERSION_URL = exports.SELF_UPDATE_VERSION_URL = 'https://yarnpkg.com/latest-version';
const SELF_UPDATE_TARBALL_URL = exports.SELF_UPDATE_TARBALL_URL = 'https://yarnpkg.com/latest.tar.gz';
const SELF_UPDATE_DOWNLOAD_FOLDER = exports.SELF_UPDATE_DOWNLOAD_FOLDER = 'updates';

const ENV_PATH_KEY = exports.ENV_PATH_KEY = getPathKey(process.platform, process.env);

function getPathKey(platform, env) {
  let pathKey = 'PATH';

  // windows calls it's path "Path" usually, but this is not guaranteed.
  if (platform === 'win32') {
    pathKey = 'Path';

    for (const key in env) {
      if (key.toLowerCase() === 'path') {
        pathKey = key;
      }
    }
  }

  return pathKey;
}