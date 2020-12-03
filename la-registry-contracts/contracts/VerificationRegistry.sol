pragma solidity ^0.5.0;

contract VerificationRegistry {
    struct LogEntry {
        uint256 timestamp;
        bytes32 userId;
        bytes32 dataHash;
    }

    mapping(bytes32 => LogEntry[]) userToLogEntries;
    mapping(bytes32 => uint256) hashesToTimestamps;

    function createLogEntry(bytes32 userId, bytes32 dataHash) public {
        _createLogEntry(LogEntry(now, userId, dataHash));
    }

     function _createLogEntry(LogEntry memory logEntry) private {
         userToLogEntries[logEntry.userId].push(logEntry);
         hashesToTimestamps[logEntry.dataHash] = logEntry.timestamp;
     }

    function getLogEntries(bytes32 userId) public view returns(uint[] memory, bytes32[] memory) {
        uint[] memory timestamps = new uint[](userToLogEntries[userId].length);
        bytes32[] memory hashes = new bytes32[](userToLogEntries[userId].length);
        for (uint i = 0; i < userToLogEntries[userId].length; i++) {
            timestamps[i] = userToLogEntries[userId][i].timestamp;
            hashes[i] = userToLogEntries[userId][i].dataHash;
        }
        return (timestamps, hashes);
    }

    function getDataHashesForUser(bytes32 userId) public view returns (bytes32[] memory) {
        bytes32[] memory hashes = new bytes32[](userToLogEntries[userId].length);
        for (uint i = 0; i < userToLogEntries[userId].length; i++) {
            hashes[i] = userToLogEntries[userId][i].dataHash;
        }
        return hashes;
    }

    function hasHashBeenRecorded(bytes32 hash) public view returns (bool) {
        if (hashesToTimestamps[hash] != 0) {
            return true;
        }
        return false;
    }

}
