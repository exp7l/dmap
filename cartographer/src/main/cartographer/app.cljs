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
(defonce provider (.getDefaultProvider ethers rpc))

(defn emap-obj  [addr]
  (ethers/Contract. addr abi/emap-abi provider))

(defn fetch-kvs [emap-obj map-id key typ]
  (let [encoded (dmap/abiEncode #js ["bytes32" "bytes24"] #js [map-id key])
        physical-key (dmap/keccak256 encoded)]
    (js-await [bytes (.get emap-obj physical-key)]
              (println "debug 1: " typ)
              (swap! leaf-kvs #(assoc % [key typ] bytes)))))

(defn fetch-keys [emap-addr map-id map?]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (if map?
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
                      (catch err (println "fetch-name,err: " err)))))

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
(def $bold (css {:font-weight "bold"}))
(def $input-btn (css {:text-align "center"
                      :padding-block "1px"
                      :padding-inline "6px"
                      :border-color "black"
                      :border-width "thin"
                      :margin-left "1em"}))
(def $input-text (css {:padding-inline "6px"
                       :border-width "medium"
                       :border-color "black"
                       :border-radius "5px"}))

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
    "get trace"]])

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
     {:class (css {:margin-bottom "1em"})}
     [:label {:class $h3}
      "dmap view (single entry)"]
     [:div {:class (css {:padding-top "0.5em"})}
      (.toHexString (:data @leaf))
      (if (-> (:meta @leaf)
              (util/parsed-meta)
              (:map?))
        [:div "^^^ this is a map id, see its key-values below"])]]))

(defn KeyValue [kv]
  (let [[[key typ] value] kv]
    [:div
     [:div {:class $h3}
      "entry: "
      "{"
      "\"" (util/decode-utf8 key) "\""
      " "
      "\"" (.decode defaultAbiCoder #js [(util/typ-conventions typ)] value) "\""
      "}"]
     [:div [:label {:class $bold} "decoded type "] (util/typ-conventions typ)]
     [:div [:label {:class $bold} "raw key "] key]
     [:div {:class (css {:max-width "32em"
                         :word-wrap "break-word"})}
      [:label {:class $bold} "raw value "] value]]))

(defn KeyValues [kvs]
  (into [:div]
        (map #(vector
               :div
               {:class (css {:margin-top "0.75em"})}
               (KeyValue %)))
        kvs))

(defn Emap []
  (if (some? @leaf-kvs)
    [:div
     {:class (css {:margin-bottom "1em"})}
     [:label {:class (css {:font-size "1.25em"
                           :font-weight "bold"})}
      "emap view (multi entries)"]
     [:div {:class (css {:margin-top "0.5em"})}
      [:label {:class $bold} "map ID "]
      (.toHexString (:data @leaf))]
     [:div {:class (css {:margin-top "1.5em"})} [KeyValues @leaf-kvs]]]
    nil))

(defn Data []
  [:div
   [:div {:class $h2} "data"]
   [Dmap]
   [Emap]])

(defn Application []
  [:div {:class (css {:font-family "monospace" :margin "0em 0.5em"})}
   [:div {:class $h1} "dmap"]
   [Dpath]
   [Trace]
   [Meta]
   [Data]])

(dom/render [Application] (js/document.getElementById "app"))

;;;; view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;