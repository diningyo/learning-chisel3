// See LICENSE for license details.

import chisel3._
import chisel3.util._

/**
  * NIC用のデータクラス
  * @param numOfPort ポート数
  */
class NICBaseData(numOfPort: Int) extends Bundle {
  val dst = UInt(log2Ceil(numOfPort).W)
  val data = UInt(32.W)

  override def cloneType: this.type = new NICBaseData(numOfPort).asInstanceOf[this.type]
}

/**
  * ポートのパラメータ用クラス
  * @param sliceEn レジスタスライス設定
  */
class NICPortParams(val sliceEn: Boolean)

/**
  * NICのパラメータ用クラス
  * @param inParams 入力側の設定
  * @param outParams 出力側の設定
  */
class NICParams(
  val inParams: Seq[NICPortParams],
  val outParams: Seq[NICPortParams]
) {
  val numOfInput = inParams.length
  val numOfOutput = outParams.length
}

/**
  * NICTopのIOクラス
  * @param p NICの設定パラメータ
  */
class NICIO(val p: NICParams) extends Bundle {

  val numOfInput = p.inParams.length
  val numOfOutput = p.outParams.length
  val in = Vec(numOfInput, Flipped(Decoupled(new NICBaseData(numOfInput))))
  val out = Vec(numOfOutput, Decoupled(new NICBaseData(numOfOutput)))

  override def cloneType: this.type = new NICIO(p).asInstanceOf[this.type]
}

/**
  * NIC
  * @param p NICの設定パラメータ
  */
class NICTop(p: NICParams) extends Module {
  val io = IO(new NICIO(p))

  val decs = p.inParams.map(dp => Module(new NICDecoder(p.numOfOutput, dp.sliceEn)))
  val arbs = p.outParams.map(ap => Module(new NICArbiter(p.numOfInput, ap.sliceEn)))

  // io.in(n) <-> NICDecoder(n).io.in
  for ((in_port, dec) <- io.in zip decs) {
    dec.io.in <> in_port
  }

  // dec.io.out(M) <-> arb.io.in(N)
  for ((dec, i) <- decs.zipWithIndex; (arb, j) <- arbs.zipWithIndex) {
    dec.io.out(j) <> arb.io.in(i)
  }

  // io.out(N) <-> arb.io.out(N)
  for ((arb, out_port) <- arbs zip io.out) {
    out_port <> arb.io.out
  }
}
