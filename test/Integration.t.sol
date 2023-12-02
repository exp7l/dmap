/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import "forge-std/Test.sol";
import {BaseTest, Key} from "./BaseTest.t.sol";
import {ZoneLike} from "../src/ZoneLike.sol";

contract IntegrationTest is Test, BaseTest {
    function test_SetKey() public {
        ///// definitions /////
        // :free:vitalik
        string memory namePlain = "vitalik";
        // salt does not matter because appraisal is 0
        free.assume(bytes32(uint256(426942)), namePlain);
        string memory keyPlain = "primary-wallet";
        bytes memory expectedValue = abi.encode("abc");
        uint8 typ = 7;

        ///// free.set 1 /////
        bytes32 mapId = _setKey(free, namePlain, keyPlain, typ, expectedValue);

        bytes32 physicalKey = keccak256(abi.encode(mapId, _key(keyPlain)));
        require(keccak256(emap.get(physicalKey)) == keccak256(expectedValue));
        Key[] memory keys = emap.getKeys(mapId);
        require(keys.length == 1);
        require(keys[0].mapId == mapId);
        require(keys[0].typ == typ);
        require(keys[0].key == _key(keyPlain));

        bytes32 name = keccak256(abi.encode(namePlain));
        bytes32 slot = keccak256(abi.encode(address(free), name));
        (bytes32 meta, bytes32 data) = dmap.get(slot);
        require(data == mapId);

        ///// free.set 2 /////
        bytes memory expectedValue2 = abi.encode("bcd");
        _setKey(free, namePlain, keyPlain, typ, expectedValue2);
        require(keccak256(emap.get(physicalKey)) == keccak256(expectedValue2));
        keys = emap.getKeys(mapId);
        require(keys.length == 2);
        require(keys[0].mapId == mapId);
        require(keys[0].typ == typ);
        require(keys[0].key == _key(keyPlain));
        require(keys[1].mapId == mapId);
        require(keys[1].typ == typ);
        require(keys[1].key == _key(keyPlain));

        ///// free.set 3 (lock) /////
        bytes32 LOCK = bytes32(uint256(1));
        free.set(name, LOCK, mapId);

        ///// revert on set after lock /////
        vm.expectRevert(bytes("ERR_LOCKED"));
        _setKey(free, namePlain, keyPlain, typ, expectedValue2);
    }

    function _setKey(ZoneLike zone, string memory namePlain, string memory keyPlain, uint8 typ, bytes memory value)
        internal
        returns (bytes32 mapId)
    {
        bytes32 name = keccak256(abi.encode(namePlain));
        bytes24 key = _key(keyPlain);
        mapId = zone.setKey(name, key, typ, value); // for typ tag, see EmapLike.sol
    }

    function _key(string memory keyPlain) internal pure returns (bytes24 key) {
        // note that non-standarded encoding! will truncate if the string is longer than 24 bytes
        key = bytes24(abi.encodePacked(keyPlain));
    }
}
