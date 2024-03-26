package acal_lab04.Lab

import chisel3.iotesters.{Driver, PeekPokeTester}

class CLAdderTest(dut: CLAdder)
    extends PeekPokeTester(dut) {
  for (i <- 0 to 15) {
    for (j <- 0 to 15) {
      poke(dut.io.in1, i)
      poke(dut.io.in2, j)
      if (
        peek(dut.io.cout) * 16 + peek(
          dut.io.sum) != (i + j)
      ) {
        println("Oh No!!")
      }
    }
  }
  println("CLAdder test completed!!!")
}

object CLAdderTest extends App {
  Driver.execute(args, () => new CLAdder()) { c =>
    new CLAdderTest(c)
  }
}
