package acal_lab04.Lab

import chisel3.iotesters.{Driver, PeekPokeTester}

class RCAdderTest(dut: RCAdder)
    extends PeekPokeTester(dut) {

  val in1 = Array(5, 32, 1, 77, 34, 55, 12)
  val in2 =
    Array(3456, 89489, 78, 5216, 4744, 8, 321)

  // in1.zip(in2)
  (in1 zip in2).foreach { case (i, j) =>
    poke(dut.io.in1, i)
    poke(dut.io.in2, j)
    expect(dut.io.sum, i + j)
    step(1)
  }

  // for(i <- in1){
  //     for(j <- in2){
  //         poke(dut.io.In1,i)
  //         poke(dut.io.In2,j)
  //         expect(dut.io.Sum,i+j)
  //     }
  // }

  println("RCAdder test completed!!!!!")
}

object RCAdderTest extends App {
  Driver.execute(args, () => new RCAdder(32)) { c =>
    new RCAdderTest(c)
  }
}
