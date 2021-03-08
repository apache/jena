'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _hostedGitResolver;

function _load_hostedGitResolver() {
  return _hostedGitResolver = _interopRequireDefault(require('./hosted-git-resolver.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class BitbucketResolver extends (_hostedGitResolver || _load_hostedGitResolver()).default {

  static getTarballUrl(parts, hash) {
    return `https://bitbucket.org/${ parts.user }/${ parts.repo }/get/${ hash }.tar.gz`;
  }

  static getGitHTTPUrl(parts) {
    return `https://bitbucket.org/${ parts.user }/${ parts.repo }.git`;
  }

  static getGitSSHUrl(parts) {
    return `git@bitbucket.org:${ parts.user }/${ parts.repo }.git`;
  }

  static getHTTPFileUrl(parts, filename, commit) {
    return `https://bitbucket.org/${ parts.user }/${ parts.repo }/raw/${ commit }/${ filename }`;
  }
}
exports.default = BitbucketResolver;
BitbucketResolver.hostname = 'bitbucket.org';
BitbucketResolver.protocol = 'bitbucket';