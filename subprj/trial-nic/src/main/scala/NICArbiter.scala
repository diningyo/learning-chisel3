// See LICENSE for license details.

import chisel3._
import chisel3.util._


class NICArbiterIO(numOfInput: Int) extends Bundle {
  val in = Vec(numOfInput, Flipped(Decoupled(new NICBaseData(numOfInput))))
  val out = Decoupled(new NICBaseData(numOfInput))

  override def cloneType: this.type = new NICArbiterIO(numOfInput).asInstanceOf[this.type]
}


class NICArbiter(defaultNumOfInput: Int, sliceEn: Boolean) extends Module{
  val io = IO(new NICArbiterIO(defaultNumOfInput))

  io := DontCare

  val arb = Module(new Arbiter(chiselTypeOf(io.in(0).bits), defaultNumOfInput))
  val slice = Queue(arb.io.out, 1, !sliceEn, !sliceEn)

  for ((in_port, arb_in) <- io.in zip arb.io.in) {
    arb_in <> in_port
  }

  io.out <> slice
}
