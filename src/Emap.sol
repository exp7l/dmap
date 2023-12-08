/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {EmapLike, Key} from "./EmapLike.sol";

// Elastic Map
contract Emap is EmapLike {
    uint256 nonce;
    mapping(bytes32 => bytes) public get;
    mapping(bytes32 => Key[]) public getKey;
    mapping(bytes32 => address) public owner;
    mapping(uint256 => string) public typeConvention;

    constructor() {
        typeConvention[0] = "bool";
        typeConvention[1] = "uint256";
        typeConvention[2] = "int256";
        typeConvention[3] = "address";
        typeConvention[4] = "bytes32";
        typeConvention[5] = "bytes";
        typeConvention[6] = "string";
    }

    function getMapId() external returns (bytes32 mapId) {
        mapId = keccak256(abi.encode(msg.sender, block.chainid, nonce++));
        owner[mapId] = msg.sender;
    }

    function set(
        bytes32 mapId,
        bytes24 key,
        uint8 typ,
        bytes calldata value
    ) external {
        require(msg.sender == owner[mapId], "ERR_OWNER");
        bytes32 physicalKey = keccak256(abi.encode(mapId, key));
         // allows overwrite therefore key writing are first-in-first-out
        if (get[physicalKey].length != 0) {  
            remove(mapId, key);
        }
        getKey[mapId].push(Key({mapId: mapId, key: key, typ: typ}));
        get[physicalKey] = value;
        emit Set(msg.sender, mapId, key, typ, value);
    }

    function remove(bytes32 mapId, bytes24 key) public {
        require(msg.sender == owner[mapId], "ERR_OWNER");
        bytes32 physicalKey = keccak256(abi.encode(mapId, key));
        delete get[physicalKey];
        Key[] storage keys = getKey[mapId];
        for (uint256 i = 0; i < keys.length; ++i) {
            if (keys[i].key == key) {
                keys[i] = keys[keys.length - 1];
                keys.pop();
                return;
            }
        }
        revert("ERR_KEY");
    }

    function getKeys(bytes32 mapId) public view returns (Key[] memory) {
        return getKey[mapId];
    }
}
