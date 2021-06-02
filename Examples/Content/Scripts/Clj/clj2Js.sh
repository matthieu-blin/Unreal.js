#!/bin/bash
java -cp "cljs.jar;src" cljs.main --target none --output-to main.js -c JeuDeLOie.core
printf "\nmodule.exports = {goog}" >> './out/goog/base.js'
