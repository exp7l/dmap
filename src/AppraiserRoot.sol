/// SPDX-License-Identifier: AGPL-3.0
pragma solidity 0.8.13;

contract AppraiserRoot {
    event Yield(address indexed current, address indexed next);

    address gov;

    constructor() {
        gov = msg.sender;
    }

    function appraise(string calldata, bytes calldata context) external view returns (uint256) {
        if (abi.decode(context, (address)) == gov) {
            return 0;
        } else {
            return 100e18;
        }
    }
    
    function yield(address next) external {
        require(msg.sender == gov, "ERR_GOV");
        address _gov = gov;
        gov = next;
        emit Yield(_gov, next);
    }
}
