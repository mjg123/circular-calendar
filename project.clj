(defproject ccal "0.1.0-SNAPSHOT"
  :description "Circular calendar"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [jayq "2.0.0"]]
  :plugins [[lein-cljsbuild "0.2.10"]]

  :cljsbuild {
    :builds [{
      :source-path "src-cljs"
      :compiler {
        :output-to "out/js/ccal.js"
        :optimizations :whitespace
        :pretty-print true}}]})
