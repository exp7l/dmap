(ns cartographer.app
  (:require [reagent.core :as r]
            [reagent.dom  :as dom]
            [shadow.cljs.modern :refer (js-await)]
            [clojure.pprint :as pp]
            [cljs.math :as math]
            ["./dmap.js" :as dmap]
            ["./dmain.js" :as dmain]
            ["@ethersproject/bignumber" :refer (BigNumber)]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init [] (println "init"))

(defonce rpc "https://arbitrum-goerli.public.blastapi.io")
(defonce trace (r/atom nil))
(defonce leaf-meta (r/atom nil))
(defonce leaf-data (r/atom nil))

(defn walk [rpc dpath]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (js-await [[facade _] (dmain/getFacade rpc)]
            (js-await [t (dmap/walk2 facade dpath)]
                      (let [v (js->clj t)]
                        (println "cljs, trace: " v)
                        (swap! trace (constantly v))
                        (swap! leaf-meta (constantly (.from BigNumber (first (last v)))))
                        (swap! leaf-data (constantly (.from BigNumber (second (last v))))))
                      (catch err (println "err: " err)))))

(defn Trace []
  [:div 
   [:p "## Trace: "] 
   [:p "* Querying aginst contract object: " dmap/address "; RPC: " rpc]
   [:div (with-out-str (pp/pprint @trace))]])

(defn Dpath []
  [:div
   [:label 
    [:p "## Dpath: "]]
   [:input {:type "text" :id "dpath"}]
   [:button {:onClick #(walk rpc
                             (-> js/document
                                 (.getElementById "dpath")
                                 (.-value)))}
    "walk"]])

(defn extract-bit [bn i]
  (-> bn
      (.mask i)
      (.toNumber) 
      (bit-and (math/pow 2 (dec i)))))

(defn MetaTypeParser []
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (some? @leaf-meta)
    (let [parsed (-> {}
                     ;; standard #1: the least significant bit denotes 
                     ;; whether the slot is lock
                     (assoc :lock? (= 1 (extract-bit @leaf-meta 1) 1))
                     ;; standard #2: the 2nd least significant bit denotes
                     ;; ether `data` points to a blob
                     (assoc :blob? (= 1 (extract-bit @leaf-meta 2) 1)))]
      [:div ":lock? " (str (:lock? parsed)) "   |   " "blob? " (str (:blob? parsed))])))

(defn LeafMetaView []
  [:div 
   [:p "## Leaf Meta View"]
   [:div [MetaTypeParser]]])

(defn LeafDataView []
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (some? @leaf-data)
    [:div 
     [:p "## Leaf Data View"]
     [:div (.toHexString @leaf-data)]]))

(defn Application []
  [:div
   [:p "# Application"]
   [Dpath]
   [Trace]
   [LeafMetaView]
   [LeafDataView]])

(dom/render [Application] (js/document.getElementById "app"))