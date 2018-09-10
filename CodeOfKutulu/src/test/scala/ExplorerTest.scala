
class ExplorerTest
  extends UnitTest {
  "Explorer" should {
    "renvoyer le résultat attendu" when {
      "l'allié est à la même case" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6)
        val givenExplorer1 = Explorer(0, Position(2, 2), 9999, 0, 0, givenLab)
        val givenExplorer2 = Explorer(1, Position(2, 2), 9999, 0, 0, givenLab)

        val friendRange: Option[Position] = givenExplorer1.goInFriendRange(Seq(givenExplorer2), givenLab)
        friendRange.get shouldEqual Position(2, 2)
      }

      "l'allié est à une case d'eccart" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6)
        val givenExplorer1 = Explorer(0, Position(1, 1), 9999, 0, 0, givenLab)
        val givenExplorer2 = Explorer(1, Position(2, 2), 9999, 0, 0, givenLab)

        val friendRange: Option[Position] = givenExplorer1.goInFriendRange(Seq(givenExplorer2), givenLab)
        friendRange.get shouldEqual Position(1, 1)
      }

      "l'allié est ailleurs" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6)
        val givenExplorer1 = Explorer(0, Position(1, 1), 9999, 0, 0, givenLab)
        val givenExplorer2 = Explorer(1, Position(4, 4), 9999, 0, 0, givenLab)

        val friendRange: Option[Position] = givenExplorer1.goInFriendRange(Seq(givenExplorer2), givenLab)
        friendRange.get shouldEqual Position(2, 4)
      }

      "l'allié ne peut être atteint" in {
        val givenLab = LabyrinthBuilder.givenLabFromFile("src/test/resources/depthSearch/simpleCross/given.txt", 5, 5)
        val givenExplorer1 = Explorer(0, Position(2, 2), 9999, 0, 0, givenLab)
        val givenExplorer2 = Explorer(1, Position(4, 4), 9999, 0, 0, givenLab)

        val friendRange: Option[Position] = givenExplorer1.goInFriendRange(Seq(givenExplorer2), givenLab)
        friendRange shouldBe None
      }
    }
  }
}