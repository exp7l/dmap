/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

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

// Elastic Map
interface EmapLike {
    event Set(
        address indexed zone, bytes32 indexed mapId, bytes24 indexed key, uint8 indexed typ, bytes value
    ) anonymous;

    function getMapId() external returns (bytes32);

    function getKeys(bytes32 mapId) external view returns (Key[] memory);

    function get(bytes32 physicalKey) external returns (bytes memory value);

    function owners(bytes32 mapId) external returns (address owner);

    function set(bytes32 mapId, bytes24 key, uint8 typ, bytes calldata value) external;
}
