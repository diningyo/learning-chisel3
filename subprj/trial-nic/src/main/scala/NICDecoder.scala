// See LICENSE for license details.

import chisel3._
import chisel3.util._

/**
  * NICDecoderのIOクラス
  * @param numOfOutput 出力ポートの数
  */
class NICDecoderIO(numOfOutput: Int) extends Bundle {
  val in = Flipped(Decoupled(new NICBaseData(numOfOutput)))
  val out = Vec(numOfOutput, Decoupled(new NICBaseData(numOfOutput)))

  override def cloneType: this.type = new NICDecoderIO(numOfOutput).asInstanceOf[this.type]
}

/**
  * NICDecoder
  * @param numOfOutput 出力ポートの数
  */
class NICDecoder(numOfOutput: Int, sliceEn: Boolean) extends Module {
  val io = IO(new NICDecoderIO(numOfOutput))

  val q = Queue(io.in, 1, !sliceEn, !sliceEn)

  val chosen_readies = Seq.fill(numOfOutput)((Wire(Bool()), Wire(Bool())))

  for ((out_port, idx) <- io.out.zipWithIndex) {
    // qの出力のdstがポートのインデックスと一致すれば選択された状態
    val chosen = q.bits.dst === idx.U

    out_port <> q
    out_port.valid := chosen && q.valid

    // out側の各readyを格納
    chosen_readies(idx)._1 := chosen
    chosen_readies(idx)._2 := out_port.ready
  }

  // in側のreadyの接続
  q.ready := MuxCase(false.B, chosen_readies)

}
