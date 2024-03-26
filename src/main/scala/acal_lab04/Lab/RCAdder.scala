package acal_lab04.Lab

import chisel3._

/**
 * Ripple carry adder
 */
class RCAdder(n: Int) extends Module {
  val io = IO(new Bundle {
    val cin = Input(UInt(1.W))
    val in1 = Input(UInt(n.W))
    val in2 = Input(UInt(n.W))
    val sum = Output(UInt(n.W))
    val cout = Output(UInt(1.W))
  })

  val carries = Wire(Vec(n + 1, UInt(1.W)))
  val sum = Wire(Vec(n, UInt(1.W)))

  carries(0) := io.cin

  for (i <- 0 until n) {
    FullAdder(cin = carries(i),
              in1 = io.in1(i),
              in2 = io.in2(i),
              sum = sum(i),
              cout = carries(i + 1))
  }

  io.sum := sum.asUInt
  io.cout := carries(n)
}

/**
 * Singleton to make RCAdder
 */
object RCAdder {
  def apply(n: Int) = {
    Module(new RCAdder(n))
  }
}
