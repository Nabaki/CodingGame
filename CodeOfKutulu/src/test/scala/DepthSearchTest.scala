class DepthSearchTest
  extends UnitTest {

  "DepthSearch" should {
    "renvoyer le résultat attendu" when {
      "le labyrinthe est vide" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6)
        println("givenLab")
        Labyrinth.print(givenLab)

        val depthLab: Array[Array[Int]] = Algorithm.depthSearch(givenLab, Position(1, 1))
        println("depthLab")
        Labyrinth.print(depthLab)

        val expectedLab = LabyrinthBuilder.expectedLabFromFile("src/test/resources/depthSearch/emptyLab/expected.txt", 6, 6)
        println("expectedLab")
        Labyrinth.print(expectedLab)

        depthLab shouldEqual expectedLab
      }

      "le labyrinthe est une croix simple" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/simpleCross/given.txt", 5, 5)
        println("givenLab")
        Labyrinth.print(givenLab)

        val depthLab: Array[Array[Int]] = Algorithm.depthSearch(givenLab, Position(2, 2))
        println("depthLab")
        Labyrinth.print(depthLab)

        val expectedLab = LabyrinthBuilder.expectedLabFromFile("src/test/resources/depthSearch/simpleCross/expected.txt", 5, 5)
        println("expectedLab")
        Labyrinth.print(expectedLab)

        depthLab shouldEqual expectedLab
      }

      "le labyrinthe assymétrique" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/assymLab/given.txt", 9, 7)
        println("givenLab")
        Labyrinth.print(givenLab)

        val depthLab: Array[Array[Int]] = Algorithm.depthSearch(givenLab, Position(5, 1))
        println("depthLab")
        Labyrinth.print(depthLab)

        val expectedLab = LabyrinthBuilder.expectedLabFromFile("src/test/resources/depthSearch/assymLab/expected.txt", 9, 7)
        println("expectedLab")
        Labyrinth.print(expectedLab)

        depthLab shouldEqual expectedLab
      }
    }
  }
}