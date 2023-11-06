/// SPDX-License-Identifier: AGPL-3.0

pragma solidity 0.8.13;

contract RootAppraiser {
    address gov;

    constructor() {
        gov = msg.sender;
    }

    function appraise(string calldata) external view returns (uint256) {
        if (msg.sender == gov) {
            return 0;
        } else {
            return 100e18;
        }
    }
}
