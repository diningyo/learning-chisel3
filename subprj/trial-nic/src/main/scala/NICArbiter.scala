// See LICENSE for license details.

import chisel3._
import chisel3.util._


class NICArbiterIO(numOfInput: Int) extends Bundle {
  val in = Vec(numOfInput, Flipped(Decoupled(new NICBaseData(numOfInput))))
  val out = Decoupled(new NICBaseData(numOfInput))

  override def cloneType: this.type = new NICArbiterIO(numOfInput).asInstanceOf[this.type]
}


class NICArbiter(defaultNumOfInput: Int) extends Module{
  val io = IO(new NICArbiterIO(defaultNumOfInput))

  io := DontCare
}
