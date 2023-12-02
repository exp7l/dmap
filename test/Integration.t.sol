/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import "forge-std/Test.sol";
import {BaseTest} from "./BaseTest.t.sol";
import {ZoneLike} from "../src/ZoneLike.sol";

contract IntegrationTest is Test, BaseTest {
    function test_SetKey() public {
        // :free:vitalik
        string memory namePlain = "vitalik";
        // salt does not matter because appraisal is 0
        free.assume(bytes32(uint256(426942)), namePlain);
        string memory keyPlain = "primary-wallet";
        bytes memory expectedValue = abi.encode(address(0x696969));

        // IMPORTANT
        bytes32 mapId = _setKey(free, namePlain, keyPlain, expectedValue);

        bytes32 physicalKey = keccak256(abi.encode(mapId, _key(keyPlain)));
        require(keccak256(emap.get(physicalKey)) == keccak256(expectedValue));
    }

    function _setKey(ZoneLike zone, string memory namePlain, string memory keyPlain, bytes memory value)
        internal
        returns (bytes32 mapId)
    {
        bytes32 name = keccak256(abi.encode(namePlain));
        bytes24 key = _key(keyPlain);
        mapId = zone.setKey(name, key, 4, value); // for typ tag, see EmapLike.sol
    }

    function _key(string memory keyPlain) internal pure returns (bytes24 key) {
        key = bytes24(abi.encode(keyPlain));
    }
}
