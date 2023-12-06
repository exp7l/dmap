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
        uint8 addrTyp = 3;
        bytes32 name = keccak256(abi.encode(namePlain));
        freezone.setMap(name);

        ///// set key 1 /////
        string memory keyPlain = "primary-wallet";
        address addy = 0x00000000219ab540356cBB839Cbe05303d7705Fa;
        bytes memory value = abi.encode(addy);
        freezone.setKey(name, _key(keyPlain), addrTyp, value);

        ///// set key 2 /////
        string memory keyPlain2 = "secondary-wallet";
        address addy2 = 0xb20a608c624Ca5003905aA834De7156C68b2E1d0;
        bytes memory value2 = abi.encode(addy2);
        freezone.setKey(name, _key(keyPlain2), addrTyp, value2);

        vm.stopBroadcast();
    }

    function _key(string memory keyPlain) internal pure returns (bytes24 key) {
        // note that non-standarded encoding, we need it because standard encoding is too wide
        // will truncate if the string is longer than 24 bytes
        key = bytes24(abi.encodePacked(keyPlain));
    }
}
