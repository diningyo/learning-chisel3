// See README.md for license details.


import chisel3._
import chisel3.util._

class MyBundle extends Bundle {
  val b = Bool()
  val vec_uint = Vec(2, UInt(8.W))
}

class ObjTestVec extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(2, new MyBundle))
    val out = Output(Vec(2, new MyBundle))
  })
  io.out := io.in
}
