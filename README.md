### Taking this example for a spin

Assuming you have the [shadow-cljs npm package](https://shadow-cljs.github.io/docs/UsersGuide.html#_high_level_overview) installed:

1. From the `shadow/` directory, run `shadow-cljs release extension` and wait for the build to complete.
2. Go to `"chrome://extensions"`.
3. Toggle on 'Developer mode' in the top right of the page.
4. Click the 'Load unpacked' button and find `~/[...]/hacky-chromex-shadow-manifest-v2-to-v3-migration/shadow/resources/unpacked`.

### Changes made (and why)

1. Versions in `deps.edn` bumped to latest, and `binaryage/chromex` made a non-local dependency. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/5091ce4a851384e1e7e02fc4592d698a05812142))
2. `:manifest-version` in `shadow/resources/unpacked/manifest.edn` bumped from `2` to `3`. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/32d891c44bb66639424ab9b97634d2e7c742e3e6))
3. Introduced a [shadow-cljs build hook](https://shadow-cljs.github.io/docs/UsersGuide.html#build-hooks), to run at the very end of the build. This is responsible for all of the hacky patching required to get this thing working. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/7667c6e72982d7b018666b91350cbb9157685e27))
   1. Patched `"background"` key in manifest. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/7667c6e72982d7b018666b91350cbb9157685e27))
      - [Under Manifest V3](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#man-sw), the `"background"` key can only take a single `"service_worker"` where before it took an array of `"scripts"`.
      - This is why we ignore all `"scripts"` but `"out/background.js"`, here.
   2. Prepended copy of `out/shared.js` to `out/background.js`. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/7667c6e72982d7b018666b91350cbb9157685e27))
      - Even developing against release builds, shadow generates multiple `"background"` scripts: `out/background.js`, `out/bg-shared.js`, and `out/shared.js`. So whilst [it's possible](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#man-sw) under Manifest V3 to declare the background `"service_worker"` an ES Module and statically import such additional code into `out/background.js`, `out/shared.js` currently doesn't [export](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/export) stuff in the necessary way.
   3. Patched `"content_security_policy"` key in manifest. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/f151608d201547b635b61b33b95008a7aff46099))
      - It [has a different shape in Manifest V3](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#content-security-policy).
   4. Removed `'unsafe-eval'` from Content Security Policy. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/f151608d201547b635b61b33b95008a7aff46099))
      - Again, [Manifest V3 doesn't allow it](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#content-security-policy).
   5. Patched `"actions"`. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/758511ff5e5275d97d0a7b50aa469170abf44537))
      - It [has a different shape in Manifest V3](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#action-api-unification).
   6. Fixed permissions. ([commit](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration/commit/2c13ebac2aff9864215aec24cdd94b506e3a16d9))
      - [Host permissions are declared separately in Manifest V3.](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#host-permissions)

### Some questions!

1. Regarding (3)(ii) [above](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration#changes-made-and-why), perhaps _hot reloading of code during development_ could be recovered if there were a way to deal with the many background-relevant scripts that `shadow-cljs watch extension` generates in addition to `out/background.js` — as opposed to just `out/shared.js` for `shadow-cljs release extension`. So... aside from prepending every single one to `out/background.js`, what options are there?
   1. They could potentially be [statically imported](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#man-sw) if shadow threw a few [export](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/export)s in during building, right?
   2. Something else?
2. With `'unsafe-eval` [gone in Manifest V3, even during dev](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#content-security-policy), it's unclear how to get a REPL hooked up to the various runtimes comprising a running extension. (Related shadow-cljs issue/comment: https://github.com/thheller/shadow-cljs/issues/902#issuecomment-1021935288.) And even if it _were_ possible, the [short-lived nature of background service workers](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#background-service-workers) (replacing Manifest V2's background pages) would make REPL-driven dev against background-related `cljs` files annoying... probably. Are there any good solutions here, aside from maybe:
   1. Developing against Manifest V2 in a V3-compatible way, then building the extension as V3?
   2. Developing against a [modified Chromium, enabling unsafe-eval](https://github.com/thheller/shadow-cljs/issues/902#issuecomment-1021973818)?
3. It seems so far that all the required changes are shadow-related, because they're related to how the extension is built/packaged — in particular, by [this namespace here](https://github.com/thheller/shadow-cljs/blob/49fb078b834e64f63122e3a8ad3ddcd1f4485969/src/main/shadow/build/targets/chrome_extension.clj). Is this right? And if so, should a patch be submitted?
