import scala.io.Source
import scala.util.Random

class Wordle(id: String, api: WordleAPI):

  val instructions: String =
    "Computer will guess the word; feedback is fetched from the API"

  val allWords: List[String] =
    try Source.fromFile("5_letter_words.txt").getLines().map(_.trim.toLowerCase).filter(_.length == 5).toList
    catch case _: Exception =>
      println("Word list file not found."); sys.exit(1)

  var possibleWords: List[String] = Random.shuffle(allWords)
  var guess: String = possibleWords.head
  var attempts: Int = 0
  val maxAttempts: Int = 6
  var status: String = "PLAY"
  var response: String = ""
  val wordSize = 5

  def play(): Unit =
    println(guess)
    val resp = api.guess(id, guess)
    if resp.length != wordSize || resp.exists(c => !"GYR".contains(c)) then
      println("Invalid feedback from API")
    else if resp == "GGGGG" then
      println(s"The computer guessed your word in ${attempts + 1} attempts!")
      status = "WON"
    else
      response = resp

  def removeImpossibleWords(): Unit =
    val guessChars = guess.toList
    val feedbackChars = response.toList

    val greens: List[Option[Char]] =
      feedbackChars.zip(guessChars).map((f, c) =>
        if f == 'G' then Some(c) else None
      )

    val ambers: List[Char] =
      feedbackChars.zip(guessChars).collect {
        case (f, c) if f == 'Y' => c
      }

    val greys: List[Char] =
      feedbackChars.zip(guessChars)
        .collect { case (f, c) if f == 'R' => c }
        .distinct
        .diff(ambers)

    def matchGreens(word: String): Boolean =
      word.zip(greens).forall {
        case (_, None)      => true
        case (wc, Some(c))  => wc == c
      }

    def matchAmbers(word: String): Boolean =
      ambers.forall(word.contains) &&
        feedbackChars.zipWithIndex.forall {
          case ('Y', i) => word(i) != guess(i)
          case _        => true
        }

    def matchGreys(word: String): Boolean =
      greys.forall(g => !word.contains(g))

    possibleWords = possibleWords.tail.filter(word =>
      matchGreens(word) && matchAmbers(word) && matchGreys(word)
    )

    println(s"Filtered, ${possibleWords.length} possible words remain.")

  def game(): Unit =
    println(instructions)

    while status == "PLAY" && possibleWords.nonEmpty && attempts < maxAttempts do
      play()
      if status == "WON" then return

      removeImpossibleWords()

      if possibleWords.isEmpty then
        println("No more possible words. Game over.")
        return

      guess = possibleWords.head
      attempts += 1

    if status != "WON" then
      println("The computer couldn't guess your word in 6 attempts.")