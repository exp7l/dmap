// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import "forge-std/Script.sol";
import {Setup} from "../util/Setup.sol";

contract Deployer is Script, Setup {
    address gov;
    uint256 sk;
    
    function run() public virtual {
        string memory m = vm.envString("SEEDPHRASE");
        sk = vm.deriveKey(m, 0);
        gov = vm.envAddress("GOV");
        vm.startBroadcast(sk);
        _setUp(gov);
        vm.stopBroadcast();
    }
}
