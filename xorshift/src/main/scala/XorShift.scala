// See LICENSE for license details.

import chisel3._
import chisel3.util.{log2Ceil}

abstract class XorShiftBase extends Module


/**
  *  XorShift32
  *  とりあえず32bitベタ書き版
  */
class XorShift32Fixed(seed: BigInt, range: List[BigInt]) extends XorShiftBase {

  require(range.length == 2, "range size must be 2 (min: Int, max: Int)")

  val min :: max :: Nil = range

  val outBits = log2Ceil(max)

  require(outBits <= 32, s"output bit width <= 32")

  val io = IO(new Bundle {
    val update = Input(Bool())
    val randEn = Output(Bool())
    val randOut = Output(UInt(outBits.W))
  })

  val calcBuf = RegInit(seed.asUInt(32.W))

  when(io.update) {
    val y0 = calcBuf ^ (calcBuf << 13.U).asUInt()(31, 0)
    val y1 = y0 ^ (y0 >> 17.U).asUInt()(31, 0)
    val y2 = y1 ^ (y1 << 5.U).asUInt()(31, 0)
    calcBuf := y2
  }

  io.randEn := true.B
  io.randOut := calcBuf(outBits - 1, 0)
}
