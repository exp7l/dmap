/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {EmapLike} from "./EmapLike.sol";

contract Emap is EmapLike {
    uint256 objNonce;
    mapping(bytes32 => bytes) public get;
    mapping(bytes32 => LogicalKey[]) public getKeys;

    function getNonce() external returns (uint256 n) {
        n = ++objNonce;
    }

    function set(uint256 nonce, bytes24 key, uint8 typ, bytes calldata value) external {
        bytes32 mapId = keccak256(abi.encodePacked(msg.sender, nonce));
        bytes32 physicalKey = keccak256(abi.encodePacked(mapId, key));
        get[physicalKey] = value;
        getKeys[mapId].push(LogicalKey({typ: typ, key: key}));
        emit Set(msg.sender, nonce, key, value);
    }
}
