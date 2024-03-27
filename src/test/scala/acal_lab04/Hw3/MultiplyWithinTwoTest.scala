package acal_lab04.Hw3

import chisel3.iotesters.{PeekPokeTester, Driver}

class MultiplyWithinTwoTest(m: MultiplyWithinTwo)
    extends PeekPokeTester(m) {
  for (a <- 0 until 1 << 3) {
    for (b <- 0 until 5) {
      poke(m.io.num, a)
      poke(m.io.by, b)

      var res = a * (b match {
        case 0 => 0 
        case 1 => 1 
        case 2 => 2 
        case 3 => -1 
        case 4 => -2 
      })

      expect(m.io.res, res)
      step(1)
    }
  }
  println("MultiplyWithinTwo test completed!!!")
}

object MultiplyWithinTwoTest extends App {
  Driver.execute(
    args,
    () => new MultiplyWithinTwo(4)
  ) { c =>
    new MultiplyWithinTwoTest(c)
  }
}
