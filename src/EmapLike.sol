/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

// Elastic Map
interface EmapLike {
    struct Key {
        // type tag convention:
        // 1. bool
        // 2. uint256
        // 3. int256
        // 4. address
        // 5. bytes32
        // 6. bytes
        // 7. string
        uint8 typ;
        bytes24 key;
        bytes32 mapId;
        bytes32 physicalKey;
    }

    event Set(
        address indexed zone,
        bytes32 indexed mapId,
        bytes24 indexed logicalKey,
        bytes32 indexed physicalKey,
        bytes value,
        bytes32 nonce,
        uint8 typ
    ) anonymous;

    function get(bytes32 physicalKey) external returns (bytes memory value);

    function getNonce() external returns (bytes32 n);

    function set(bytes32 nonce, bytes24 logicalKey, uint8 typ, bytes calldata value)
        external
        returns (bytes32 mapId, bytes32 physicalKey);
}
