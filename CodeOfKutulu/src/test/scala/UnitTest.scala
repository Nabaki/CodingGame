import org.scalatest.{Matchers, OptionValues, TryValues, WordSpec}

abstract class UnitTest
  extends WordSpec
    with Matchers
    with OptionValues
    with TryValues
