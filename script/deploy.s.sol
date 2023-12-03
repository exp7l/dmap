// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import "forge-std/Script.sol";
import {Setup} from "../util/Setup.sol";

contract Deployer is Script, Setup {
    function run() external {
        string memory m = vm.envString("SEEDPHRASE");
        uint256 sk = vm.deriveKey(m, 0);
        address gov = vm.envString("GOV");
        vm.startBroadcast(sk);
        _setUp(gov);
        vm.stopBroadcast();
    }
}
