const Delegation = artifacts.require('Delegation')
const ConsentRegistry = artifacts.require('ConsentRegistry')
const TransactionLogRegistry = artifacts.require('TransactionLogRegistry')

module.exports = function (deployer) {
    deployer.deploy(TransactionLogRegistry)
    deployer.deploy(ConsentRegistry)

    // return deployer.deploy(Delegation).then(function () {
    //     deployer.link(Delegation, ConsentRegistry)
    // }).then(function() {
    //      return deployer.deploy(ConsentRegistry)
    // });
};
