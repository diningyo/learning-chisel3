// See LICENSE for license details.

import scala.util.Random

import chisel3._
import chisel3.iotesters._

/**
  * NICDecoderのユニットテスト用クラス
  * @param c 出力ポートの数
  */
class NICDecoderUnitTester(c: NICDecoder) extends PeekPokeTester(c) {

  val in = c.io.in
  val out = c.io.out

  val r = new Random(1)

  def idle(cycle: Int = 1): Unit = {
    poke(in.valid, false)
    poke(in.bits.dst, 0)
    poke(in.bits.data, 0)

    for (out_port <- out) {
      poke(out_port.ready, true)
    }

    step(cycle)
  }

  /**
    * データ送信
    * @param dst 出力先のポート番号
    * @param data 送信データ
    */
  def sendData(dst: Int, data: BigInt): Unit = {
    poke(in.valid, true)
    poke(in.bits.dst, dst)
    poke(in.bits.data, data)
  }

  /**
    * データの期待値比較
    * @param dst 出力先のポート番号
    * @param data 送信データ
    */
  def compareData(dst: Int, data: BigInt): Unit = {
    for ((out_port, idx) <- out.zipWithIndex) {
      val validExp = if (dst == idx) true else false
      expect(out_port.valid, validExp)
      expect(out_port.bits.dst, dst)
      expect(out_port.bits.data, data)
    }
  }
}

/**
  * NICDecoderのテストクラス
  */
class NICDecoderTester extends BaseTester {

  behavior of "NICDecoder"

  val testArgs = baseArgs :+ "-tn=NICDecoder"
  val defaultNumOfOutput = 3

  it should "io.in.validをHighにした時のio.dstに従って出力ポートが選択される" in {
    iotesters.Driver.execute(
      testArgs :+ "-td=test_run_dir/NICDecoder-000",
      () => new NICDecoder(defaultNumOfOutput, false)) {
      c => new NICDecoderUnitTester(c) {

        idle(10)

        for (dst <- 0 until defaultNumOfOutput) {
          val data = intToUnsignedBigInt(r.nextInt())
          sendData(dst, data)
          compareData(dst, data)

          step(1)
          poke(in.valid, false)

          step(1)

          idle(10)
        }
      }
    } should be (true)
  }

  it should "選択した出力先のout(N).io.readyがLowになるとio.in.readyもLowになる" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICDecoder-001",
      () => new NICDecoder(defaultNumOfOutput, false)) {
      c => new NICDecoderUnitTester(c) {

        idle(10)

        for (dst <- 0 until defaultNumOfOutput) {
          val data = intToUnsignedBigInt(r.nextInt())

          // 選択したポートのみreadyをfalseに
          for (idx <- 0 until defaultNumOfOutput) {
            val v = if (dst == idx) false else true
            println(s"$v")
            poke(out(idx).ready, v)
          }

          sendData(dst, data)

          // データがqueueに格納されるため、このサイクル
          // ではreadyはtrue
          expect(in.ready, true)

          step(1)

          poke(in.valid, false)
          compareData(dst, data)
          expect(in.ready, false)

          step(1)

          idle(10)
        }
      }
    } should be (true)
  }

  it should "io.in.validをHighにした時のio.dstに従って出力ポートが選択され、1cycle後にvalidがHighになる" in {
    iotesters.Driver.execute(
      testArgs :+ "-td=test_run_dir/NICDecoder-100",
      () => new NICDecoder(defaultNumOfOutput, true)) {
      c => new NICDecoderUnitTester(c) {

        idle(10)

        for (dst <- 0 until defaultNumOfOutput) {
          val data = intToUnsignedBigInt(r.nextInt())
          sendData(dst, data)

          step(1)
          poke(in.valid, false)
          compareData(dst, data)

          step(1)

          idle(10)
        }
      }
    } should be (true)
  }

  it should "選択した出力先のout(N).io.readyがLowになるとio.in.readyが1cycle後にLowになる" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICDecoder-101",
      () => new NICDecoder(defaultNumOfOutput, true)) {
      c => new NICDecoderUnitTester(c) {

        idle(10)

        for (dst <- 0 until defaultNumOfOutput) {
          val data = intToUnsignedBigInt(r.nextInt())

          // 選択したポートのみreadyをfalseに
          for (idx <- 0 until defaultNumOfOutput) {
            val v = if (dst == idx) false else true
            println(s"$v")
            poke(out(idx).ready, v)
          }

          sendData(dst, data)

          step(1)

          compareData(dst, data)
          poke(in.valid, false)
          expect(in.ready, false)

          step(1)

          idle(10)
        }
      }
    } should be (true)
  }
}
