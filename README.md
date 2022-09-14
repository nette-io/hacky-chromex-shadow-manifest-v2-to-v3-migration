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

### Changes made and why

...

### Some questions!

...
