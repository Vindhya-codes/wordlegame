@main def runWordle(): Unit =
  val api = new WordleAPI()
  val id = api.register("vindhya")
  api.createGame(id)
  val game = new Wordle(id, api)
  game.game()