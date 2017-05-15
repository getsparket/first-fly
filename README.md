ok. this *might* work if you follow the directions for [re-natal](https://github.com/drapanjanas/re-natal) and [re-navigate](https://github.com/vikeri/re-navigate).

and you'll have to change in index.ios.js (possibly some equivalent in index.android.js)
```
require('figwheel-bridge').withModules(modules).start('$WHATEVER','ios','localhost');
```
to
```
require('figwheel-bridge').withModules(modules).start('flierplath','ios','localhost');
```
if you're lucky, all you'll have to do is the re-navigate instructions. i.e. yarn, re-natal, etc. maybe you'll get lucky! or i can get you set up.
