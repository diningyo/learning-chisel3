// See LICENSE for license details.

import chisel3._
import chisel3.util.{Cat, log2Ceil}

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

/// ここから下がパラメタライズ版 ///

sealed trait XorShiftN {
  def bits: Int
  def bufNum: Int
  def regBits: Int

  val shiftFunc:Array[UInt => UInt] = Array()
}

case object XorShift32 extends XorShiftN {
  override def bits: Int = 32
  override def bufNum: Int = 3
  override def regBits: Int = 32

  val y0 = (y: UInt) => y ^ (y << 13.U).asUInt()(regBits - 1, 0)
  val y1 = (y: UInt) => y ^ (y >> 17.U).asUInt()(regBits - 1, 0)
  val y2 = (y: UInt) => y ^ (y << 5.U).asUInt()(regBits - 1, 0)
  val y3 = (y: UInt) => y

  override val shiftFunc = Array(y0, y1, y2, y3)
}

case object XorShift64 extends XorShiftN {
  override def bits: Int = 64
  override def bufNum: Int = 2
  override def regBits: Int = 32
}

case object XorShift96 extends XorShiftN {
  override def bits: Int = 96
  override def bufNum: Int = 3
  override def regBits: Int = 32
}

case object XorShift128 extends XorShiftN {
  override def bits: Int = 128
  override def bufNum: Int = 3
  override def regBits: Int = 32
}

case object XorShift256 extends XorShiftN {
  override def bits: Int = 256
  override def bufNum: Int = 4
  override def regBits: Int = 64
}

class XorShift(randN: XorShiftN, seed: BigInt, range: List[BigInt]) extends Module {

  require(seed != 0, "seed != 0")
  require(range.length == 2, "range size must be 2 (min: Int, max: Int)")

  val min :: max :: Nil = range

  val outBits = log2Ceil(max)

  require(outBits <= randN.bits, s"output bit width <= ${randN.bits}")

  val io = IO(new Bundle {
    val update = Input(Bool())
    val randEn = Output(Bool())
    val randOut = Output(UInt(outBits.W))
  })

  val bufNum = randN.bufNum

  val initVal = Seq.fill(bufNum)(0.U(32.W))
  val calcBuf = WireInit(VecInit(initVal))
  val rand = RegInit(seed.asUInt(32.W))

  (Vec(rand) ++ calcBuf, calcBuf, randN.shiftFunc).zipped.map {
    case (x, y, f) =>  when (io.update) {
      y := f(x)
      printf("(x, y) = (%x, %x)\n", x, y)
    }
  }

  when(io.update) {
    rand := calcBuf(bufNum - 1)
  }

  io.randEn := true.B
  io.randOut := Cat(calcBuf.reverse)(outBits - 1, 0)
}
