#!/bin/bash
java -cp "cljs.jar;src" cljs.main --target none --output-to main.js -c hello-world.core
printf "\nmodule.exports = {goog}" >> './out/goog/base.js'
