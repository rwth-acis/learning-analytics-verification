const ConsentRegistry = artifacts.require('ConsentRegistry')
const VerificationRegistry = artifacts.require('VerificationRegistry')

module.exports = function (deployer) {
    deployer.deploy(VerificationRegistry)
    deployer.deploy(ConsentRegistry)
};
