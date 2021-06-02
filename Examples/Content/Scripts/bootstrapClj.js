(function (global) {
    "use strict"

    module.exports = function (clojureFile, filename) {
//        Context.WriteDTS(Context.Paths[0] + 'typings/ue.d.ts')
//        Context.WriteAliases(Context.Paths[0] + 'aliases.js')
        Context.RunFile('aliases.js')
        Context.RunFile('polyfill/unrealengine.js')
        Context.RunFile('polyfill/timers.js')


        var googClosureBaseFile = 'Clj/out/goog/base.js'
        //load all dependencies, need to be compiled with clj2Js.sh (target = none and module.export added to goog closure base)
        global.CLOSURE_UNCOMPILED_DEFINES = {};
        global.CLOSURE_NO_DEPS = true;
        try {
            const { goog } = require(googClosureBaseFile)
            global.goog = goog
            global.CLOSURE_IMPORT_SCRIPT = require
            Context.RunFile('Clj/main.js')
            goog.require(clojureFile)
        } catch (Exception) {
            console.warn("Can't find google closure library, please compile your clojurescripts using clj2js (default location in Clj/) or check for errors")
        }

        require('devrequire')(filename)
    }
})(this)


