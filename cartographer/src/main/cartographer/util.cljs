(ns cartographer.util
  (:require ["./dmap.js" :as dmap]))

(defn test-bit [meta-bn i]
  (-> meta-bn
      (.mask (inc i))
      (.toNumber)
      (bit-test i)))

(defn extract-emap [meta-bn]
  (.toHexString (.shr meta-bn 8)))

(defn decode-utf8 [s]
  (let [decoder (js/TextDecoder. "utf-8")]
    (->> s
         (dmap/_hexToArrayBuffer)
         (filter #(not= 0 %))
         (js/Uint8Array.)
         (.decode decoder))))

(defn parsed-meta [meta]
  ;; standard #1: the least significant bit denotes whether the slot is lock
  ;; standard #2: the 2nd least significant bit denotes whether `data` is a reference
  ;; standard #3: the most significant 31 bytes for a leaf gives the Emap address to resolve the map ID, if `data` is a map ID
  (-> {}
      (assoc :lock? (test-bit meta 0))
      (assoc :map? (test-bit meta 1))
      (assoc :emap (extract-emap meta))))

(def typ-conventions {0 "bool"
                      1 "uint256"
                      2 "int256"
                      3 "address"
                      4 "bytes32"
                      5 "bytes"
                      6 "string"})

(def typ-number->typ-annotation
  {"bool" 0
   "uint256" 1
   "int256" 2
   "address" 3
   "bytes32" 4
   "bytes" 5
   "string" 6})

(defn decode-registry [trace]
  (let [len (count trace)
        idx (- len 2)
        pair (nth trace idx)
        registry (second pair)
        decoded (subs registry 0 (+ 2 (* 20 2)))]
    decoded))

(defn dpath->name [dpath]
  (-> dpath
      (dmap/parse)
      (js->clj)
      (last)
      (get "name")
      (#(dmap/abiEncode
         #js ["string"]
         #js [%]))
      (dmap/keccak256)))
