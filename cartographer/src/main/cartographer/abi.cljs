(ns cartographer.abi)

(defonce emap-abi (clj->js
                   [{"type" "function",
                     "name" "getKeys",
                     "inputs" [{"name" "mapId",
                                "type" "bytes32",
                                "internalType" "bytes32"}],
                     "outputs" [{"name" "",
                                 "type" "tuple[]",
                                 "internalType" "struct Key[]",
                                 "components" [{"name" "typ",
                                                "type" "uint8",
                                                "internalType" "uint8"},
                                               {"name" "key",
                                                "type" "bytes24",
                                                "internalType" "bytes24"},
                                               {"name" "mapId",
                                                "type" "bytes32",
                                                "internalType" "bytes32"}]}],
                     "stateMutability" "view"}
                    {"type" "function",
                     "name" "get",
                     "inputs" [{"name" "physicalKey",
                                "type" "bytes32",
                                "internalType" "bytes32"}],
                     "outputs" [{"name" "value",
                                 "type" "bytes",
                                 "internalType" "bytes"}],
                     "stateMutability" "view"}]))