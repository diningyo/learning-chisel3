// See LICENSE for license details.

import chisel3._
import chisel3.util._

/**
  * NICArbiterのIOクラス
  * @param numOfInput 入力ポートの数
  */
class NICArbiterIO(numOfInput: Int) extends Bundle {
  val in = Vec(numOfInput,
    Flipped(Decoupled(new NICBaseData(numOfInput))))
  val out = Decoupled(new NICBaseData(numOfInput))

  override def cloneType: this.type =
    new NICArbiterIO(numOfInput).asInstanceOf[this.type]
}

/**
  * NICArbiter
  * @param numOfInput 出力ポートの数
  * @param sliceEn 出力ポートの手前にレジスタスライス挿入するかどうか
  */
class NICArbiter(numOfInput: Int, sliceEn: Boolean)
  extends Module{
  val io = IO(new NICArbiterIO(numOfInput))

  val arb = Module(new Arbiter(
    chiselTypeOf(io.in(0).bits), numOfInput))
  val slice = Queue(arb.io.out, 1, !sliceEn, !sliceEn)

  for ((in_port, arb_in) <- io.in zip arb.io.in) {
    arb_in <> in_port
  }

  io.out <> slice
}
