
/**
  * Survive the wrath of Kutulu
  * Coded fearlessly by JohnnyYuge & nmahoude (ok we might have been a bit scared by the old god...but don't say anything)
  **/
object Player extends App {
  val width = scala.io.StdIn.readInt
  val height = scala.io.StdIn.readInt

  var labyrinth = Array.ofDim[Char](width, height)
  for (y <- 0 until height) {
    val line = scala.io.StdIn.readLine.toCharArray
    for (x <- 0 until width) {
      labyrinth(x)(y) = line(x)
    }
  }

  // sanitylosslonely: how much sanity you lose every turn when alone, always 3 until wood 1
  // sanitylossgroup: how much sanity you lose every turn when near another player, always 1 until wood 1
  // wandererspawntime: how many turns the wanderer take to spawn, always 3 until wood 1
  // wandererlifetime: how many turns the wanderer is on map after spawning, always 40 until wood 1
  val Array(sanitylosslonely, sanitylossgroup, wandererspawntime, wandererlifetime) = for (i <- scala.io.StdIn.readLine() split " ") yield i.toInt

  // game loop
  while (true) {
    val initializeTimer = System.currentTimeMillis()
    var me: Explorer = null
    var explorers = Seq[Explorer]()
    var wanderers = Seq[Wanderer]()

    // The first given entity corresponds to your explorer
    val entityCount = scala.io.StdIn.readInt
    for (i <- 0 until entityCount) {
      val Array(_entitytype, _id, _x, _y, _param0, _param1, _param2) = scala.io.StdIn.readLine() split " "
      val entitytype = EntityType.withName(_entitytype)
      val id = _id.toInt
      val x = _x.toInt
      val y = _y.toInt
      val param0 = _param0.toInt
      val param1 = _param1.toInt
      val param2 = _param2.toInt

      if (id != -1) {
        entitytype match {
          case EntityType.EXPLORER =>
            val newExplorer = Explorer(id, Position(x, y), param0, param1, param2, labyrinth)
            if (me == null) {
              me = newExplorer
            } else {
              explorers = explorers :+ newExplorer
            }
          case EntityType.WANDERER =>
            wanderers = wanderers :+ Wanderer(id, Position(x, y), param0, Minion.stateOf(param1), param2, labyrinth)
          case _ => Console.err.println(s"$entitytype not managed yet")
        }
      }
    }
    Console.err.println(s"Initialisation : ${System.currentTimeMillis() - initializeTimer} ms")
    val timer = System.currentTimeMillis()

    //Process
    val moveTo =
    if (!explorers.exists(_.isCloseTo(me, 2))) {
      //isAlone
      Console.err.println("isAlone")
      me.goInFriendRange(explorers, labyrinth)
    } else {
      //isTargeted
      Console.err.println("isTargeted")
      me.runFromWanderers(wanderers, explorers)
    }

    // Write an action using println
    // To debug: Console.err.println("Debug messages...")
    if (moveTo.isDefined) {
      println(s"MOVE ${moveTo.get.x} ${moveTo.get.y}")
    } else {
      println("WAIT")
    }

    Console.err.println(s"Timer : ${System.currentTimeMillis() - timer} ms")
  }
}

case class Explorer(id: Int, position: Position, health: Int, param1: Int, param2: Int, labyrinth: Array[Array[Char]]) extends Entity {

  def goInFriendRange(explorers: Seq[Explorer], labyrinth: Array[Array[Char]]): Option[Position] = {
    val timer = System.currentTimeMillis()

    val safePositions = explorers
      .flatMap(_.getReallySafePositions)
      .filter(position =>
        position.x >= 0 && position.y >= 0 && position.x < labyrinth.length && position.y < labyrinth(0).length
          && labyrinth(position.x)(position.y) != '#'
      )

    Console.err.println(s"goInFriendRange : ${System.currentTimeMillis() - timer} ms")
    Console.err.println(safePositions.mkString(" "))
    Algorithm.bestTarget(depthLab, position, safePositions)
  }

  /**
    * Distance from other explorers to lose less health = 2
    */
  private def getSafePositions: Set[Position] = {
    Set(
      Position(position.x + 2, position.y),
      Position(position.x - 2, position.y),
      Position(position.x, position.y - 2),
      Position(position.x, position.y + 2)
    ) ++ {
      for {
        x <- -1 to 1
        y <- -1 to 1
      } yield Position(position.x + x, position.y + y)
    }
  }

  /**
    * Distance from other explorers to lose less health = 2
    */
  private def getReallySafePositions: Set[Position] = {
    Set(
      Position(position.x + 1, position.y),
      Position(position.x - 1, position.y),
      Position(position.x, position.y),
      Position(position.x, position.y - 1),
      Position(position.x, position.y + 1)
    )
  }

  def runBehindAllies(allies: Seq[Explorer], closerWanderer: Option[Wanderer]) = {
    val safePositions = allies.flatMap{ allie =>
      Direction.values
        .map(direction => Position(allie.position.x + direction.x, allie.position.y + direction.y))
        .filter(position =>
          position.x >= 0 && position.y >= 0 && position.x < labyrinth.length && position.y < labyrinth(0).length
            && labyrinth(position.x)(position.y) != '#'
        )
        .sortBy(position => depthLab(position.x)(position.y))
        .drop(1)
    }

    Algorithm.bestTarget(depthLab, position, safePositions)
  }

  def runFromWanderers(wanderers: Seq[Wanderer], allies: Seq[Explorer]): Option[Position] = {

    //Récupération du wanderer le plus proche
    val closerWanderer = wanderers
      .sortBy(wanderer => depthLab(wanderer.position.x)(wanderer.position.y))
      .headOption
    Console.err.println(s"closerWanderer : $closerWanderer")

    //Console.err.println("depthSearch")
    //Labyrinth.print(depthLab)

    //Récupération du chemin que devra parcourir le wanderer
    val path = closerWanderer
      .map(wanderer => findPathTo(wanderer.position))

    System.err.println(s"path : $path")

    //Récupération des alliés qui feront bouclier
    val shields = path.map(
      _.filter(position =>
        allies.exists(allie =>
          allie.position == position
        )
      )
    )

    System.err.println(s"shields : $shields")

    //S'il n'existe aucun bouclier à une distance de 2 ou + de nous, on s'éloigne
    if (closerWanderer.isDefined) {
      if (shields.isDefined && shields.get.nonEmpty && shields.map(!_.exists(position => depthLab(position.x)(position.y) >= 2)).isDefined) {
        val closerAllie = shields.get.toSeq
          .sortBy(position => depthLab(position.x)(position.y))
          .head

        System.err.println(s"closerAllie : $closerAllie")

        //Go 2 depths behind the allie
        val res = closerWanderer.get.runFromHim(closerAllie, 2)
        System.err.println(s"runFromHim shield : $res")
        res
      } else {
        val res = runBehindAllies(allies, closerWanderer)
        System.err.println(s"runFromHim no shield : $res")
        res
      }
    } else {
      None
    }
  }
}

case class Wanderer(id: Int, position: Position, timer: Int, minionState: Minion.State, targetId: Int, labyrinth: Array[Array[Char]]) extends Entity

trait Entity {
  val id: Int
  val position: Position
  val labyrinth: Array[Array[Char]]

  lazy val depthLab = Algorithm.depthSearch(labyrinth, position)

  def isCloseTo(entity: Entity, distance: Int): Boolean = {
    val res = Position.distanceBetween(position, entity.position) <= distance
    //Console.err.println(s"${entity.position} is close to ${this.position} ? $res")
    res
  }

  /**
    * Position où on doit aller pour être à depth de distance du startPoint par rapport à l'entité en question
    */
  //FIXME bizarre
  def runFromHim(startPoint: Position, depth: Int): Option[Position] = {
    var res = Set(startPoint)
    for (i <- 0 until depth) {
      res = res.flatMap(startPoint =>
        Direction.values.map(direction => Position(startPoint.x + direction.x, startPoint.y + direction.y))
          .filter(newPosition =>
            //On garde seulement les cas où l'on s'éloigne
            depthLab(startPoint.x)(startPoint.y) < depthLab(newPosition.x)(newPosition.y)
          )
      )
    }
    res.headOption
  }

  override def toString: String = {
    position.toString
  }

  def findPathTo(destination: Position): Set[Position] = {
    val res = findPathLoop(Set(destination), depthLab(destination.x)(destination.y))
    if (res.isEmpty || depthLab(res.last.x)(res.last.y) != 0) {
      Set[Position]()
    } else {
      res
    }
  }

  private def findPathLoop(destinations: Set[Position], destinationsDepth: Int): Set[Position] = {
    val res = destinations.flatMap(destination =>
      Direction.values
        .map(direction => Position(destination.x + direction.x, destination.y + direction.y))
        .filter(position =>
          position.y >= 0 && position.x >= 0 && position.y < depthLab(0).length && position.x < depthLab.length
            && depthLab(position.x)(position.y) != -1
            && depthLab(position.x)(position.y) < destinationsDepth)
    )

    if (res.isEmpty || destinationsDepth == 0) {
      res
    } else {
      res ++ findPathLoop(res, destinationsDepth - 1)
    }
  }
}

case class Portal()

object Action extends Enumeration {
  val WAIT, MOVE = Value
}

object EntityType extends Enumeration {
  val EXPLORER, WANDERER, EFFECT_PLAN, EFFECT_LIGHT = Value
}

/**
  * # : mur
  * w : portail d'invocation
  * . : case vide
  */
object Node {

  abstract sealed class State(val value: Char)

  case object WALL extends State('#')

  case object PORTAL extends State('w')

  case object EMPTY extends State('.')

}

/**
  * SPAWNING = 0
  * WANDERING = 1
  */
object Minion {

  abstract sealed class State(val value: Int)

  case object SPAWNING extends State(0)

  case object WANDERING extends State(1)

  val states = Seq(SPAWNING, WANDERING)

  def stateOf(value: Int): State = {
    states.filter(_.value == value).head
  }
}

case class Position(x: Int, y: Int)

object Position {

  /**
    * Distance entre deux positions sans prendre en compte les murs
    */
  def distanceBetween(position1: Position, position2: Position): Int = {
    val res = Math.abs(position1.x - position2.x) + Math.abs(position1.y - position2.y)
    //Console.err.println(s"Distance between $position1 and $position2 ? $res")
    res
  }
}

object Algorithm {

  /**
    * Initialize depthSearchLoop
    */
  def depthSearch(labyrinth: Array[Array[Char]], startPosition: Position): Array[Array[Int]] = {
    val timer = System.currentTimeMillis()
    val width = labyrinth.length
    val height = labyrinth(0).length
    val depthLab = Array.fill(width)(Array.fill(height)(-1))
    Console.err.println(s"depthSearch middle : ${System.currentTimeMillis() - timer}")
    val loop: Array[Array[Int]] = depthSearchLoop(labyrinth, Set(startPosition), Set[Position](), depthLab, 0)
    Console.err.println(s"depthSearch end : ${System.currentTimeMillis() - timer}")
    loop
  }

  /**
    * BFS
    */
  private def depthSearchLoop(labyrinth: Array[Array[Char]], toVisit: Set[Position], visited: Set[Position], depthLab: Array[Array[Int]], depth: Int): Array[Array[Int]] = {
    val timer = System.currentTimeMillis()
    if (toVisit.isEmpty) {
      depthLab
    } else {
      toVisit.foreach { position => depthLab(position.x)(position.y) = depth }

      val newPositionsToVisit =
        toVisit
          .flatMap(
            position =>
              Direction.values.map(direction => Position(position.x + direction.x, position.y + direction.y))
          )
          .filter(position =>
            position.y >= 0 && position.x >= 0 && position.y < labyrinth(0).length && position.x < labyrinth.length
              && labyrinth(position.x)(position.y) != '#'
              && !visited.contains(position)
          )

      Console.err.println(s"depthSearchLoop : ${System.currentTimeMillis() - timer}")
      depthSearchLoop(labyrinth, newPositionsToVisit, visited ++ toVisit, depthLab, depth + 1)
    }
  }

  /**
    * A partir du tableau des distances on cré une map (distance, position) que l'on tri pour sortir le plus proche
    */
  def bestTarget(depthLab: Array[Array[Int]], start: Position, targets: Seq[Position]): Option[Position] = {
    val timer = System.currentTimeMillis()
    Console.err.println(s"bestTarget : ${targets.mkString(" ")}")

    var bestTarget: Option[Position] = None
    var bestDepth = Int.MaxValue
    for (target <- targets) {
      val tmpDepth = depthLab(target.x)(target.y)
      if (tmpDepth < bestDepth) {
        bestDepth = tmpDepth
        bestTarget = Some(target)
      }
    }
    //Console.err.println(s"bestTarget : ${System.currentTimeMillis() - timer} ms")
    bestTarget
  }
}

object Direction {

  sealed abstract class Value(_x: Int, _y: Int) {
    val x = _x
    val y = _y
  }

  case object UP extends Value(0, -1)

  case object RIGHT extends Value(1, 0)

  case object DOWN extends Value(0, 1)

  case object LEFT extends Value(-1, 0)

  val values = Seq(UP, RIGHT, DOWN, LEFT)

  def opposite(value: Value): Value = {
    value match {
      case UP => DOWN
      case RIGHT => LEFT
      case DOWN => UP
      case LEFT => RIGHT
    }
  }
}

object Labyrinth {
  def print(lab: Array[Array[Int]]) {
    val width = lab.length
    val height = lab(0).length
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        Console.err.print("%02d ".format(lab(x)(y)))
      }
      Console.err.println()
    }
  }

  def print(lab: Array[Array[Char]]) {
    val width = lab.length
    val height = lab(0).length
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        Console.err.print(lab(x)(y))
      }
      Console.err.println()
    }
  }
}