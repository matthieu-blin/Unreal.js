(ns hello-world.core)

(println "Hello Clojure!")

(defn average [a b]
  (/ (+ a b) 2.0))

(println (average 2 3))