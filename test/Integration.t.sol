/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import "forge-std/Test.sol";
import {Setup, Key} from "../util/Setup.sol";
import {ZoneLike} from "../src/ZoneLike.sol";

contract IntegrationTest is Test, Setup {
    function setUp() public {
        _setUp(address(this));
    }

    function test_SetKey() public {
        ///// definitions /////
        // :free:vitalik
        string memory namePlain = "vitalik";
        // salt does not matter because appraisal is 0
        freezone.assume(bytes32(uint256(426942)), namePlain);
        string memory keyPlain = "primary-wallet";
        bytes memory expectedValue = abi.encode("abc");
        bytes32 name = keccak256(abi.encode(namePlain));
        uint8 typ = 7;

        ///// free.set 1 /////
        freezone.setMap(name);
        bytes32 mapId = freezone.setKey(name, _key(keyPlain), typ, expectedValue);
        bytes32 physicalKey = keccak256(abi.encode(mapId, _key(keyPlain)));
        require(keccak256(emap.get(physicalKey)) == keccak256(expectedValue));
        Key[] memory keys = emap.getKeys(mapId);
        require(keys.length == 1);
        require(keys[0].mapId == mapId);
        require(keys[0].typ == typ);
        require(keys[0].key == _key(keyPlain));
        bytes32 slot = keccak256(abi.encode(address(freezone), name));
        (bytes32 meta, bytes32 data) = dmap.get(slot);
        require(data == mapId);

        ///// free.set 2 /////
        bytes memory expectedValue2 = abi.encode("bcd");
        freezone.setKey(name, _key(keyPlain), typ, expectedValue2);
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
        freezone.set(name, LOCK, mapId);

        ///// revert on set after lock /////
        vm.expectRevert(bytes("ERR_LOCKED"));
        freezone.setKey(name, _key(keyPlain), typ, expectedValue2);
    }

    function test_setKeyOwnership() external {
        ///// definitions /////
        string memory namePlain = "vitalik";
        freezone.assume(bytes32(uint256(426942)), namePlain);
        string memory keyPlain = "primary-wallet";
        bytes memory expectedValue = abi.encode("abc");
        uint8 typ = 7;
        bytes32 name = keccak256(abi.encode(namePlain));
        freezone.setMap(name);
        bytes32 mapId = freezone.setKey(name, _key(keyPlain), typ, expectedValue);

        ///// only registry is allowed to set emap /////
        require(emap.owner(mapId) == address(freezone));

        vm.expectRevert(bytes("ERR_OWNER"));
        emap.set(mapId, _key(keyPlain), typ, expectedValue);

        vm.prank(address(freezone));
        emap.set(mapId, _key(keyPlain), typ, expectedValue);
    }

    function _key(string memory keyPlain) internal pure returns (bytes24 key) {
        // note that non-standarded encoding! will truncate if the string is longer than 24 bytes
        key = bytes24(abi.encodePacked(keyPlain));
    }
}
