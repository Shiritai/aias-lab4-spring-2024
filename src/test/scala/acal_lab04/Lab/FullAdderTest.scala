package acal_lab04.Lab

import chisel3.iotesters.{PeekPokeTester, Driver}

class FullAdderTest(fa: FullAdder)
    extends PeekPokeTester(fa) {
  for (a <- 0 until 2) {
    for (b <- 0 until 2) {
      for (c <- 0 until 2) {
        poke(fa.io.in1, a)
        poke(fa.io.in2, b)
        poke(fa.io.cin, c)

        var x = c & (a ^ b)
        var y = a & b

        expect(fa.io.sum, (a ^ b ^ c))
        expect(fa.io.cout, (x | y))
        step(1)
      }
    }
  }
  println("FullAdder test completed!!!")
}

object FullAdderTest extends App {
  Driver.execute(
    args,
    () => new FullAdder()
  ) { c =>
    new FullAdderTest(c)
  }
}
