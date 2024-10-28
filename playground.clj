(ns playground
  (:require [tech.v3.dataset :as tmd])
  (:import [tech.v3.dataset.impl.column Column]))

(defprotocol ExtendedColumn
  (hello-world [this] "Hello World!"))

(extend-type Column
  ExtendedColumn
  (hello-world [this]
    "Hello World!"))

(def col (tech.v3.dataset.column/new-column "test" [1 2 3 4 5]))

col


(hello-world col)



