package acal_lab04.Lab

import chisel3._

/**
 * Carry look ahead adder
 */
class CLAdder extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(4.W))
    val in2 = Input(UInt(4.W))
    val cin = Input(UInt(1.W))
    val sum = Output(UInt(4.W))
    val cout = Output(UInt(1.W))
  })

  val P = Wire(Vec(4, UInt()))
  val G = Wire(Vec(4, UInt()))
  val C = Wire(Vec(4, UInt()))
  val S = Wire(Vec(4, UInt()))

  for (i <- 0 until 4) {
    G(i) := io.in1(i) & io.in2(i)
    P(i) := io.in1(i) | io.in2(i)
  }

  C(0) := io.cin
  C(1) := G(0) | (P(0) & C(0))
  C(2) := G(1) |
    (P(1) & G(0)) |
    (P(1) & P(0) & C(0))
  C(3) := G(2) |
    (P(2) & G(1)) |
    (P(2) & P(1) & G(0)) |
    (P(2) & P(1) & P(0) & C(0))

  val couts = Wire(Vec(4, UInt(1.W)))

  for (i <- 0 until 4) {
    FullAdder(cin = C(i),
              in1 = io.in1(i),
              in2 = io.in2(i),
              sum = S(i),
              cout = couts(i))
  }

  io.sum := S.asUInt
  io.cout := couts(3)
}

/**
 * Singleton to make CLAdder (CLAdder factory)
 */
object CLAdder {
  def apply() = {
    Module(new CLAdder())
  }

  /**
   * Make a CLAdder from IO ports
   *
   * This method will wrap CLAdder with `Module`
   * undoubtedly in order to assign the ports
   */
  def apply(cin: UInt,
            in1: UInt,
            in2: UInt,
            sum: UInt,
            cout: UInt) = {

    /**
     * Always wrap instantiated module with `Module`
     * if we're going to manipulate it to assure
     * chisel trace IO decently.
     */
    val cla = Module(new CLAdder())

    /**
     * Connect io ports p.s. `TO_BITS := FROM_BITS`
     */

    // inputs: from arguments to cla inputs
    cla.io.cin := cin
    cla.io.in1 := in1
    cla.io.in2 := in2
    // outputs: from cla outputs to arguments
    sum := cla.io.sum
    cout := cla.io.cout
    // return result
    cla
  }
}
