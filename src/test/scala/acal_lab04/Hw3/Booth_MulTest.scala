package acal_lab04.Hw3

import chisel3.iotesters.{Driver, PeekPokeTester}

class Booth_MulTest(dut: Booth_Mul)
    extends PeekPokeTester(dut) {
  var pass = 0
  for (a <- Seq(-32767, -1, 0, 1, 32767)) {
    for (b <- Seq(-32767, -1, 0, 1, 32767)) {
      poke(dut.io.in1, a)
      poke(dut.io.in2, b)
      if (peek(dut.io.out).toInt != a * b) {
        println(
          "Error : A: " + a + " B: " + b + " get " + peek(
            dut.io.out).toShort)
        pass = pass + 1
      }
      step(1)
    }
  }
  if (pass == 0)
    println("Booth_Mul test completed!!!!!")
  else
    println(
      s"Booth_Mul test failed...you have ${pass} errors to fix")
}

object Booth_MulTest extends App {
  Driver.execute(args, () => new Booth_Mul(16)) {
    c => new Booth_MulTest(c)
  }
}
