(ns cartographer.app
  (:require [reagent.core :as r]
            [reagent.dom  :as dom]
            [shadow.cljs.modern :refer (js-await)]
            [cartographer.abi :as abi]
            [cartographer.util :as util]
            [shadow.css :refer (css)]
            ["./dmap.js" :as dmap]
            ["./dmain.js" :as dmain]
            ["@ethersproject/bignumber" :refer (BigNumber)]
            ["@ethersproject/abi" :refer (defaultAbiCoder)]
            ["ethers" :as ethers]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; state

(defonce rpc "http://127.0.0.1:8545")
(defonce leaf (r/atom nil))
(defonce leaf-kvs (r/atom nil))
(defonce trace (r/atom nil))
;; default provider gives redundant providers
;; https://docs.ethers.org/v5/api/providers/#providers-getDefaultProvider 
(defonce provider (r/atom (.getDefaultProvider ethers rpc)))
(defonce signer (r/atom nil))
(defonce wallet-address (r/atom nil))

;; load from persistent state to memory
(defn init []

  (if (some? (.-ethereum js/window))
    ;; only web3 provider can connect to the provider injected into browser
    (let [web3-provider (-> ethers
                            (.-providers)
                            (.-Web3Provider)
                            (new (.-ethereum js/window)))
          _signer (.getSigner web3-provider)]
      #_{:clj-kondo/ignore [:unresolved-symbol]}
      (js-await [_wallet-address (.getAddress _signer)]
                (reset! signer _signer)
                (reset! wallet-address _wallet-address))))
  (println "init"))

(defn emap-obj
  ([] (emap-obj
       (->> @leaf
            (:meta)
            (util/parsed-meta)
            (:emap))))
  ([addr] (ethers/Contract. addr abi/emap-abi @provider)))

(defn zone-obj [addr]
  (ethers/Contract. addr abi/zone-abi @signer))

(defn fetch-kvs [emap-obj map-id key typ]
  (let [encoded (dmap/abiEncode #js ["bytes32" "bytes24"] #js [map-id key])
        physical-key (dmap/keccak256 encoded)]
    (js-await [bytes (.get emap-obj physical-key)]
              (swap! leaf-kvs #(assoc % [map-id key typ] bytes)))))

(defn fetch-keys [emap-addr map-id is-leaf-map?]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (if is-leaf-map?
    (let [emap-obj (emap-obj emap-addr)]
      (js-await [ks (.getKeys emap-obj map-id)]
                (doseq [key (js->clj ks)]
                  (fetch-kvs emap-obj map-id (second key) (first key)))))
    (swap! leaf-kvs (constantly nil))))

(defn fetch-name [rpc dpath]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (js-await [[facade _] (dmain/getFacade rpc)]
            (js-await [_trace (dmap/walk facade dpath)]
                      (let [_trace (js->clj _trace)
                            meta-bn (.from BigNumber (first (last _trace)))
                            data-bn (.from BigNumber (second (last _trace)))]
                        (swap! trace (constantly _trace))
                        (swap! leaf (constantly {:meta meta-bn
                                                 :data data-bn}))
                        (let [data (.toHexString data-bn)
                              parsed-meta (util/parsed-meta meta-bn)]
                          (fetch-keys (:emap parsed-meta) data (:map? parsed-meta))))
                      (catch err (do
                                   (println "err: " (str err))
                                   (js/alert "error: see console"))))))

;;;; state
;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; onclick

(defn new-entry-onclick []
  (let [new-key (-> (.getElementById js/document "newkey")
                    (.-value)
                    (#(.solidityPack ethers/utils #js ["string"] #js [%]))
                    (#(dmap/hexZeroPad % 24)))
        new-typ (-> (.getElementById js/document "newtyp")
                    (.-value))
        new-val (-> (.getElementById js/document "newval")
                    (.-value)
                    (#(cond (not= new-typ "bool") (identity %)
                            (= % "false") false
                            (= % "true") true
                            :else (throw (ExceptionInfo. "ERR_BOOL"))))
                    (#(dmap/abiEncode #js [new-typ] #js [%])))
        dpath (-> (js/document.getElementById "dpath")
                  (.-value)
                  (dmap/parse)
                  (js->clj))
        name-plain (-> dpath
                       (last)
                       (get "name"))
        name (-> (dmap/abiEncode
                  #js ["string"]
                  #js [name-plain])
                 (dmap/keccak256))
        new-typnum (util/typ-number->typ-annotation new-typ)]
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (js-await [_ (.setKey
                  (zone-obj (util/decode-registry @trace))
                  name
                  new-key
                  new-typnum
                  new-val)]
              (println "async call done"))))

(defn remove-entry-onclick [key-uuid]
  #(let [key-node (js/document.getElementById key-uuid)
         dpath (-> (js/document.getElementById "dpath")
                   (.-value)
                   (dmap/parse)
                   (js->clj))
         name-plain (-> dpath
                        (last)
                        (get "name"))
         name (-> (dmap/abiEncode
                   #js ["string"]
                   #js [name-plain])
                  (dmap/keccak256))
         registry (.getAttribute key-node "registry")
         key (.getAttribute key-node "keyinmap")]
     (println "debug: " [registry name key])
     (.removeKey (zone-obj registry) name key)))

(defn connect-onclick []
  #_{:clj-kondo/ignore [:redundant-do]}
  (do (println "ConnectBtn: connect onclick!")
      #_{:clj-kondo/ignore [:missing-else-branch]}
      (if (nil? (.-ethereum js/window))
        (js/alert "err: install wallet"))
      #_(swap! provider (fn [_]
                          (-> ethers
                              (.-providers)
                              (.-Web3Provider)
                              (new (.-ethereum js/window) "any"))))
      #_{:clj-kondo/ignore [:unresolved-symbol]}
      (js-await [_ (.send @provider "eth_requestAccounts" [])]
                #_{:clj-kondo/ignore [:unresolved-symbol]}
                (js-await [wallet (.getAddress (.getSigner @provider))]
                          (swap! signer (constantly (.getSigner @provider)))
                          (swap! wallet-address (constantly wallet))))
      (println "ConnectBtn: wallet connected!")))

;;;; onclick
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; view

(def $h1 (css {:font-size "2em" :font-weight "bold"}))
(def $h2 (css {:font-size "1.5em"
               :font-weight "bold"
               :margin-top "1em"
               :margin-bottom "0.5em"}))
(def $h3 (css {:font-size "1.25em"
               :font-weight "bold"
               :margin-top "1em"
               :margin-bottom "0.5em"}))
(def $input-btn (css {:text-align "center"
                      :padding-block "1px"
                      :padding-inline "6px"
                      :border-color "black"
                      :border-width "thin"
                      :margin-left "1em"}))
(def $input-text (css {:padding-inline "6px"
                       :border-width "medium"
                       :border-color "black"
                       :border-radius "5px"
                       :font-size "1.5em"
                       :width "20em"
                       :height "2em"}))
(def $section (css {:font-family "monospace" :margin "0em 1.5em"}))

(defn Trace []
  [:div
   [:div {:class $h2} "resolution trace"]
   (into [:div {:class (css {:margin-bottom "2em"})}]
         #_{:clj-kondo/ignore [:missing-else-branch]}
         (if (some? @trace)
           (->> @trace
                (map
                 #(vector
                   :div "step"
                   [:div
                    {:class (css {:margin-left "1em"})}
                    "meta "
                    (first %)]
                   [:div
                    {:class (css {:margin-left "1em"})}
                    "data "
                    (second %)])))))
   [:div "dmap object address " dmap/address]
   [:div "rpc " rpc]])

(defn Dpath []
  [:div
   [:div {:class $h2} "dpath"]
   [:input {:type "text"
            :id "dpath"
            :placeholder ":dmap:free.vitalik"
            :class $input-text}]
   [:button
    {:class $input-btn
     :onClick #(fetch-name
                rpc
                (-> js/document
                    (.getElementById "dpath")
                    (.-value)))}
    "get data"]])

(defn Meta []
  [:div
   [:div {:class $h2} "meta"]
   (if (some? @leaf)
     (let [parsed (util/parsed-meta (:meta @leaf))]
       [:div
        [:div "lock " (str (:lock? parsed))]
        [:div "map " (str (:map? parsed))]
        [:div "emap address " (str (:emap parsed))]])
     nil)])

(defn Dmap []
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (some? @leaf)
    [:div
     [:div
      [:div
       [:label {:class $h3} (.toHexString (:data @leaf))]
       [:button {:class $input-btn} "set name"]
       [:button {:class $input-btn} "set name to new map"]
       [:button {:class $input-btn} "lock name"]]
      (if (-> (:meta @leaf)
              (util/parsed-meta)
              (:map?))
        [:div "^^^ this is a map id, its entries below"])]]))

(defn KeyValue [kv]
  (let [[[map-id key typ] value] kv
        decoded-key (util/decode-utf8 key)
        decoded-value (->> value
                           (.decode defaultAbiCoder #js [(util/typ-conventions typ)])
                           (first)
                           (str))
        key-uuid (random-uuid)
        value-uuid (random-uuid)]
    [:div
     [:div
      [:label {:class $h3}
       "\""
       [:label {:id key-uuid
                :mapid map-id
                :keyinmap key
                :registry (util/decode-registry @trace)}
        decoded-key]
       "\""
       " "
       "\"" [:label {:id value-uuid :value value}  decoded-value] "\""]
      [:button {:class $input-btn :onClick (remove-entry-onclick key-uuid)} "remove entry"]
      [:button {:class $input-btn} "set value"]]]))

(defn KeyValues [kvs]
  [:div
   (into [:div]
         (map #(vector
                :div
                {:class (css {:margin-top "0.75em"})}
                (KeyValue %)))
         kvs)
   [:div {:class (css {:margin-top "2em"})}
    [:input {:id "newkey"
             :type "text"
             :placeholder "my key"
             :class (css {:margin-left "0.25em"
                          :padding-inline "0.5em"
                          :border-width "medium"
                          :border-color "black"
                          :border-radius "0.2em"})}]
    [:select
     {:id "newtyp" :class (css {:margin-left "0.5em"})}
     [:option "bool"]
     [:option "uint256"]
     [:option "int256"]
     [:option "address"]
     [:option "bytes32"]
     [:option "bytes"]
     [:option "string"]]
    [:input {:id "newval"
             :type "text"
             :placeholder "my value"
             :class (css {:margin-left "0.25em"
                          :padding-inline "0.5em"
                          :border-width "medium"
                          :border-color "black"
                          :border-radius "0.2em"
                          :width "25em"})}]]
   [:div [:button {:class (css {:margin-left "0.25em"
                                :margin-top "0.25em"
                                :text-align "center"
                                :padding-block "1px"
                                :padding-inline "6px"
                                :border-color "black"
                                :border-width "thin"})
                   :onClick new-entry-onclick}
          "add new entry"]]])

(defn Emap []
  (if (and (some? @leaf)
           (-> (:meta @leaf)
               (util/parsed-meta)
               (:map?)))
    [:div {:class (css {:margin-bottom "1em" :margin-top "1em"})}
     [:label {:class $h3} "{"]
     [KeyValues @leaf-kvs]
     [:label {:class $h3} "}"]]
    nil))

(defn Data []
  [:div
   [:div {:class $h2} "data"]
   [Dmap]
   [Emap]])

(defn ConnectBtn []
  [:button
   {:class (css {:margin-left "3em"
                 :text-align "center"
                 :padding-block "1px"
                 :padding-inline "6px"
                 :border-color "black"
                 :border-width "thin"})
    :onClick connect-onclick}
   (if (some? @signer)
     (str "connected to " (subs @wallet-address 0 10) "...")
     "connect")])

(defn Application []
  [:div {:class $section}
   [:div [:label {:class $h1} "dmap"] [ConnectBtn]]
   [Dpath]
   [Data]
   [Meta]
   [Trace]])

(dom/render [Application] (js/document.getElementById "app"))

;;;; view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;