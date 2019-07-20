// See LICENSE for license details.

import chisel3._
import chisel3.iotesters._

import scala.util.Random

/**
  * NICTopのユニットテスト用クラス
  * @param c テスモジュール
  */
class NICTopUnitTester(c: NICTop) extends PeekPokeTester(c) {

  val in = c.io.in
  val out = c.io.out

  val r = new Random(1)

  def idle(cycle: Int = 1): Unit = {
    for (in_port <- in) {
      poke(in_port.valid, false)
      poke(in_port.bits.dst, 0)
      poke(in_port.bits.data, 0)
    }

    for (out_port <- out) {
      poke(out_port.ready, true)
    }

    step(cycle)
  }

  /**
    * データ送信
    * @param src 入力ポート番号
    * @param dst 出力ポート番号
    * @param data 送信データ
    */
  def sendData(src: Int, dst: Int, data: BigInt): Unit = {
    poke(in(src).valid, true)
    poke(in(src).bits.dst, dst)
    poke(in(src).bits.data, data)
  }

  /**
    * データの期待値比較
    * @param dst 出力ポート番号
    * @param data 送信データ
    */
  def compareData(dst: Int, data: BigInt): Unit = {
    expect(out(dst).bits.dst, dst)
    expect(out(dst).bits.data, data)

    for ((out_port, idx) <- out.zipWithIndex) {
      val validExp = if (dst == idx) true else false
      expect(out_port.valid, validExp)
    }
  }
}

/**
  * NICTopのテストクラス
  */
class NICTopTester extends BaseTester {

  behavior of "NICTop"

  val testArgs = baseArgs :+ "-tn=NICTop"
  val numOfInput = 3
  val numOfOutput = 4

  /**
    * NICのパラメータ生成
    * @param numOfInput 入力ポート数
    * @param numOfOutput 出力ポート数
    * @param inSliceEn 入力のレジスタスライス設定
    * @param outSliceEn 出力のレジスタスライス設定
    * @return NICの入出力のパラメータ
    */
  def getPortParams(numOfInput: Int,
                    numOfOutput: Int,
                    inSliceEn: Boolean = false,
                    outSliceEn: Boolean = false
                   ): NICParams = {
    val inParams = Seq.fill(numOfInput)(new NICPortParams(inSliceEn))
    val outParams = Seq.fill(numOfOutput)(new NICPortParams(outSliceEn))

    new NICParams(inParams, outParams)
  }

  it should "io.in(N).validをHighにした時のio.in(N).dstに従って出力ポートが選択される" in {
    val p = getPortParams(numOfInput, numOfOutput)
    iotesters.Driver.execute(
      testArgs :+ "-td=test_run_dir/NICTop-000",
      () => new NICTop(p)) {
      c => new NICTopUnitTester(c) {

        idle(10)

        for (src <- 0 until numOfInput; dst <- 0 until numOfOutput) {
          val data = intToUnsignedBigInt(r.nextInt())
          sendData(src, dst, data)
          compareData(dst, data)

          step(1)
          poke(in(src).valid, false)

          step(1)

          idle(10)
        }
      }
    } should be (true)
  }
}
