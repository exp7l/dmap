/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {AppraiserLike} from "../src/AppraiserLike.sol";
import {AppraiserFree} from "../src/AppraiserFree.sol";
import {AppraiserRoot} from "../src/AppraiserRoot.sol";
import {DmapLike} from "../src/DmapLike.sol";
import {Dmap} from "../src/Dmap.sol";
import {EmapLike} from "../src/EmapLike.sol";
import {Emap} from "../src/Emap.sol";
import {ZoneLike} from "../src/ZoneLike.sol";
import {Zone} from "../src/Zone.sol";

contract BaseTest {
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

}