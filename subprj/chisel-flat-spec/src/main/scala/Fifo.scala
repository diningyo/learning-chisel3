// See LICENSE for license details.

package fifo

import chisel3._
import chisel3.util.{MuxCase, log2Ceil}

class SimpleIO(dataBits: Int, fifoStage: Int) extends Bundle {
  val wren = Input(Bool())
  val wrData = Input(UInt(dataBits.W))
  val rden = Input(Bool())
  val rdData = Output(UInt(dataBits.W))
  val count = Output(UInt((log2Ceil(fifoStage) + 1).W))
  val empty = Output(Bool())
  val full = Output(Bool())

  override def cloneType: SimpleIO.this.type = new SimpleIO(dataBits, fifoStage).asInstanceOf[this.type]
}

class Fifo(dataBits: Int, fifoStage: Int) extends Module {
  val io = IO(new SimpleIO(dataBits, fifoStage))

  require(0 < dataBits)
  require(0 < fifoStage)

  // log2Ceil(1) == 0 になるのでその場合だけ1に固定
  val addrBits = if (fifoStage == 1) 1 else log2Ceil(fifoStage)

  /**
    * アドレスを更新
    * @param currAddr
    * @return 更新後のアドレス
    */
  def updataAddr(currAddr: UInt): UInt = {
    val wrap = currAddr === (fifoStage - 1).U
    return Mux(wrap, 0.U, currAddr + 1.U)
  }

  // データカウント
  val fifoCount = RegInit(0.U((addrBits + 1).W))

  fifoCount := MuxCase(fifoCount, Seq(
    (io.rden && io.wren) -> fifoCount,
    io.rden -> (fifoCount - 1.U),
    io.wren -> (fifoCount + 1.U)
  ))

  // リード/ライトアドレス
  val rdAddr = RegInit(0.U(addrBits.W))
  val wrAddr = RegInit(0.U(addrBits.W))

  when (io.rden) {
    rdAddr := updataAddr(rdAddr)
  }

  when (io.wren) {
    wrAddr := updataAddr(wrAddr)
  }

  // 内部バッファ
  val fifoBuf = RegInit(VecInit(Seq.fill(fifoStage)(0.U(dataBits.W))))

  when (io.wren) {
    fifoBuf(wrAddr) := io.wrData
  }

  // 出力ポートの接続
  io.rdData := fifoBuf(rdAddr)
  io.empty := fifoCount === 0.U
  io.count := fifoCount
  io.full := fifoCount === fifoStage.U
}
