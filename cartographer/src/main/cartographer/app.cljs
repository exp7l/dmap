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

(defn init [] (println "init"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; state

(defonce rpc "http://127.0.0.1:8545")
(defonce leaf (r/atom nil))
(defonce leaf-kvs (r/atom nil))
(defonce trace (r/atom nil))
(defonce provider (r/atom (.getDefaultProvider ethers rpc)))
(defonce signer (r/atom nil))
(defonce wallet-address (r/atom nil))

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
              (println "debug 1: " typ)
              (swap! leaf-kvs #(assoc % [map-id key typ] bytes)))))

(defn fetch-keys [emap-addr map-id is-leaf-map?]
  #_{:clj-kondo/ignore [:unresolved-symbol]} 
  (if is-leaf-map?
    (let [emap-obj (emap-obj emap-addr)]
      (js-await [ks (.getKeys emap-obj map-id)]
                (println "ks: ..." [emap-obj emap-addr map-id ks])
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

(defn remove-entry [key-uuid]
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

(defn KeyValue [kv]
  (let [[[map-id key typ] value] kv
        decoded-key (util/decode-utf8 key)
        decoded-value (.decode defaultAbiCoder
                               #js [(util/typ-conventions typ)]
                               value)
        key-uuid (random-uuid)
        value-uuid (random-uuid)]
    [:div
     [:div
      [:label
       {:class $h3}
       "\""
       [:label {:id key-uuid
                :mapid map-id
                :keyinmap key
                :registry (util/decode-registry @trace)}
        decoded-key]
       "\""
       ", "
       "\"" [:label {:id value-uuid :value value} decoded-value] "\""]
      [:button {:class $input-btn :onClick (remove-entry key-uuid)} "remove entry"]
      [:button {:class $input-btn} "set value"]]]))

(defn KeyValues [kvs]
  [:div
   [:label {:class $h3} "{"]
   (into [:div]
         (map #(vector
                :div
                {:class (css {:margin-top "0.75em"})}
                (KeyValue %)))
         kvs)
   [:label {:class $h3} "}"]
   [:div {:class (css {:margin-top "2em"})}
    [:button {:class (css {:text-align "center"
                           :padding-block "1px"
                           :padding-inline "6px"
                           :border-color "black"
                           :border-width "thin"})} "add new entry"]]])

(defn Emap []
  (if (some? @leaf-kvs)
    [:div {:class (css {:margin-bottom "1em" :margin-top "1em"})} [KeyValues @leaf-kvs]]
    nil))

(defn Data []
  [:div
   [:div {:class $h2} "data"]
   [Dmap]
   [Emap]])



(defn ConnecBtn []
  [:button {:class (css {:margin-left "3em"
                         :text-align "center"
                         :padding-block "1px"
                         :padding-inline "6px"
                         :border-color "black"
                         :border-width "thin"})
            :onClick #(do (println "connect onclick!")
                          (if (nil? (.-ethereum js/window))
                            (js/alert "err: install wallet"))
                          (swap! provider (fn [_]
                                            (-> ethers
                                                (.-providers)
                                                (.-Web3Provider)
                                                (new (.-ethereum js/window) "any"))))
                          (js-await [_ (.send @provider "eth_requestAccounts" [])]
                                    (js-await [wallet (.getAddress (.getSigner @provider))]
                                              (swap! signer (constantly (.getSigner @provider)))
                                              (swap! wallet-address (constantly wallet))))
                          (println "wallet connected!"))}

   (if (some? @wallet-address)
     (concat "connected to " (subs @wallet-address 0 10) "...")
     "connect")])


(defn Application []
  [:div {:class $section}
   [:div [:label {:class $h1} "dmap"] [ConnecBtn]]
   [Dpath]
   [Data]
   [Meta]
   [Trace]])

(dom/render [Application] (js/document.getElementById "app"))

;;;; view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;