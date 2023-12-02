/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

interface ZoneLike {
    event Commit(bytes32 indexed comm, uint256 indexed value);
    event Assume(bytes32 indexed name, string plain);
    event Transfer(address indexed owner, address indexed recipient, bytes32 indexed name);
    event Abdicate(uint256 indexed param);
    event Configure(uint256 indexed param, address indexed data);

    function commit(bytes32 comm) external payable;

    function assume(bytes32 salt, string calldata plain) external;

    function transfer(bytes32 name, address recipient) external;

    function set(bytes32 name, bytes32 meta, bytes32 data) external;

    function setKey(bytes32 name, bytes24 key, uint8 typ, bytes calldata value)
        external
        returns (bytes32 nonce, bytes32 mapId, bytes32 phsyicalKey);

    function abdicate(uint256 param) external;

    function configure(uint256 param, address addr) external;
}
