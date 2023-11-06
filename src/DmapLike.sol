/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

interface DmapLike {
    error LOCKED();

    event Set(address indexed zone, bytes32 indexed name, bytes32 indexed meta, bytes32 indexed data) anonymous;

    function set(bytes32 name, bytes32 meta, bytes32 data) external;

    function get(bytes32 slot) external view returns (bytes32 meta, bytes32 data);
}
