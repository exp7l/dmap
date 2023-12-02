pragma solidity 0.8.13;

import "forge-std/Test.sol";
import {AppraiserLike} from "../src/AppraiserLike.sol";
import {AppraiserFree} from "../src/AppraiserFree.sol";
import {AppraiserRoot} from "../src/AppraiserRoot.sol";
import {DmapLike} from "../src/DmapLike.sol";
import {Dmap} from "../src/Dmap.sol";
import {EmapLike} from "../src/EmapLike.sol";
import {Emap} from "../src/Emap.sol";
import {ZoneLike} from "../src/ZoneLike.sol";
import {Zone} from "../src/Zone.sol";

contract IntegrationTest is Test {
    uint256 testNumber;
    AppraiserLike appraiserFree;
    AppraiserLike appraiserRoot;
    DmapLike dmap;
    EmapLike emap;
    ZoneLike root;
    ZoneLike free;

    function setUp() public {
        appraiserFree = AppraiserLike(address(new AppraiserFree()));
        appraiserRoot = AppraiserLike(address(new AppraiserRoot()));
        emap = EmapLike(address(new Emap()));
        root = ZoneLike(address(new Zone(address(this), address(appraiserRoot), address(emap), 120)));
        dmap = DmapLike(address(new Dmap(address(root))));
        root.configure(3, address(dmap));
        free = ZoneLike(address(new Zone(address(this), address(appraiserFree), address(emap), 120)));
        free.configure(3, address(dmap));
        // set (and lock) the free zone under the root zone
        string memory plain = "free";
        bytes32 name = keccak256(abi.encodePacked(plain));
        root.assume(bytes32(uint256(42)), plain);
        root.set(name, bytes32(uint256(1)), bytes32(bytes20(address(free))));
    }

    function test_SetKey() public {
        string memory plain = "vitalik"; // :free:vitalik
        bytes32 name = keccak256(abi.encodePacked(plain));
        free.assume(bytes32(uint256(426942)), plain); // salt does not matter because appraisal is 0
        string memory keyPlain = "primary-wallet";
        bytes24 key = bytes24(abi.encodePacked(keyPlain));
        // @audit packed or not?
        // packed data: 0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000140000000000000000000000000000000000696969000000000000000000000000
        //    abi data: 0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000696969
        bytes memory expectedValue = abi.encode(address(0x696969));
        (bytes32 nonce, bytes32 mapId, bytes32 physicalKey) = free.setKey(name, key, 4, expectedValue); // for typ tag, see EmapLike.sol
        require(keccak256(abi.encodePacked(mapId, key)) == physicalKey);
        require(keccak256(emap.get(physicalKey)) == keccak256(expectedValue));
    }
}
