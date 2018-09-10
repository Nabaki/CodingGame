import scala.collection.immutable.IndexedSeq

object Player extends App {

  private var phase = Phase.DRAFT
  var turn = 0
  var myDeck = Seq[Card]()

  // game loop
  while (true) {
    turn += 0
    val masters: IndexedSeq[CardMaster] = for (i <- 0 until 2) yield {
      val Array(playerhealth, playermana, playerdeck, playerrune) = for (i <- scala.io.StdIn.readLine split " ") yield i.toInt
      val cardMaster = CardMaster(playerhealth, playermana, playerdeck, playerrune)
      cardMaster
    }

    if (phase == Phase.DRAFT && masters.head.playerMana > 0) {
      phase = Phase.BATTLE

      myDeck
        .groupBy(_.cost).toSeq
        .sortBy(_._1)
        .foreach { costNCard =>
          Console.err.println(s"COST ${costNCard._1}")
          costNCard._2.foreach(card => Console.err.println(BaseCards.map(card.cardnumber)))
        }
      Console.err.println("---")
    }

    val opponenthand = scala.io.StdIn.readInt
    val cardcount = scala.io.StdIn.readInt
    val cards: IndexedSeq[Card] = for (i <- 0 until cardcount) yield {
      val Array(_cardnumber, _instanceid, _location, _cardtype, _cost, _attack, _defense, _abilities, _myhealthchange, _opponenthealthchange, _carddraw) = scala.io.StdIn.readLine split " "
      val cardnumber = _cardnumber.toInt
      val instanceid = _instanceid.toInt
      val location = Location.apply(_location.toInt)
      val cardtype = CardType.apply(_cardtype.toInt)
      val cost = _cost.toInt
      val attack = _attack.toInt
      val defense = _defense.toInt
      val abilities = Abilities.apply(_abilities, _myhealthchange.toInt, _opponenthealthchange.toInt, _carddraw.toInt)

      Card(cardnumber, instanceid, location, cardtype, cost, attack, defense, abilities)
    }

    // Write an action using println
    // To debug: Console.err.println("Debug messages...")
    phase match {
      case Phase.DRAFT =>
        val bestCard = CardMaster.draft(turn, myDeck, cards)
        myDeck = myDeck :+ bestCard._1
        println(s"${DraftCommand.PICK} ${bestCard._2}")
      case Phase.BATTLE => println(CardMaster.battle(masters.head, cards))
    }
  }
}

object DeckBuilding {
  val delta = 2
  val spellsExpected = 10

  //Pour 20 creatures
  val creaturesCurve = Map(
    0 -> 0,
    1 -> 2,
    2 -> 3,
    3 -> 4,
    4 -> 5,
    5 -> 3,
    6 -> 2,
    7 -> 1
  )

  val globalCurve = Map(
    0 -> 0,
    1 -> 3,
    2 -> 5,
    3 -> 6,
    4 -> 6,
    5 -> 5,
    6 -> 3,
    7 -> 2
  )
}

case class CardMaster(playerHealth: Int, playerMana: Int, playerDeck: Int, playerRune: Int)

object CardMaster {

  def draft(turn: Int, actualDeck: Seq[Card], cards: Seq[Card]): (Card, Int) = {

    val turnFactor = (10 + turn) / 10d
    Console.err.println(s"turnFactor $turnFactor")
    val pointsNid = cards
      .map { card =>
        val tmpCost = if (card.cost > 7) 7 else card.cost
        val actual = if (card.cardtype == CardType.CREATURE) actualDeck.count(_.cost == tmpCost) else actualDeck.count(_.cardtype != CardType.CREATURE)
        val expected = DeckBuilding.creaturesCurve(tmpCost)
        val res = if (card.cardtype == CardType.CREATURE) expected - actual else DeckBuilding.spellsExpected - actual
        BaseCards.map(card.cardnumber).points + (res * turnFactor).toInt
      }.zipWithIndex
      .sortBy(-_._1)

    pointsNid.foreach(pointNid => Console.err.println(s"${BaseCards.map(cards(pointNid._2).cardnumber)} -> ${pointNid._1}"))

    val bestId = pointsNid.head._2
    (cards(bestId), bestId)
  }

  def battle(cardMaster: CardMaster, _cards: Seq[Card]): String = {
    var tmpPlayerMana = cardMaster.playerMana
    val battleWords = StringBuilder.newBuilder
    var cards = _cards

    //CREATURE
    var hand: Seq[Card] = cards.filter(card => card.location == Location.MY_HAND && card.cardtype == CardType.CREATURE).sortBy(-_.cost)
    var cntMyCreatures = cards.count(card => card.location == Location.MY_BOARD)
    if (cntMyCreatures < 6) {
      do {
        hand = hand.filter(_.cost <= tmpPlayerMana)
        if (hand.nonEmpty) {
          val tmpHead = hand.head
          battleWords.append(s"${BattleCommand.SUMMON} ${tmpHead.instanceid};")
          tmpPlayerMana -= hand.head.cost
          hand = hand.drop(1)
          cntMyCreatures += 1
          if (tmpHead.abilities.charge) {
            cards = cards :+ tmpHead.copy(location = Location.MY_BOARD)
          }
        }
      } while (hand.nonEmpty && cntMyCreatures < 6)
    }

    //RED malus on creature
    //BLUE malus on creature (or player mais les points sont à 0 pour ce genre de carte)
    if (tmpPlayerMana > 0) {
      val ennemiCreatures = cards.filter(card => card.location == Location.ENNEMI_BOARD && (card.abilities.guard || card.abilities.lethal))
      val myMalus = cards.filter(card => (card.cardtype == CardType.RED || card.cardtype == CardType.BLUE) && card.cost <= tmpPlayerMana).sortBy(card => -Math.abs(card.defense))

      for (
        creature <- ennemiCreatures;
        malus <- myMalus
      ) {
        if (creature.defense + malus.defense <= 0 && tmpPlayerMana >= malus.cost) {
          battleWords.append(s"${BattleCommand.USE} ${malus.instanceid} ${creature.instanceid};")
          tmpPlayerMana -= malus.cost
        }
      }
    }

    //GREEN bonus on creature
    if (tmpPlayerMana > 0) {
      val myCreatureToBoost = cards.filter(card => card.location == Location.MY_BOARD).sortBy(card => -BaseCards.map(card.cardnumber).points).headOption
      val myBonus = cards.filter(card => card.cardtype == CardType.GREEN && card.cost <= tmpPlayerMana).sortBy(-_.cost).headOption
      myCreatureToBoost.foreach { creature =>
        myBonus.foreach { bonus =>
          battleWords.append(s"${BattleCommand.USE} ${bonus.instanceid} ${creature.instanceid};")
          tmpPlayerMana -= bonus.cost
        }
      }
    }

    //ATTACK
    //TODO A tester !
    var myCreatures = cards.filter(card => card.location == Location.MY_BOARD && card.attack > 0).sortBy(card => (card.abilities.lethal, card.attack))
    val ennemiGuards = cards.filter(card => card.location == Location.ENNEMI_BOARD && card.abilities.guard).sortBy(-_.defense)

    //WARD à focus en premier
    //Les plus faibles creatures détruisent le shield
    ennemiGuards.filter(_.abilities.ward).foreach { shieldGuard =>
      if (myCreatures.nonEmpty) {
        battleWords.append(s"${BattleCommand.ATTACK} ${myCreatures.head.instanceid} ${shieldGuard.instanceid};")
        myCreatures = myCreatures.drop(1)
      }
    }

    //GUARD
    //Les plus fortes creatures tuent les creatures adverses
    //FIXME On devrait trier dabord les lethal qui ont le moins d'attaque puis les non lethal qui ont le plus d'attaque
    myCreatures = myCreatures.reverse
    ennemiGuards.foreach { guard =>
      var tmpDef = guard.defense

      //On regarde si une creature peut être one shot
      val oneShots = myCreatures.filter(_.attack >= guard.defense).sortBy(_.attack)
      if(oneShots.nonEmpty){
        tmpDef = 0
        battleWords.append(s"${BattleCommand.ATTACK} ${oneShots.head.instanceid} ${guard.instanceid};")
        for(creatureNid <- myCreatures.zipWithIndex){
         if( creatureNid._1.instanceid == oneShots.head.instanceid){
           myCreatures = myCreatures.drop(creatureNid._2)
         }
        }
      }

      //Sinon on utilise le minimum de creatures possible
      while (tmpDef > 0 && myCreatures.nonEmpty) {
        tmpDef = if (myCreatures.head.abilities.lethal) 0 else tmpDef - myCreatures.head.attack
        battleWords.append(s"${BattleCommand.ATTACK} ${myCreatures.head.instanceid} ${guard.instanceid};")
        myCreatures = myCreatures.drop(1)
      }
    }

    myCreatures.foreach(card => battleWords.append(s"${BattleCommand.ATTACK} ${card.instanceid} -1;"))

    //RETURN
    if (battleWords.nonEmpty) {
      battleWords.toString()
    } else {
      BattleCommand.PASS.toString
    }
  }
}

object Phase extends Enumeration {
  val DRAFT, BATTLE = Value
}

object DraftCommand extends Enumeration {
  val PICK, PASS = Value
}

object BattleCommand extends Enumeration {
  val SUMMON, USE, ATTACK, PASS = Value
}

object CardType extends Enumeration {
  val CREATURE = Value(0)
  val GREEN = Value(1)
  val RED = Value(2)
  val BLUE = Value(3)
}

object Ability extends Enumeration {
  val BREAKTHROUGH = Value("B")
  val CHARGE = Value("C")
  val GUARD = Value("G")
  val DRAIN = Value("D")
  val LETHAL = Value("L")
  val WARD = Value("W")
}

object Location extends Enumeration {
  val MY_HAND = Value(0)
  val MY_BOARD = Value(1)
  val ENNEMI_BOARD = Value(-1)
}

case class Card(cardnumber: Int, instanceid: Int, location: Location.Value, cardtype: CardType.Value, cost: Int, attack: Int, defense: Int, abilities: Abilities)

case class Abilities(abilities: String, breakthrough: Boolean, charge: Boolean, guard: Boolean, drain: Boolean, lethal: Boolean, ward: Boolean, myhealthchange: Int, opponenthealthchange: Int, carddraw: Int) {
  override def toString: String = s"Abilities($abilities, $myhealthchange, $opponenthealthchange, $carddraw)"
}

object Abilities {
  def apply(abilities: String, myhealthchange: Int, opponenthealthchange: Int, carddraw: Int): Abilities = new Abilities(
    abilities,
    abilities.contains(Ability.BREAKTHROUGH.toString),
    abilities.contains(Ability.CHARGE.toString),
    abilities.contains(Ability.GUARD.toString),
    abilities.contains(Ability.DRAIN.toString),
    abilities.contains(Ability.LETHAL.toString),
    abilities.contains(Ability.WARD


      .toString),
    myhealthchange,
    opponenthealthchange,
    carddraw
  )

  def noAbilities(abilities: Abilities): Boolean = {
    !abilities.breakthrough && !abilities.charge && !abilities.guard && !abilities.drain && !abilities.lethal && !abilities.ward && abilities.myhealthchange == 0 && abilities.opponenthealthchange == 0 && abilities.carddraw == 0
  }
}

case class BaseCard(cardNumber: Int, name: String, cardType: CardType.Value, cost: Int, damage: Int, health: Int, abilities: Abilities, points: Int) {

  def this(cardNumber: Int, name: String, cardType: CardType.Value, cost: Int, damage: Int, health: Int, abilities: Abilities) = this(
    cardNumber,
    name,
    cardType,
    cost,
    damage,
    health,
    abilities,
    (Math.abs(damage) + Math.abs(health)) * 20 / cost
  )
}

class Potential extends Enumeration {
  val USELESS = Value(0)
  val WHY_NOT = Value(10)
  val WORTH = Value(20)
  val MEDIUM = Value(30)
  val GOOD = Value(40)
  val EXCELENT = Value(50)
  val MUST_HAVE = Value(60)
}

object Test extends App {

  //Creatures sans dégat
  /*BaseCards.map.toSeq
    .filter(idNCard => idNCard._2.cardType == CardType.CREATURE)
    .groupBy(_._2.cost).toSeq
    .sortBy(_._1)
    .foreach { costNidNCard =>
      val points = costNidNCard._2.map(_._2.points)
      println(s"COST ${costNidNCard._1} -> ${points.sum / points.length}")
    }*/

  println("------------------------------")

  //Classement des sorts les plus appréciés
  BaseCards.map.toSeq
    .filter(idNCard => idNCard._2.cardType == CardType.CREATURE && idNCard._2.cost == 1)
    //.filter(idNCard => idNCard._2.cardType == CardType.RED || idNCard._2.cardType == CardType.BLUE)
    //.filter(idNCard => idNCard._2.cardType == CardType.GREEN)
    //.filter(idNCard => idNCard._2.points >= 0 && idNCard._2.points < 50)
    .sortBy(idNCard => (idNCard._2.points, idNCard._2.cardNumber))
    .foreach(idNCard => println(s"${idNCard._2.points} -> ${idNCard._2}"))

  println("------------------------------")

  /*  val by = BaseCards.map.toSeq
      .filter(idNCard => idNCard._2.cardType == CardType.CREATURE && idNCard._2.cost != 0)
      .map(idNCard => (idNCard._2.damage + idNCard._2.health) * 20 / idNCard._2.cost)
    println(by.sum / by.length)*/
}

object BaseCards {
  val map = Map(
    1 -> new BaseCard(1, "Slimer", CardType.CREATURE, 1, 2, 1, Abilities("------", 1, 0, 0), 0),
    2 -> new BaseCard(2, "Scuttler", CardType.CREATURE, 1, 1, 2, Abilities("------", 0, -1, 0), 0),
    3 -> new BaseCard(3, "Beavrat", CardType.CREATURE, 1, 2, 2, Abilities("------", 0, 0, 0), 50),
    4 -> new BaseCard(4, "Plated Toad", CardType.CREATURE, 2, 1, 5, Abilities("------", 0, 0, 0)),
    5 -> new BaseCard(5, "Grime Gnasher", CardType.CREATURE, 2, 4, 1, Abilities("------", 0, 0, 0)),
    6 -> new BaseCard(6, "Murgling", CardType.CREATURE, 2, 3, 2, Abilities("------", 0, 0, 0)),
    7 -> new BaseCard(7, "Rootkin Sapling", CardType.CREATURE, 2, 2, 2, Abilities("-----W", 0, 0, 0)),
    8 -> new BaseCard(8, "Psyshroom", CardType.CREATURE, 2, 2, 3, Abilities("------", 0, 0, 0)),
    9 -> new BaseCard(9, "Corrupted Beavrat", CardType.CREATURE, 3, 3, 4, Abilities("------", 0, 0, 0), 40),
    10 -> new BaseCard(10, "Carnivorous Bush", CardType.CREATURE, 3, 3, 1, Abilities("--D---", 0, 0, 0), 20),
    11 -> new BaseCard(11, "Snowsaur", CardType.CREATURE, 3, 5, 2, Abilities("------", 0, 0, 0), 40),
    12 -> new BaseCard(12, "Woodshroom", CardType.CREATURE, 3, 2, 5, Abilities("------", 0, 0, 0), 40),
    13 -> new BaseCard(13, "Swamp Terror", CardType.CREATURE, 4, 5, 3, Abilities("------", 1, -1, 0)),
    14 -> new BaseCard(14, "Fanged Lunger", CardType.CREATURE, 4, 9, 1, Abilities("------", 0, 0, 0)),
    15 -> new BaseCard(15, "Pouncing Flailmouth", CardType.CREATURE, 4, 4, 5, Abilities("------", 0, 0, 0), 40),
    16 -> new BaseCard(16, "Wrangler Fish", CardType.CREATURE, 4, 6, 2, Abilities("------", 0, 0, 0)),
    17 -> new BaseCard(17, "Ash Walker", CardType.CREATURE, 4, 4, 5, Abilities("------", 0, 0, 0), 40),
    18 -> new BaseCard(18, "Acid Golem", CardType.CREATURE, 4, 7, 4, Abilities("------", 0, 0, 0), 30),
    19 -> new BaseCard(19, "Foulbeast", CardType.CREATURE, 5, 5, 6, Abilities("------", 0, 0, 0), 40),
    20 -> new BaseCard(20, "Hedge Demon", CardType.CREATURE, 5, 8, 2, Abilities("------", 0, 0, 0)),
    21 -> new BaseCard(21, "Crested Scuttler", CardType.CREATURE, 5, 6, 5, Abilities("------", 0, 0, 0), 40),
    22 -> new BaseCard(22, "Sigbovak", CardType.CREATURE, 6, 7, 5, Abilities("------", 0, 0, 0), 20),
    23 -> new BaseCard(23, "Titan Cave Hog", CardType.CREATURE, 7, 8, 8, Abilities("------", 0, 0, 0), 10),
    24 -> new BaseCard(24, "Exploding Skitterbug", CardType.CREATURE, 1, 1, 1, Abilities("------", 0, -1, 0), 10),
    25 -> new BaseCard(25, "Spiney Chompleaf", CardType.CREATURE, 2, 3, 1, Abilities("------", -2, -2, 0)),
    26 -> new BaseCard(26, "Razor Crab", CardType.CREATURE, 2, 3, 2, Abilities("------", 0, -1, 0)),
    27 -> new BaseCard(27, "Nut Gatherer", CardType.CREATURE, 2, 2, 2, Abilities("------", 2, 0, 0)),
    28 -> new BaseCard(28, "Infested Toad", CardType.CREATURE, 2, 1, 2, Abilities("------", 0, 0, 1)),
    29 -> new BaseCard(29, "Steelplume Nestling", CardType.CREATURE, 2, 2, 1, Abilities("------", 0, 0, 1)),
    30 -> new BaseCard(30, "Venomous Bog Hopper", CardType.CREATURE, 3, 4, 2, Abilities("------", 0, -2, 0)),
    31 -> new BaseCard(31, "Woodland Hunter", CardType.CREATURE, 3, 3, 1, Abilities("------", 0, -1, 0), 20),
    32 -> new BaseCard(32, "Sandsplat", CardType.CREATURE, 3, 3, 2, Abilities("------", 0, 0, 1), 30),
    33 -> new BaseCard(33, "Chameleskulk", CardType.CREATURE, 4, 4, 3, Abilities("------", 0, 0, 1), 30),
    34 -> new BaseCard(34, "Eldritch Cyclops", CardType.CREATURE, 5, 3, 5, Abilities("------", 0, 0, 1), 30),
    35 -> new BaseCard(35, "Snail - eyed Hulker", CardType.CREATURE, 6, 5, 2, Abilities("B-----", 0, 0, 1), 10),
    36 -> new BaseCard(36, "Possessed Skull", CardType.CREATURE, 6, 4, 4, Abilities("------", 0, 0, 2), 10),
    37 -> new BaseCard(37, "Eldritch Multiclops", CardType.CREATURE, 6, 5, 7, Abilities("------", 0, 0, 1), 20),
    38 -> new BaseCard(38, "Imp", CardType.CREATURE, 1, 1, 3, Abilities("--D---", 0, 0, 0), 40),
    39 -> new BaseCard(39, "Voracious Imp", CardType.CREATURE, 1, 2, 1, Abilities("--D---", 0, 0, 0), 20),
    40 -> new BaseCard(40, "Rock Gobbler", CardType.CREATURE, 3, 2, 3, Abilities("--DG--", 0, 0, 0), 30),
    41 -> new BaseCard(41, "Blizzard Demon", CardType.CREATURE, 3, 2, 2, Abilities("-CD---", 0, 0, 0), 20),
    42 -> new BaseCard(42, "Flying Leech", CardType.CREATURE, 4, 4, 2, Abilities("--D---", 0, 0, 0)),
    43 -> new BaseCard(43, "Screeching Nightmare", CardType.CREATURE, 6, 5, 5, Abilities("--D---", 0, 0, 0), 30),
    44 -> new BaseCard(44, "Deathstalker", CardType.CREATURE, 6, 3, 7, Abilities("--D-L-", 0, 0, 0), 40),
    45 -> new BaseCard(45, "Night Howler", CardType.CREATURE, 6, 6, 5, Abilities("B-D---", -3, 0, 0), 30),
    46 -> new BaseCard(46, "Soul Devourer", CardType.CREATURE, 9, 7, 7, Abilities("--D---", 0, 0, 0), 30),
    47 -> new BaseCard(47, "Gnipper", CardType.CREATURE, 2, 1, 5, Abilities("--D---", 0, 0, 0)),
    48 -> new BaseCard(48, "Venom Hedgehog", CardType.CREATURE, 1, 1, 1, Abilities("----L-", 0, 0, 0), 30),
    49 -> new BaseCard(49, "Shiny Prowler", CardType.CREATURE, 2, 1, 2, Abilities("---GL-", 0, 0, 0)),
    50 -> new BaseCard(50, "Puff Biter", CardType.CREATURE, 3, 3, 2, Abilities("----L-", 0, 0, 0), 20),
    51 -> new BaseCard(51, "Elite Bilespitter", CardType.CREATURE, 4, 3, 5, Abilities("----L-", 0, 0, 0)),
    52 -> new BaseCard(52, "Bilespitter", CardType.CREATURE, 4, 2, 4, Abilities("----L-", 0, 0, 0)),
    53 -> new BaseCard(53, "Possessed Abomination", CardType.CREATURE, 4, 1, 1, Abilities("-C--L-", 0, 0, 0), 60),
    54 -> new BaseCard(54, "Shadow Biter", CardType.CREATURE, 3, 2, 2, Abilities("----L-", 0, 0, 0), 30),
    55 -> new BaseCard(55, "Hermit Slime", CardType.CREATURE, 2, 0, 5, Abilities("---G--", 0, 0, 0), 0),
    56 -> new BaseCard(56, "Giant Louse", CardType.CREATURE, 4, 2, 7, Abilities("------", 0, 0, 0), 40),
    57 -> new BaseCard(57, "Dream-Eater", CardType.CREATURE, 4, 1, 8, Abilities("------", 0, 0, 0), 40),
    58 -> new BaseCard(58, "Darkscale Predator", CardType.CREATURE, 6, 5, 6, Abilities("B-----", 0, 0, 0), 30),
    59 -> new BaseCard(59, "Sea Ghost", CardType.CREATURE, 7, 7, 7, Abilities("------", 1, -1, 0)),
    60 -> new BaseCard(60, "Gritsuck Troll", CardType.CREATURE, 7, 4, 8, Abilities("------", 0, 0, 0), 20),
    61 -> new BaseCard(61, "Alpha Troll", CardType.CREATURE, 9, 10, 10, Abilities("------", 0, 0, 0), 30),
    62 -> new BaseCard(62, "Mutant Troll", CardType.CREATURE, 12, 12, 12, Abilities("B--G--", 0, 0, 0), 0),
    63 -> new BaseCard(63, "Rootkin Drone", CardType.CREATURE, 2, 0, 4, Abilities("---G-W", 0, 0, 0)),
    64 -> new BaseCard(64, "Coppershell Tortoise", CardType.CREATURE, 2, 1, 1, Abilities("---G-W", 0, 0, 0)),
    65 -> new BaseCard(65, "Steelplume Defender", CardType.CREATURE, 2, 2, 2, Abilities("-----W", 0, 0, 0)),
    66 -> new BaseCard(66, "Staring Wickerbeast", CardType.CREATURE, 5, 5, 1, Abilities("-----W", 0, 0, 0), 20),
    67 -> new BaseCard(67, "Flailing Hammerhead", CardType.CREATURE, 6, 5, 5, Abilities("-----W", 0, -2, 0), 10),
    68 -> new BaseCard(68, "Giant Squid", CardType.CREATURE, 6, 7, 5, Abilities("-----W", 0, 0, 0)),
    69 -> new BaseCard(69, "Charging Boarhound", CardType.CREATURE, 3, 4, 4, Abilities("B-----", 0, 0, 0), 50),
    70 -> new BaseCard(70, "Murglord", CardType.CREATURE, 4, 6, 3, Abilities("B-----", 0, 0, 0), 40),
    71 -> new BaseCard(71, "Flying Murgling", CardType.CREATURE, 4, 3, 2, Abilities("BC----", 0, 0, 0), 20),
    72 -> new BaseCard(72, "Shuffling Nightmare", CardType.CREATURE, 4, 5, 3, Abilities("B-----", 0, 0, 0)),
    73 -> new BaseCard(73, "Bog Bounder", CardType.CREATURE, 4, 4, 4, Abilities("B-----", 4, 0, 0)),
    74 -> new BaseCard(74, "Crusher", CardType.CREATURE, 5, 5, 4, Abilities("B--G--", 0, 0, 0), 30),
    75 -> new BaseCard(75, "Titan Prowler", CardType.CREATURE, 5, 6, 5, Abilities("B-----", 0, 0, 0), 40),
    76 -> new BaseCard(76, "Crested Chomper", CardType.CREATURE, 6, 5, 5, Abilities("B-D---", 0, 0, 0), 30),
    77 -> new BaseCard(77, "Lumbering Giant", CardType.CREATURE, 7, 7, 7, Abilities("B-----", 0, 0, 0), 30),
    78 -> new BaseCard(78, "Shambler", CardType.CREATURE, 8, 5, 5, Abilities("B-----", 0, -5, 0), 0),
    79 -> new BaseCard(79, "Scarlet Colossus", CardType.CREATURE, 8, 8, 8, Abilities("B-----", 0, 0, 0), 30),
    80 -> new BaseCard(80, "Corpse Guzzler", CardType.CREATURE, 8, 8, 8, Abilities("B--G--", 0, 0, 1)),
    81 -> new BaseCard(81, "Flying Corpse Guzzler", CardType.CREATURE, 9, 6, 6, Abilities("BC----", 0, 0, 0), 10),
    82 -> new BaseCard(82, "Slithering Nightmare", CardType.CREATURE, 7, 5, 5, Abilities("B-D--W", 0, 0, 0), 30),
    83 -> new BaseCard(83, "Restless Owl", CardType.CREATURE, 0, 1, 1, Abilities("-C----", 0, 0, 0), 0),
    84 -> new BaseCard(84, "Fighter Tick", CardType.CREATURE, 2, 1, 1, Abilities("-CD--W", 0, 0, 0)),
    85 -> new BaseCard(85, "Heartless Crow", CardType.CREATURE, 3, 2, 3, Abilities("-C----", 0, 0, 0), 30),
    86 -> new BaseCard(86, "Crazed Nose-pincher", CardType.CREATURE, 3, 1, 5, Abilities("-C----", 0, 0, 0)),
    87 -> new BaseCard(87, "Bloat Demon", CardType.CREATURE, 4, 2, 5, Abilities("-C-G--", 0, 0, 0), 30),
    88 -> new BaseCard(88, "Abyss Nightmare", CardType.CREATURE, 5, 4, 4, Abilities("-C----", 0, 0, 0), 30),
    89 -> new BaseCard(89, "Boombeak", CardType.CREATURE, 5, 4, 1, Abilities("-C----", 2, 0, 0), 40),
    90 -> new BaseCard(90, "Eldritch Swooper", CardType.CREATURE, 8, 5, 5, Abilities("-C----", 0, 0, 0), 20),
    91 -> new BaseCard(91, "Flumpy", CardType.CREATURE, 0, 1, 2, Abilities("---G--", 0, 1, 0), 40),
    92 -> new BaseCard(92, "Wurm", CardType.CREATURE, 1, 0, 1, Abilities("---G--", 2, 0, 0)),
    93 -> new BaseCard(93, "Spinekid", CardType.CREATURE, 1, 2, 1, Abilities("---G--", 0, 0, 0), 0),
    94 -> new BaseCard(94, "Rootkin Defender", CardType.CREATURE, 2, 1, 4, Abilities("---G--", 0, 0, 0), 20),
    95 -> new BaseCard(95, "Wildum", CardType.CREATURE, 2, 2, 3, Abilities("---G--", 0, 0, 0), 30),
    96 -> new BaseCard(96, "Prairie Protector", CardType.CREATURE, 2, 3, 2, Abilities("---G--", 0, 0, 0), 10),
    97 -> new BaseCard(97, "Turta", CardType.CREATURE, 3, 3, 3, Abilities("---G--", 0, 0, 0)),
    98 -> new BaseCard(98, "Lilly Hopper", CardType.CREATURE, 3, 2, 4, Abilities("---G--", 0, 0, 0)),
    99 -> new BaseCard(99, "Cave Crab", CardType.CREATURE, 3, 2, 5, Abilities("---G--", 0, 0, 0), 40),
    100 -> new BaseCard(100, "Stalagopod", CardType.CREATURE, 3, 1, 6, Abilities("---G--", 0, 0, 0), 40),
    101 -> new BaseCard(101, "Engulfer", CardType.CREATURE, 4, 3, 4, Abilities("---G--", 0, 0, 0), 30),
    102 -> new BaseCard(102, "Mole Demon", CardType.CREATURE, 4, 3, 3, Abilities("---G--", 0, -1, 0)),
    103 -> new BaseCard(103, "Mutating Rootkin", CardType.CREATURE, 4, 3, 6, Abilities("---G--", 0, 0, 0), 40),
    104 -> new BaseCard(104, "Deepwater Shellcrab", CardType.CREATURE, 4, 4, 4, Abilities("---G--", 0, 0, 0)),
    105 -> new BaseCard(105, "King Shellcrab", CardType.CREATURE, 5, 4, 6, Abilities("---G--", 0, 0, 0)),
    106 -> new BaseCard(106, "Far-reaching Nightmare", CardType.CREATURE, 5, 5, 5, Abilities("---G--", 0, 0, 0)),
    107 -> new BaseCard(107, "Worker Shellcrab", CardType.CREATURE, 5, 3, 3, Abilities("---G--", 3, 0, 0), 20),
    108 -> new BaseCard(108, "Rootkin Elder", CardType.CREATURE, 5, 2, 6, Abilities("---G--", 0, 0, 0), 30),
    109 -> new BaseCard(109, "Elder Engulfer", CardType.CREATURE, 5, 5, 6, Abilities("------", 0, 0, 0), 40),
    110 -> new BaseCard(110, "Gargoyle", CardType.CREATURE, 5, 0, 9, Abilities("---G--", 0, 0, 0), 30),
    111 -> new BaseCard(111, "Turta Knight", CardType.CREATURE, 6, 6, 6, Abilities("---G--", 0, 0, 0)),
    112 -> new BaseCard(112, "Rootkin Leader", CardType.CREATURE, 6, 4, 7, Abilities("---G--", 0, 0, 0), 40),
    113 -> new BaseCard(113, "Tamed Bilespitter", CardType.CREATURE, 6, 2, 4, Abilities("---G--", 4, 0, 0)),
    114 -> new BaseCard(114, "Gargantua", CardType.CREATURE, 7, 7, 7, Abilities("---G--", 0, 0, 0), 40),
    115 -> new BaseCard(115, "Rootkin Warchief", CardType.CREATURE, 8, 5, 5, Abilities("---G-W", 0, 0, 0), 30),
    116 -> new BaseCard(116, "Emperor Nightmare", CardType.CREATURE, 12, 8, 8, Abilities("BCDGLW", 0, 0, 0), 0),
    //id 136 do better
    117 -> new BaseCard(117, "Protein", CardType.GREEN, 1, 1, 1, Abilities("B-----", 0, 0, 0), 0),
    118 -> new BaseCard(118, "Royal Helm", CardType.GREEN, 0, 0, 3, Abilities("------", 0, 0, 0), 10),
    119 -> new BaseCard(119, "Serrated Shield", CardType.GREEN, 1, 1, 2, Abilities("------", 0, 0, 0), 20),
    120 -> new BaseCard(120, "Venomfruit", CardType.GREEN, 2, 1, 0, Abilities("----L-", 0, 0, 0), 40),
    121 -> new BaseCard(121, "Enchanted Hat", CardType.GREEN, 2, 0, 3, Abilities("------", 0, 0, 1), 40),
    122 -> new BaseCard(122, "Bolstering Bread", CardType.GREEN, 2, 1, 3, Abilities("---G--", 0, 0, 0), 40),
    123 -> new BaseCard(123, "Wristguards", CardType.GREEN, 2, 4, 0, Abilities("------", 0, 0, 0), 10),
    124 -> new BaseCard(124, "Blood Grapes", CardType.GREEN, 3, 2, 1, Abilities("--D---", 0, 0, 0), 40),
    125 -> new BaseCard(125, "Healthy Veggies", CardType.GREEN, 3, 1, 4, Abilities("------", 0, 0, 0), 30),
    126 -> new BaseCard(126, "Heavy Shield", CardType.GREEN, 3, 2, 3, Abilities("------", 0, 0, 0), 30),
    127 -> new BaseCard(127, "Imperial Helm", CardType.GREEN, 3, 0, 6, Abilities("------", 0, 0, 0), 10),
    128 -> new BaseCard(128, "Enchanted Cloth", CardType.GREEN, 4, 4, 3, Abilities("------", 0, 0, 0), 30),
    129 -> new BaseCard(129, "Enchanted Leather", CardType.GREEN, 4, 2, 5, Abilities("------", 0, 0, 0), 40),
    130 -> new BaseCard(130, "Helm of Remedy", CardType.GREEN, 4, 0, 6, Abilities("------", 4, 0, 0), 10),
    131 -> new BaseCard(131, "Heavy Gauntlet", CardType.GREEN, 4, 4, 1, Abilities("------", 0, 0, 0), 10),
    132 -> new BaseCard(132, "High Protein", CardType.GREEN, 5, 3, 3, Abilities("B-----", 0, 0, 0), 20),
    133 -> new BaseCard(133, "Pie of Power", CardType.GREEN, 5, 4, 0, Abilities("-----W", 0, 0, 0), 40),
    134 -> new BaseCard(134, "Light The Way", CardType.GREEN, 4, 2, 2, Abilities("------", 0, 0, 1), 10),
    135 -> new BaseCard(135, "Imperial Armour", CardType.GREEN, 6, 5, 5, Abilities("------", 0, 0, 0), 30),
    136 -> new BaseCard(136, "Buckler", CardType.GREEN, 0, 1, 1, Abilities("------", 0, 0, 0), 20),
    137 -> new BaseCard(137, "Ward", CardType.GREEN, 2, 0, 0, Abilities("-----W", 0, 0, 0), 40),
    138 -> new BaseCard(138, "Grow Horns", CardType.GREEN, 2, 0, 0, Abilities("---G--", 0, 0, 1), 10),
    139 -> new BaseCard(139, "Grow Stingers", CardType.GREEN, 4, 0, 0, Abilities("----LW", 0, 0, 0), 40),
    140 -> new BaseCard(140, "Grow Wings", CardType.GREEN, 2, 0, 0, Abilities("-C----", 0, 0, 0), 0),
    141 -> new BaseCard(141, "Throwing Knife", CardType.RED, 0, -1, -1, Abilities("------", 0, 0, 0), 10),
    142 -> new BaseCard(142, "Staff of Suppression", CardType.RED, 0, 0, 0, Abilities("BCDGLW", 0, 0, 0), 0),
    143 -> new BaseCard(143, "Pierce Armour", CardType.RED, 0, 0, 0, Abilities("---G--", 0, 0, 0), 0),
    144 -> new BaseCard(144, "Rune Axe", CardType.RED, 1, 0, -2, Abilities("------", 0, 0, 0), 50),
    145 -> new BaseCard(145, "Cursed Sword", CardType.RED, 3, -2, -2, Abilities("------", 0, 0, 0), 40),
    146 -> new BaseCard(146, "Cursed Scimitar", CardType.RED, 4, -2, -2, Abilities("------", 0, -2, 0), 20),
    147 -> new BaseCard(147, "Quick Shot", CardType.RED, 2, 0, -1, Abilities("------", 0, 0, 1), 20),
    148 -> new BaseCard(148, "Helm Crusher", CardType.RED, 2, 0, -2, Abilities("BCDGLW", 0, 0, 0), 40),
    149 -> new BaseCard(149, "Rootkin Ritual", CardType.RED, 3, 0, 0, Abilities("BCDGLW", 0, 0, 1), 10),
    150 -> new BaseCard(150, "Throwing Axe", CardType.RED, 2, 0, -3, Abilities("------", 0, 0, 0), 50),
    151 -> new BaseCard(151, "Decimate", CardType.RED, 5, 0, -99, Abilities("BCDGLW", 0, 0, 0), 60),
    152 -> new BaseCard(152, "Mighty Throwing Axe", CardType.RED, 7, 0, -7, Abilities("------", 0, 0, 1), 20),
    153 -> new BaseCard(153, "Healing Potion", CardType.BLUE, 2, 0, 0, Abilities("------", 5, 0, 0), 0),
    154 -> new BaseCard(154, "Poison", CardType.BLUE, 2, 0, 0, Abilities("------", 0, -2, 1), 0),
    155 -> new BaseCard(155, "Scroll of Firebolt", CardType.BLUE, 3, 0, -3, Abilities("------", 0, -1, 0), 30),
    156 -> new BaseCard(156, "Major Life Steal Potion", CardType.BLUE, 3, 0, 0, Abilities("------", 3, -3, 0), 0),
    157 -> new BaseCard(157, "Life Sap Drop", CardType.BLUE, 3, 0, -1, Abilities("------", 1, 0, 1), 10),
    158 -> new BaseCard(158, "Tome of Thunder", CardType.BLUE, 3, 0, -4, Abilities("------", 0, 0, 0), 40),
    159 -> new BaseCard(159, "Vial of Soul Drain", CardType.BLUE, 4, 0, -3, Abilities("------", 3, 0, 0), 30),
    160 -> new BaseCard(160, "Minor Life Steal Potion", CardType.BLUE, 2, 0, 0, Abilities("------", 2, -2, 0), 0)
  )
}