java -cp "cljs.jar;src" cljs.main --target none --output-to main.js -c JeuDeLOie.core
ECHO.>>".\out\goog\base.js"
ECHO module.exports = {goog}>>".\out\goog\base.js"
pause