const ConsentRegistry = artifacts.require('ConsentRegistry')
const TransactionLogRegistry = artifacts.require('TransactionLogRegistry')

module.exports = function (deployer) {
    deployer.deploy(TransactionLogRegistry)
    deployer.deploy(ConsentRegistry)
};
