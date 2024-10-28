(ns tablecloth.index.protocols
  (:require [tech.v3.dataset :refer [missing]])
  (:import [tech.v3.dataset.impl.column Column]))

(defprotocol PHasIndexStructure
  (index-structure [this]
    "Returns an index-structure for the column. The index-structure will only be
generated the first time this is function is called.")
  (index-structure-realized? [this]
    "Returns true if the index-structure value has been produced. The index-structure
is only produced the first time it is requested.")
  (with-index-structure
    [this custom-make-index-structure-fn]
    "Returns a copy of the column that will return an index-structure using the
provided `custom-make-index-strucutre-fn`."))

(defprotocol PIndexStructure
  (select-from-index
    [index-structure mode selection-spec options]
    "Select a subset of the index. Supports a variety of modes."))


(def index-structure-registry (atom {}))


(extend-type Column
  PHasIndexStructure
  ;; This index-structure returned by this function can be invalid if
  ;; the column's reader is based on a non-deterministic computation.
  ;; For now, we think this may be okay because it's a unique edge-case.
  ;; What value could an index have on data that is random and changing?
  ;; We think it is reasonable to expect the user of tech.ml.dataset, which
  ;; is a somewhat low-level library, to know that it wouldn't make sense
  ;; to request the index structure on a column consisting of such data.
  ;; For more, see this discussion on Clojurians Zulip: https://bit.ly/3dRa9MY
  ;;
  ;; TODO: Considering validating by checking index values against column data (traversal or hashing)
  (index-structure [this]
    (if (empty? missing)
      @*index-structure
      (throw (Exception.
              (str "Cannot obtain an index for column `"
                   (col-proto/column-name this)
                   "` because it contains missing values.")))))
  (index-structure-realized? [this]
    (realized? *index-structure))
  (with-index-structure [this make-index-structure-fn]
    (Column. missing
             data
             metadata
             cached-vector
             (delay (make-index-structure-fn data metadata)))))


;; tech.ml.dataset at point just after index had matured.
;; https://github.com/techascent/tech.ml.dataset/tree/73681aae0c225f53b13b881a3c8a50f3a04b9257
