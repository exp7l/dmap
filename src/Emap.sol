/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {EmapLike, Key} from "./EmapLike.sol";

contract Emap is EmapLike {
    uint256 nonce;
    mapping(bytes32 => bytes) public get;
    mapping(bytes32 => Key[]) public getKey;

    function getMapId() external returns (bytes32) {
        return keccak256(abi.encode(msg.sender, block.chainid, nonce++));
    }

    function set(bytes32 mapId, bytes24 key, uint8 typ, bytes calldata value) external {
        get[keccak256(abi.encode(mapId, key))] = value;
        getKey[mapId].push(Key({mapId: mapId, key: key, typ: typ}));
        emit Set(msg.sender, mapId, key, typ, value);
    }

    function getKeys(bytes32 mapId) external view returns (Key[] memory) {
        return getKey[mapId];
    }
}
