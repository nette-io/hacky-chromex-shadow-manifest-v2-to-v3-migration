(ns hooks.core
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :refer [pprint]]))

(defn- read-manifest []
  (json/read-str (slurp "resources/unpacked/manifest.json")))

(defn- write-manifest [manifest]
  (spit "resources/unpacked/manifest.json"
    (with-out-str
      (json/pprint manifest))))

;; Note what we drop/ignore, here:
;;  - All the various `"scripts"`, because for the purposes of this minimal migration developing against release builds
;;    simplifies things considerably.
;;  - `"type" "module"`, because the generated `out/shared.js` and `out/bg-shared.js` aren't set up to be statically
;;    imported, as per https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#man-sw. We hackily prepend
;;    copies to `out/background.js` instead, in `fix-background-calls` below.
(defn- fix-background-key [manifest]
  (update manifest "background"
          (fn [{:strs [_scripts]}]
            {"service_worker" "out/background.js"
             #_#_"type" "module"})))

(defn- fix-background-calls []
  (let [background (slurp "resources/unpacked/out/background.js")
        ;; FIXME: what about `out/bg-shared.js`? It's effectively empty right now, but might not be in future — e.g.
        ;;        during dev builds or as code is added to this minimal example...
        shared (slurp "resources/unpacked/out/shared.js")]
    (spit "resources/unpacked/out/background.js"
          (str "// hacky prepend of `out/shared.js`:" "\n\n" shared
               "\n\n\n"
               "// `out/background.js`, unchanged:" "\n\n" background))))

(defn patch-extension-outputs-manifest-v2->v3
  {:shadow.build/stage :flush}
  [build-state & _args]
  (let [manifest (read-manifest)
        manifest' (-> manifest
                      fix-background-key)]
    (prn)
    (prn "manifest.json before:")
    (pprint manifest)
    (prn)
    (prn "manifest.json after:")
    (pprint manifest')
    (prn)

    (write-manifest manifest')
    (fix-background-calls))
  build-state)
