/**
  * Survive the wrath of Kutulu
  * Coded fearlessly by JohnnyYuge & nmahoude (ok we might have been a bit scared by the old god...but don't say anything)
  **/
object Player extends App {
  val width = readInt
  val height = readInt
  for (i <- 0 until height) {
    val line = readLine
  }
  // sanitylosslonely: how much sanity you lose every turn when alone, always 3 until wood 1
  // sanitylossgroup: how much sanity you lose every turn when near another player, always 1 until wood 1
  // wandererspawntime: how many turns the wanderer take to spawn, always 3 until wood 1
  // wandererlifetime: how many turns the wanderer is on map after spawning, always 40 until wood 1
  val Array(sanitylosslonely, sanitylossgroup, wandererspawntime, wandererlifetime) = for (i <- readLine split " ") yield i.toInt

  // game loop
  while (true) {
    val entitycount = readInt // the first given entity corresponds to your explorer
    for (i <- 0 until entitycount) {
      val Array(entitytype, _id, _x, _y, _param0, _param1, _param2) = readLine split " "
      val id = _id.toInt
      val x = _x.toInt
      val y = _y.toInt
      val param0 = _param0.toInt
      val param1 = _param1.toInt
      val param2 = _param2.toInt
    }

    // Write an action using println
    // To debug: Console.err.println("Debug messages...")

    println("WAIT") // MOVE <x> <y> | WAIT
  }
}