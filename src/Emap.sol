/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {EmapLike, Key} from "./EmapLike.sol";

contract Emap is EmapLike {
    uint256 nonce;
    mapping(bytes32 => bytes) public get;
    mapping(bytes32 => Key[]) public getKey;
    mapping(bytes32 => address) public owners;
    mapping(uint256 => string) public typeAnnotations;

    constructor() {
        typeAnnotations[1] = "bool";
        typeAnnotations[2] = "uint256";
        typeAnnotations[3] = "int256";
        typeAnnotations[4] = "address";
        typeAnnotations[5] = "bytes32";
        typeAnnotations[6] = "bytes";
        typeAnnotations[7] = "string";
    }

    function getMapId() external returns (bytes32 mapId) {
        mapId = keccak256(abi.encode(msg.sender, block.chainid, nonce++));
        owners[mapId] = msg.sender;
    }

    function set(bytes32 mapId, bytes24 key, uint8 typ, bytes calldata value) external {
        require(msg.sender == owners[mapId], "ERR_OWNER");
        get[keccak256(abi.encode(mapId, key))] = value;
        getKey[mapId].push(Key({mapId: mapId, key: key, typ: typ}));
        emit Set(msg.sender, mapId, key, typ, value);
    }

    function getKeys(bytes32 mapId) external view returns (Key[] memory) {
        return getKey[mapId];
    }
}
