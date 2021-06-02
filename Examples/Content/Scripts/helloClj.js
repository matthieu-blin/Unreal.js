/// <reference path="typings/ue.d.ts">/>
// ; typing info for auto-completion in Visual Studio Code

"use strict"

function main() {
    return function () {}
}

// bootstrap to initiate live-reloading dev env.
try {
    module.exports = () => {
        let cleanup = null

        // wait for map to be loaded.
        process.nextTick(() => cleanup = main());

        // live-reloadable function should return its cleanup function
        return () => cleanup()
    }
}
catch (e) {
    require('bootstrapClj')('hello_world.core', 'helloClj' )
}
