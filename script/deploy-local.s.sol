// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {Setup} from "../util/Setup.sol";
import {Deployer} from "./deploy.s.sol";
import {ZoneLike} from "../src/ZoneLike.sol";

contract DeployerLocal is Deployer {
    function run() public override {
        super.run();
        
        vm.startBroadcast();

        string memory namePlain = "vitalik";
        // salt does not matter because appraisal is 0
        freezone.assume(bytes32(uint256(426942)), namePlain);
        string memory keyPlain = "primary-wallet";
        bytes memory expectedValue = abi.encode("abc");
        uint8 typ = 7;

        ///// free.set 1 /////
        _setKey(freezone, namePlain, keyPlain, typ, expectedValue);

        vm.stopBroadcast();
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