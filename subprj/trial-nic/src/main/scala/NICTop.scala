// See LICENSE for license details.

import chisel3._
import chisel3.util._

class NICBaseData(numOfPort: Int) extends Bundle {
  val dst = UInt(log2Ceil(numOfPort).W)
  val data = UInt(32.W)

  override def cloneType: this.type = new NICBaseData(numOfPort).asInstanceOf[this.type]
}

class NICTop extends Module {
  val io = IO(new Bundle {})
}
