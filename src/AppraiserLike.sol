/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

interface AppraiserLike {
    // context is for passing arbitrary form of data
    function appraise(string calldata plain, bytes calldata context) external view returns (uint256);
}
