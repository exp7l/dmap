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
    }

    event Set(
        address indexed zone, bytes32 indexed mapId, bytes24 indexed key, uint8 indexed typ, bytes value
    ) anonymous;

    function getMapId() external returns (bytes32);

    function get(bytes32 physicalKey) external returns (bytes memory value);

    function set(bytes32 mapId, bytes24 key, uint8 typ, bytes calldata value) external;
}
