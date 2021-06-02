/// <reference path="typings/ue.d.ts">/>
// ; typing info for auto-completion in Visual Studio Code

"use strict"

let UMG = require('UMG')
let _ = require('lodash')
    
function GetPC() {
    return PlayerController.C(GWorld.GetAllActorsOfClass(PlayerController).OutActors[0])
}

function main() {

    console.log("Jeu De l'Oie : ClojureScript")
    console.log("Initialisation")
    var state = game.core.InitGameState(game.core.players)
    let widget = null
    let PC = GetPC()

    // create a widget
    widget = GWorld.CreateWidget(JavascriptWidget, PC)
    widget.JavascriptContext = Context
    widget.bSupportsKeyboardFocus = true

    let design = UMG.span({},
        UMG.div({ 'slot.size.size-rule': 'Fill' },
            nextUI()
        )
    )


    let instantiator = require('instantiator')
    let page = instantiator(design)

    page.Visibility = 'Visible'

    widget.SetRootWidget(page)
    widget.AddToViewport()

    // Switch PC to UI only mode.
    PC.bShowMouseCursor = true
    PC.SetInputMode_UIOnly(page)


    function createGoose(id , name, color) {
        let character =GWorld.BeginSpawningActorFromClass(Goose_Player_C)
        character.SetIntPropertyByName("Id", id)
        character.SetIntPropertyByName("Cell", 0)
        character.SetStringPropertyByName("Name", name)
        character.SetStringPropertyByName("Color", color)
        character.SpawnDefaultController()
        let t = new Transform({ Translation: Vector.C({ X: 1, Y: 1, Z: 10 }) })
        character.FinishSpawningActor(t);

        return character
    }
    //a bit boring to break apart this clojure object, specific func should exists btw
    let names = Array.from(cljs.core.map.call(null, new cljs.core.Keyword(null, "name", "name", 1843675177), game.core.players))
    let colors = Array.from(cljs.core.map.call(null, new cljs.core.Keyword(null, "color", "color", 1011675173), game.core.players))
    const characters = new Map()
    for(let i = 0; i < game.core.NbPlayer(state); i++)
    {
        characters.set(colors[i], createGoose(i, names[i], colors[i]))
    }

    function nextUI() {
        function fire() {
            var roll = game.core.Roll();
            state = game.core.ComputeNextGameState(state, roll)
            let color = game.core.GetPreviousColor(state);
            let cell = game.core.GetPreviousPlayerCell(state);
           characters.get(color).SetIntPropertyByName("Cell", cell)
        }
        return UMG.div({ 'slot.size.size-rule': 'Fill' },
            UMG(Button, { OnClicked: _ => fire() }, "Compute Next Turn")
        )
    }
    return function () {
        widget.RemoveFromViewport()
        characters.forEach((a) => a.DestroyActor())
    }
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
    require('bootstrapClj')('game.core','JeuDeLOie')
}
