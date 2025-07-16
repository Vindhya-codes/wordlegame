import requests._
import ujson._

class WordleAPI:
  private val baseUrl = "https://wordle.we4shakthi.in/game"
  private val session = new Session()

  def register(name: String = "vindhya"): String =
    val payload = Obj("mode" -> "wordle", "name" -> name)
    val res = session.post(s"$baseUrl/register", data = payload)
    println(s"Register: ${res.text()}")
    val json = ujson.read(res.text())
    json("id").str

  def createGame(id: String): Unit =
    val payload = Obj("id" -> id, "overwrite" -> true)
    val res = session.post(s"$baseUrl/create", data = payload)
    println(s"Create Game: ${res.text()}")

  def guess(id: String, guessWord: String): String =
    val payload = Obj("guess" -> guessWord, "id" -> id)
    val res = session.post(s"$baseUrl/guess", data = payload)
    println(s"Guess: $guessWord")
    if res.statusCode != 200 then
      println(s"Error: ${res.text()}")
      "Invalid word"
    else
      val feedback = ujson.read(res.text())("feedback").str.replace("B", "R")
      println(s"API Response: ${res.text()}")
      feedback