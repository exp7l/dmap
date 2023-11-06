/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

interface AppraiserLike {
    function appraise(string calldata plain) external view returns (uint256);
}
