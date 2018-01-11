# IOTA Flash Channel Java Wrapper

This wrapper allows the usage of [iota.flash.js](https://github.com/iotaledger/iota.flash.js) lib directly in Java. This is achieved by running the iota.flash.js script and all dependencies (modified build of iota.flash.js) in the V8 engine (using [J2V8](https://github.com/eclipsesource/J2V8) bindings for V8). 
If you have any ideas please submit a request (I am totally not a Java guy so...)



## iota.flash.js coverage

#### Multisig
- [x] composeAddress
- [x] updateLeafToRoot
- [x] getDigest

#### Model.Transfer
- [x] prepare
- [x] compose
- [x] close (needs testing)
- [x] applyTransfers
- [x] appliedSignatures 
- [ ] getDiff (not used at the moment)
- [x] sign 



### Installation

1. Clone repo
2. Update maven ressources
3. That's it.
4. You can run a test transaction by running the main func in the Main Class.



## Updating iota.flash.js
I will try to update the iota.flash.js as often as I can till it is automated.
To manually update the lib do the following:

1. clone js lib from the repo  `git clone git@github.com:iotaledger/iota.flash.js.git`
2. inside the cloned project open the gulpfile.js and change dist task to this
    ```javascript
    gulp.task('dist', () => {
      return gulp.src('lib/flash.js')
        .pipe(webpack({
          output: {
            filename: 'iota.flash.js',
            libraryTarget: 'umd',
            library: 'iotaFlash',
            umdNamedDefine: true
          }
        }))
        .pipe(gulp.dest(DEST))
    });
    ```
    By doing this we get a umd package which we can access from the global js context under the name `iotaFlash
3. Copy the file from `dist/iota.flash.js` of the js lib folder to this project under `res/iota.flash.js`
4. Add `"use strict";`to the start of the file you just coppied (If you don't the V8 will complain).
5. That was it...



## TODO

- [ ] write documentation  
- [ ] add some testing
- [ ] add a way to update iota.flash.js from the repo without manual steps
- [ ] cleanup maven
- [ ] makte this project a easy to import lib


