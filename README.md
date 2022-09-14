### Taking this example for a spin

As there's no more `'unsafe-eval'` in Manifest V3 Chrome extensions, [not even in dev](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#content-security-policy), and release builds create fewer (of what used to be, in Manifest V2) [background scripts](https://developer.chrome.com/docs/extensions/mv3/intro/mv3-migration/#man-sw), we developed this example against release builds to keep things simple. It's an [open question](https://github.com/nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration#some-questions) how to recover [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html) + [Chromex](https://github.com/binaryage/chromex)'s hot reloading, REPL-driven workflow under Manifest V3.

Anyway, assuming you have the [shadow-cljs npm package](https://shadow-cljs.github.io/docs/UsersGuide.html#_high_level_overview) installed:

```
# clone this repo somewhere
~ $ mkdir tmp
~ $ cd tmp
~/tmp $ git clone git@github.com:nette-io/hacky-chromex-shadow-manifest-v2-to-v3-migration.git

# cd into the shadow/ dir
~/tmp $ cd hacky-chromex-shadow-manifest-v2-to-v3-migration/shadow/

# produce a release build
~/tmp/hacky-chromex-shadow-manifest-v2-to-v3-migration/shadow $ shadow-cljs release extension
```
Then:
1. Go to `"chrome://extensions"`.
2. Toggle on 'Developer mode' in the top right of the page.
3. Click the 'Load unpacked' button and find `~/tmp/hacky-chromex-shadow-manifest-v2-to-v3-migration/shadow/resources/unpacked`.

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

...
