/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

// Elastic Map
interface EmapLike {
    struct LogicalKey {
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
    }

    event Set(address indexed zone, bytes32 indexed nonce, bytes24 indexed logicalKey, bytes value);

    function getNonce() external returns (bytes32 n);

    function set(bytes32 nonce, bytes24 logicalKey, uint8 typ, bytes calldata value) external;
}
