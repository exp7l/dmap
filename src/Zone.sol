/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {DmapLike} from "./DmapLike.sol";
import {EmapLike} from "./EmapLike.sol";
import {ZoneLike} from "./ZoneLike.sol";
import {AppraiserLike} from "./AppraiserLike.sol";

contract Zone is ZoneLike {
    uint256 public constant LOCK = 1;
    // if FREQ was too low, zone buyer could be too late and frontrunned upon assuming a name ownership
    uint256 public immutable FREQ;
    address public immutable EMAP;
    // RootZone and Dmap have circular dependency
    // so let DMAP be configured only once and de facto immutable
    address public DMAP;
    address public appraiser;
    address public gov;
    uint256 public lastCommitment;
    mapping(bytes32 => address) public owners;
    mapping(bytes32 => uint256) public commitments;
    mapping(uint256 => bool) public abdicated;
    string name;

    constructor(string memory _name, address _gov, address _appraiser, address _emap, uint256 _freq) {
        name = _name;
        gov = _gov;
        appraiser = _appraiser;
        EMAP = _emap;
        FREQ = _freq;
    }

    function commit(bytes32 comm) external payable {
        require(block.timestamp >= lastCommitment + FREQ, "ERR_PENDING");
        commitments[comm] = msg.value;
        lastCommitment = block.timestamp;
        // Governance cannot DOS via this payment
        payable(gov).call{value: msg.value}("");
        emit Commit(comm, msg.value);
    }

    function assume(bytes32 salt, string calldata plain) external {
        bytes32 name = keccak256(abi.encode(plain));
        require(owners[name] == address(0), "ERR_TAKEN");
        // abi.encodePacked is ambiguous so there is no decoding function!
        // https://docs.soliditylang.org/en/v0.8.13/abi-spec.html?highlight=abi.encodePacked#non-standard-packed-mode
        bytes32 comm = keccak256(abi.encode(salt, name, msg.sender));
        // appraiser can be used by gov to price names and even revert upon new name registrations
        uint256 appraisal = AppraiserLike(appraiser).appraise(plain, abi.encode(msg.sender));
        require(commitments[comm] >= appraisal, "ERR_PAYMENT");
        commitments[comm] = 0;
        owners[name] = msg.sender;
        emit Assume(name, plain);
    }

    function transfer(bytes32 name, address recipient) external {
        require(owners[name] == msg.sender, "ERR_TAKEN");
        owners[name] = recipient;
        emit Transfer(msg.sender, recipient, name);
    }

    function set(bytes32 name, bytes32 meta, bytes32 data) public {
        require(owners[name] == msg.sender, "ERR_OWNER");
        DmapLike(DMAP).set(name, meta, data);
    }

    function setKey(bytes32 name, bytes24 key, uint8 typ, bytes calldata value) external returns (bytes32 mapId) {
        require(owners[name] == msg.sender, "ERR_OWNER");
        bytes32 slot = keccak256(abi.encode(address(this), name));
        bytes32 meta;
        (meta, mapId) = DmapLike(DMAP).get(slot);
        require(uint256(meta) & 1 != LOCK, "ERR_LOCKED");
        // set Dmap upon empty nonce
        if (mapId == bytes32(0)) {
            mapId = EmapLike(EMAP).getMapId();
            set(name, meta, mapId);
        }
        EmapLike(EMAP).set(mapId, key, typ, value);
    }

    function abdicate(uint256 param) external {
        require(msg.sender == gov, "ERR_GOV");
        abdicated[param] = true;
        emit Abdicate(param);
    }

    function configure(uint256 param, address addr) external {
        require(msg.sender == gov, "ERR_GOV");
        require(!abdicated[param], "ERR_ABDICATED");
        // only appraiser is configurable not counting gov itself
        // keep this contract object credibly neutral
        if (param == 1) {
            gov = addr;
        } else if (param == 2) {
            appraiser = addr;
        } else if (param == 3 && DMAP == address(0)) {
            DMAP = addr;
        } else {
            revert("ERR_PARAM");
        }
        emit Configure(param, addr);
    }
}
