((clojure-mode
  (cider-clojure-cli-aliases . ":dev")
  (cider-preferred-build-tool . clojure-cli)
  (cider-ns-refresh-before-fn . "integrant.repl/suspend")
  (cider-ns-refresh-after-fn . "integrant.repl/resume")
  ))
