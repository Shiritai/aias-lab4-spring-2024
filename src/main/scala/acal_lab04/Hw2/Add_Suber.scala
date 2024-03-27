package acal_lab04.Hw2

import chisel3._
import chisel3.util._
import acal_lab04.Lab._

/**
 * Generalized twos complement add-subtractor
 */
class Add_Suber(n: Int = 4) extends Module {
  val io = IO(new Bundle {
    val in_1 = Input(UInt(n.W))
    val in_2 = Input(UInt(n.W))
    val op = Input(Bool()) // 0: ADD 1: SUB
    val out = Output(UInt(n.W))
    val o_f = Output(Bool())
  })

  val carries = Wire(Vec(n + 1, UInt(1.W)))
  val sums = Wire(Vec(n, UInt(1.W)))

  carries(0) := io.op

  for (i <- 0 until n) {
    FullAdder(cin = carries(i),
              in1 = io.in_1(i),
              in2 = io.in_2(i) ^ io.op,
              sum = sums(i),
              cout = carries(i + 1))
  }
  
  io.out := sums.asUInt()
  io.o_f := carries(n - 1) ^ carries(n)
}
