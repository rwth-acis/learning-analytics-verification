pragma solidity ^0.5.0;

contract ConsentRegistry {
    struct Consent {
        address owner;
        uint256 timestamp;
        bytes32 userId;
        uint8[] consentLevels;
    }

    // Mapping from user's ID to user's consent
    mapping(bytes32 => Consent) userConsent;

    // Checks for given userId (las2peer loginname) if a consent is stored already
    function hasStoredConsent(bytes32 userId) public view returns(bool){
        Consent storage potentialConsent = userConsent[userId];
        return potentialConsent.owner != address(0);
    }

    // If no consent has been stored before, consent is stored for given user
    function storeConsent(bytes32 userId, uint8[] memory consentLevels) public {
        _createConsent(Consent(msg.sender, now, userId, consentLevels));
    }

    // Stores consent Object in mapping
    function _createConsent(Consent memory consent) private {
        userConsent[consent.userId] = consent;
    }

    // Returns the consent levels stored for the given user
    function getUserConsentLevels(bytes32 userId) public view returns(uint8[] memory) {
        if (!hasStoredConsent(userId)) revert("No consent stored for this user.");
        return userConsent[userId].consentLevels;
    }

    // Returns all consent information for the given user
    function getConsent(bytes32 userId) public view returns(address, uint256, bytes32, uint8[] memory) {
        if (!hasStoredConsent(userId)) revert("No consent stored for this user.");
        Consent memory consent = userConsent[userId];
        return (consent.owner, consent.timestamp, consent.userId, consent.consentLevels);
    }

    // Replaces the consent for the given user with an empty consent.
    function revokeConsent(bytes32 userId) public {
        if (!hasStoredConsent(userId)) revert("No consent stored for this user.");
        uint8[] memory empty;
        _createConsent(Consent(msg.sender, now, userId, empty));
    }
}
