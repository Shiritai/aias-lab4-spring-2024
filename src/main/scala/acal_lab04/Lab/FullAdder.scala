package acal_lab04.Lab

import chisel3._

class HalfAdder extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(1.W))
    val in2 = Input(UInt(1.W))
    val sum = Output(UInt(1.W))
    val carry = Output(UInt(1.W))
  })
  // the behavior of circuit
  io.sum := io.in1 ^ io.in2
  io.carry := io.in1 & io.in2
}

/**
 * Singleton to make HalfAdder
 */
object HalfAdder {
  def apply() = {
    Module(new HalfAdder())
  }
}

class FullAdder extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(1.W))
    val in2 = Input(UInt(1.W))
    val cin = Input(UInt(1.W))
    val sum = Output(UInt(1.W))
    val cout = Output(UInt(1.W))
  })

  val ha1 = HalfAdder()
  val ha2 = HalfAdder()
  
  // Wiring
  ha1.io.in1 := io.in1
  ha1.io.in2 := io.in2
  
  ha2.io.in1 := ha1.io.sum
  ha2.io.in2 := io.cin
  
  io.sum := ha2.io.sum
  io.cout := ha1.io.carry | ha2.io.carry
}

/**
 * Singleton to make FullAdder
 */
object FullAdder {
  /**
   * Module Instantiate factory
   *
   * Always wrap module with `Module` when you're
   * going to wire it
   */
  def apply() = {
    Module(new FullAdder())
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
    val fa = Module(new FullAdder())

    /**
     * Connect io ports p.s. `TO_BITS := FROM_BITS`
     */

    // inputs: from arguments to fa inputs
    fa.io.cin := cin
    fa.io.in1 := in1
    fa.io.in2 := in2
    // outputs: from fa outputs to arguments
    sum := fa.io.sum
    cout := fa.io.cout
    // return result
    fa
  }
}
