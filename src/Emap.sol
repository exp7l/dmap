/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {EmapLike} from "./EmapLike.sol";

contract Emap is EmapLike {
    uint256 objNonce;
    mapping(bytes32 => bytes) public get;
    mapping(bytes32 => Key[]) public getKeys;

    function getNonce() external returns (bytes32 n) {
        n = keccak256(abi.encodePacked(objNonce++, block.chainid));
    }

    function set(bytes32 nonce, bytes24 key, uint8 typ, bytes calldata value)
        external
        returns (bytes32 mapId, bytes32 physicalKey)
    {
        mapId = keccak256(abi.encodePacked(msg.sender, nonce));
        physicalKey = keccak256(abi.encodePacked(mapId, key));
        get[physicalKey] = value;
        getKeys[mapId].push(Key({typ: typ, key: key, mapId: mapId, physicalKey: physicalKey}));
        emit Set(msg.sender, mapId, key, physicalKey, value, nonce, typ);
    }
}
