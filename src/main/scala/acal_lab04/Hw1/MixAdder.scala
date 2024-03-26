package acal_lab04.Hw1

import chisel3._
import acal_lab04.Lab._

class MixAdder(n: Int) extends Module {

  val batchSize = 4

  /**
   * Get width of `CLAdder`, i.e. `batchSize * n`
   */
  def batch(n: Int) = batchSize * n

  val io = IO(new Bundle {
    val cin = Input(UInt(1.W))
    val in1 = Input(UInt(batch(n).W))
    val in2 = Input(UInt(batch(n).W))
    val sum = Output(UInt(batch(n).W))
    val cout = Output(UInt(1.W))
  })

  val carry = Wire(Vec(n + 1, UInt(1.W)))
  val sum = Wire(Vec(n, UInt(batchSize.W)))

  carry(0) := io.cin

  for (i <- 0 until n) {

    /**
     * Use `UInt.apply(high, low)` to slice a Uint
     * works **inclusively**.
     *
     * Ref: source code at `chisel3/Bits.scala`
     */
    CLAdder(
      cin = carry(i),
      in1 = io.in1(batch(i + 1) - 1, batch(i)),
      in2 = io.in2(batch(i + 1) - 1, batch(i)),
      sum = sum(i),
      cout = carry(i + 1))
  }

  io.sum := sum.asUInt
  io.cout := carry(n)
}

/**
 * Singleton to make MixAdder
 */
object MixAdder {
  def apply(n: Int) = {
    Module(new MixAdder(n))
  }
}
