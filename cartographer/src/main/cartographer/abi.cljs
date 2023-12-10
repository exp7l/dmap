(ns cartographer.abi)

(defonce emap-abi
  (clj->js
   [{"type" "function"
     "name" "getKeys"
     "inputs" [{"name" "mapId"
                "type" "bytes32"
                "internalType" "bytes32"}]
     "outputs" [{"name" ""
                 "type" "tuple[]"
                 "internalType" "struct Key[]"
                 "components" [{"name" "typ"
                                "type" "uint8"
                                "internalType" "uint8"}
                               {"name" "key"
                                "type" "bytes24"
                                "internalType" "bytes24"}
                               {"name" "mapId"
                                "type" "bytes32"
                                "internalType" "bytes32"}]}]
     "stateMutability" "view"}
    {"type" "function"
     "name" "get"
     "inputs" [{"name" "physicalKey"
                "type" "bytes32"
                "internalType" "bytes32"}]
     "outputs" [{"name" "value"
                 "type" "bytes"
                 "internalType" "bytes"}]
     "stateMutability" "view"}
    {"type" "function"
     "name" "remove"
     "inputs" [{"name" "mapId"
                "type" "bytes32"
                "internalType" "bytes32"}
               {"name" "key"
                "type" "bytes24"
                "internalType" "bytes24"}]
     "outputs" []
     "stateMutability" "nonpayable"}]))

(defonce zone-abi
  (clj->js
   [{;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     ;; the client support using each of these functions seperately
     "type" "function"
     "name" "assume"
     "inputs" [{"name" "salt" "type" "bytes32" "internalType" "bytes32"}
               {"name" "plain" "type" "string" "internalType" "string"}]
     "outputs" []
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name" "commit"
     "inputs" [{"name" "comm" "type" "bytes32" "internalType" "bytes32"}]
     "outputs" []
     "stateMutability" "payable"}
    {"type" "function"
     "name" "set"
     "inputs" [{"name" "name" "type" "bytes32" "internalType" "bytes32"}
               {"name" "meta" "type" "bytes32" "internalType" "bytes32"}
               {"name" "data" "type" "bytes32" "internalType" "bytes32"}]
     "outputs" []
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name" "removeKey"
     "inputs" [{"name" "name" "type" "bytes32" "internalType" "bytes32"}
               {"name" "key" "type" "bytes24" "internalType" "bytes24"}]
     "outputs" []
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name" "setMap"
     "inputs" [{"name" "name" "type" "bytes32" "internalType" "bytes32"}]
     "outputs" []
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name" "setKey"
     "inputs" [{"name" "name" "type" "bytes32" "internalType" "bytes32"}
               {"name" "key" "type" "bytes24" "internalType" "bytes24"}
               {"name" "typ" "type" "uint8" "internalType" "uint8"}
               {"name" "value" "type" "bytes" "internalType" "bytes"}]
     "outputs" [{"name" "mapId" "type" "bytes32" "internalType" "bytes32"}]
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name" "transfer"
     "inputs" [{"name" "name" "type" "bytes32" "internalType" "bytes32"}
               {"name" "recipient" "type" "address" "internalType" "address"}]
     "outputs" []
     "stateMutability" "nonpayable"}
    {"type" "function"
     "name""owners"
     "inputs" [{"name" "" "type" "bytes32" "internalType" "bytes32"}]
     "outputs" [{"name" "" "type" "address" "internalType" "address"}]
     "stateMutability" "view"}]))