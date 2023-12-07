/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

import {AppraiserLike} from "../src/AppraiserLike.sol";
import {AppraiserFree} from "../src/AppraiserFree.sol";
import {AppraiserRoot} from "../src/AppraiserRoot.sol";
import {DmapLike} from "../src/DmapLike.sol";
import {Dmap} from "../src/Dmap.sol";
import {EmapLike, Key} from "../src/EmapLike.sol";
import {Emap} from "../src/Emap.sol";
import {ZoneLike} from "../src/ZoneLike.sol";
import {Zone} from "../src/Zone.sol";

contract Setup {
    AppraiserLike appraiserFree;
    AppraiserLike appraiserRoot;
    DmapLike dmap;
    EmapLike emap;
    ZoneLike rootzone;
    ZoneLike freezone;
    ZoneLike dmapzone;
    bytes32 constant LOCK = bytes32(uint256(1));
    bytes32 constant SALT = bytes32(uint256(42));
    uint256 constant FREQ = 120;
    uint8 DMAP_PARAM = 3;

    function _setUp(address gov) internal {
        bytes32 name;
        string memory plain;
        appraiserFree = AppraiserLike(address(new AppraiserFree()));
        appraiserRoot = AppraiserLike(address(new AppraiserRoot()));

        // deploy emap
        emap = EmapLike(address(new Emap()));

        // deploy root
        rootzone = ZoneLike(address(new Zone("rootzone", gov, address(appraiserRoot), address(emap), FREQ)));

        // deploy dmap
        dmap = DmapLike(address(new Dmap(address(rootzone))));
        rootzone.configure(DMAP_PARAM, address(dmap));

        // deploy TLD dmpzone
        dmapzone = ZoneLike(address(new Zone("dmapzone", gov, address(appraiserRoot), address(emap), FREQ)));
        dmapzone.configure(DMAP_PARAM, address(dmap));

        // set and lock the dmapzone under the rootzone
        plain = "dmap";
        name = keccak256(abi.encode(plain));
        rootzone.assume(SALT, plain);
        // follows dmap object encoding for the rootzone
        rootzone.set(name, LOCK, bytes32(bytes20(address(dmapzone))));

        // deploy freezone
        freezone = ZoneLike(address(new Zone("freezone", gov, address(appraiserFree), address(emap), FREQ)));
        freezone.configure(DMAP_PARAM, address(dmap));

        // set and lock the freezone under the dmpzone
        plain = "free";
        name = keccak256(abi.encode(plain));
        dmapzone.assume(SALT, plain);
        dmapzone.set(name, LOCK, bytes32(bytes20(address(freezone))));
    }
}
