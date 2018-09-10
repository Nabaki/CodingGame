import scala.io.Source

object LabyrinthBuilder {

  def givenLabFromFile(fileName: String, width: Int, height: Int): Array[Array[Char]] = {
    val lines = Source.fromFile(fileName).getLines

    var labyrinth = Array.ofDim[Char](width, height)
    for (y <- 0 until height) {
      val line = lines.next()
      for (x <- 0 until width) {
        labyrinth(x)(y) = line(x)
      }
    }
    labyrinth
  }

  def expectedLabFromFile(fileName: String, width: Int, height: Int): Array[Array[Int]] = {
    val lines = Source.fromFile(fileName).getLines

    var labyrinth = Array.ofDim[Int](width, height)
    for (y <- 0 until height) {
      val line = lines.next().split(',')
      for (x <- 0 until width) {
        labyrinth(x)(y) = line(x).toInt
      }
    }
    labyrinth
  }
}