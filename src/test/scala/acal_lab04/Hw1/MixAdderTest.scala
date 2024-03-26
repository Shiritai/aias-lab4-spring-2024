package acal_lab04.Hw1

import chisel3.iotesters.{Driver, PeekPokeTester}

class MixAdderTest(dut: MixAdder) extends PeekPokeTester(dut) {
  var pass = 0
  for (i <- 0 until 1000) {
    val x = rnd.nextInt(1 << 30)
    val y = rnd.nextInt(1 << 30)

    poke(dut.io.in1, x)
    poke(dut.io.in2, y)
    if (!expect(dut.io.sum, x + y)) { pass = pass + 1 }
    step(1)
  }

  if (pass == 0) {
    println("MixAdder test completed!!!!!")
  } else {
    println(s"MixAdder test failed...you have ${pass} errors to fix")
  }

}

object MixAdderTest extends App {
  Driver.execute(args, () => new MixAdder(8)) { c =>
    new MixAdderTest(c)
  }
}
