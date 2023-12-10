/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

interface ZoneLike {
    event Commit(bytes32 indexed comm, uint256 indexed value);
    event Assume(address indexed sender, bytes32 indexed name, string plain);
    event Transfer(address indexed owner, address indexed recipient, bytes32 indexed name);
    event Abdicate(address indexed gov, uint256 indexed param);
    event Configure(address indexed gov, uint256 indexed param, address indexed data);
    event Set(address indexed sender, bytes32 indexed name, bytes32 indexed meta, bytes32 data);
    event RemoveKey(address indexed sender, bytes32 indexed name, bytes24 key);
    event SetMap(address indexed sender, bytes32 indexed name);
    event SetKey(address indexed sender, bytes32 indexed name); 

    function commit(bytes32 comm) external payable;

    function assume(bytes32 salt, string calldata plain) external;

    function transfer(bytes32 name, address recipient) external;

    function set(bytes32 name, bytes32 meta, bytes32 data) external;

    function setMap(bytes32 name) external;

    function setKey(bytes32 name, bytes24 key, uint8 typ, bytes calldata value) external returns (bytes32 mapId);

    function removeKey(bytes32 name, bytes24 key) external;

    function abdicate(uint256 param) external;

    function configure(uint256 param, address addr) external;

    function owners(bytes32 name) external view returns (address owner);
}
