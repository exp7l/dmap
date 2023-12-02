/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

contract AppraiserRoot {
    address gov;

    constructor() {
        gov = msg.sender;
    }

    function appraise(string calldata, bytes calldata context) external view returns (uint256) {
        if (abi.decode(context, (address)) == gov) {
            return 0;
        } else {
            return 100e18;
        }
    }
}
