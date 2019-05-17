import scala.collection.immutable.IndexedSeq

/**
  * Auto-generated code below aims at helping you parse
  * the standard input according to the problem statement.
  **/
object Player extends App {

  var cardList = Seq()
  var phase = Phase.DRAFT

  // game loop
  while (true) {
    val initializeTimer = System.currentTimeMillis()
    val masters: IndexedSeq[CardMaster] = for (i <- 0 until 2) yield {
      val Array(playerhealth, playermana, playerdeck, playerrune) = for (i <- scala.io.StdIn.readLine split " ") yield i.toInt
      val cardMaster = CardMaster(playerhealth, playermana, playerdeck, playerrune)
      Console.err.println(cardMaster.toString)
      cardMaster
    }

    if (phase == Phase.DRAFT && masters.head.playerDeck > 0) {
      phase = Phase.BATTLE
    }

    val opponenthand = scala.io.StdIn.readInt
    val cardcount = scala.io.StdIn.readInt
    //Console.err.println(s"cardcount : $cardcount")
    val cards: IndexedSeq[Card] = for (i <- 0 until cardcount) yield {
      val Array(_cardnumber, _instanceid, _location, _cardtype, _cost, _attack, _defense, abilities, _myhealthchange, _opponenthealthchange, _carddraw) = scala.io.StdIn.readLine split " "
      val cardnumber = _cardnumber.toInt
      val instanceid = _instanceid.toInt
      /**
        * 0 : dans la main du joueur actif
        * 1 : sur le plateau de jeu, du côté du joueur actif
        * -1 : sur le plateau de jeu, du côté de son adversaire
        **/
      val location = _location.toInt
      val cardtype = _cardtype.toInt
      val cost = _cost.toInt
      val attack = _attack.toInt
      val defense = _defense.toInt
      val myhealthchange = _myhealthchange.toInt
      val opponenthealthchange = _opponenthealthchange.toInt
      val carddraw = _carddraw.toInt

      Card(cardnumber, instanceid, location, cardtype, cost, attack, defense, abilities, myhealthchange, opponenthealthchange, carddraw)
    }
    //Console.err.println(s"Initialisation : ${System.currentTimeMillis() - initializeTimer} ms")
    val porcessTimer = System.currentTimeMillis()
    Console.err.println(cards.mkString("\n"))


    // Write an action using println
    // To debug: Console.err.println("Debug messages...")

    phase match {
      case Phase.DRAFT => println(s"${DraftCommand.PICK} ${CardMaster.draft(cards(0), cards(1), cards(2))}")
      case Phase.BATTLE => println(s"${CardMaster.battle(masters.head, cards)}")
    }
    //Console.err.println(s"porcessTimer : ${System.currentTimeMillis() - porcessTimer} ms")
  }
}

case class CardMaster(playerHealth: Int, playerMana: Int, playerDeck: Int, playerRune: Int)

object CardMaster {

  def draft(card1: Card, card2: Card, card3: Card): Int = {
    val card1Points = (card1.attack + card1.defense) / card1.cost.toDouble
    val card2Points = (card2.attack + card2.defense) / card2.cost.toDouble
    val card3Points = (card3.attack + card3.defense) / card3.cost.toDouble

    if (card1Points > card2Points && card1Points > card3Points) {
      0
    } else if (card2Points > card3Points) {
      1
    } else {
      2
    }
  }

  def battle(cardMaster: CardMaster, cards: Seq[Card]): String = {
    val battleTimer = System.currentTimeMillis()
    var tmpPlayerMana = cardMaster.playerMana
    val battleWords = StringBuilder.newBuilder

    //SUMMON
    var hand: Seq[Card] = cards.filter(_.location == 0).sortWith(_.cost > _.cost)
    do {
      //Console.err.println(s"while")
      hand = hand.filter(_.cost <= tmpPlayerMana)
      //Console.err.println(s"$hand")
      if (hand.nonEmpty) {
        battleWords.append(s"${BatailleCommand.SUMMON} ${hand.head.instanceid};")
        tmpPlayerMana -= hand.head.cost
        hand = hand.drop(1)
      }
      //Console.err.println(s"$tmpPlayerMana")
    } while (hand.nonEmpty)
    //Console.err.println(s"summon : ${System.currentTimeMillis() - battleTimer} ms")

    //ATTACK
    val guards = cards.filter(card => card.location == -1 && card.abilities.contains("G")).sortWith(_.defense > _.defense)
    var myCreatures = cards.filter(_.location == 1).sortWith(_.attack > _.attack)

    guards.foreach { guard =>
      var tmpDef = guard.defense
      while (tmpDef > 0 && myCreatures.nonEmpty) {
        tmpDef -= myCreatures.head.attack
        battleWords.append(s"${BatailleCommand.ATTACK} ${myCreatures.head.instanceid} ${guard.instanceid};")
        myCreatures = myCreatures.drop(1)
      }
    }

    myCreatures.foreach(card => battleWords.append(s"${BatailleCommand.ATTACK} ${card.instanceid} -1;"))
    //Console.err.println(s"attack : ${System.currentTimeMillis() - battleTimer} ms")

    //RETURN
    if (battleWords.nonEmpty) {
      battleWords.toString()
    } else {
      BatailleCommand.PASS.toString
    }
  }
}

object Phase extends Enumeration {
  val DRAFT, BATTLE = Value
}

object DraftCommand extends Enumeration {
  val PICK, PASS = Value
}

object BatailleCommand extends Enumeration {
  val SUMMON, ATTACK, PASS = Value
}

object CardType extends Enumeration {
  val CREATURE = Value
}

case class Card(cardnumber: Int, instanceid: Int, location: Int, cardtype: Int, cost: Int, attack: Int, defense: Int, abilities: String, myhealthchange: Int, opponenthealthchange: Int, carddraw: Int)