(ns user
  (:require [shadow.cljs.devtools.api :refer [repl repl-runtime-clear]]))

(defn reset!
  "Reset the state of the REPL.

  This is useful when working with the Android simulator, because
  it'll sporadically just drop the WebSocket connection. So to work
  around it, you have to reset it using this:

    foo.app> :repl/quit
    user> (reset!)
    user> (repl :app)

  Which would bring you back to the ClojureScript REPL of the app, in
  the ns as defined under :devtools :repl-init-ns.

  NOTE: the assumption is that the build name of the app is `:app`,
  which as of the time of the writing, it is."
  []
  (do (repl-runtime-clear)
      (repl :app)))
