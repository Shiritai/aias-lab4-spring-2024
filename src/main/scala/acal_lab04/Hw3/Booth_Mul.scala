package acal_lab04.Hw3

import chisel3._
import chisel3.util._

/**
 * An multiplication that can multiply a binary with
 * [-2, -1, 0, 1, 2]
 *
 * For multiplier, we define it as:
 * ```scala
 * [ // value: "by" argument
 *   0: 0
 *   1: 1
 *   2: 2
 *   -1: 3
 *   -2: 4
 * ]
 * ```
 */
class MultiplyWithinTwo(width: Int) extends Module {

  val io = IO(new Bundle {
    val num = Input(UInt(width.W))
    val by = Input(UInt(3.W))
    val res = Output(SInt((width + 1).W))
  })

  val num = io.num.asSInt()

  io.res := MuxLookup(io.by,
                      0.S,
                      Seq(
                        0.U -> 0.S,
                        1.U -> num,
                        2.U -> (num << 1),
                        3.U -> -num,
                        4.U -> ((-num) << 1)
                      ))
}

object MultiplyWithinTwo {
  def apply(width: Int)(num: UInt,
                        by: UInt,
                        res: SInt) = {
    val m = Module(new MultiplyWithinTwo(width))
    m.io.num := num
    m.io.by := by
    res := m.io.res

    m
  }
}

/**
 * Partial result of Booth Multiplication
 */
class PartialBooth(width: Int) extends Module {

  val io = IO(new Bundle {
    val in1 = Input(UInt(width.W)) // Multiplicand
    val in2Window =
      Input(UInt(width.W)) // Multiplier
    val partialOut =
      Output(SInt((width + 1).W)) // product
  })

  val amp = MuxLookup( // amplitude
    io.in2Window,
    0.U,
    Seq(
      "b000".U -> 0.U,
      "b001".U -> 1.U,
      "b010".U -> 1.U,
      "b011".U -> 2.U,
      "b100".U -> 4.U, // 4 is -2
      "b101".U -> 3.U, // 3 is -1
      "b110".U -> 3.U, // 3 is -1
      "b111".U -> 0.U
    )
  )

  MultiplyWithinTwo(width = width)(num = io.in1,
                                   by = amp,
                                   res =
                                     io.partialOut)
}

object PartialBooth {
  def apply(width: Int)(in1: UInt,
                        in2Window: UInt,
                        partialOut: SInt) = {
    val pb = Module(new PartialBooth(width))
    pb.io.in1 := in1
    pb.io.in2Window := in2Window
    partialOut := pb.io.partialOut

    pb
  }
}

/**
 * Radix-4 Booth Multiplier
 */
class Booth_Mul(width: Int) extends Module {
  val runs = width / 2
  val outWidth = 2 * width

  val io = IO(new Bundle {
    val in1 = Input(UInt(width.W)) // Multiplicand
    val in2 = Input(UInt(width.W)) // Multiplier
    val out = Output(UInt(outWidth.W)) // product
  })

  // Generate dummy tailing zero for io.in2
  val in2 = io.in2 << 1

  val partials = Wire(
    Vec(runs + 1, SInt(outWidth.W)))

  partials(0) := 0.S

  for (i <- 0 until runs) {
    val window = in2(2 * i + 2, 2 * i)
    val partial = Wire(SInt((width + 1).W))
    val pb =
      PartialBooth(width)(io.in1, window, partial)

    partials(i + 1) := partials(i) +
      (partial << (2 * i))
  }

  io.out := partials(runs).asUInt()
}
