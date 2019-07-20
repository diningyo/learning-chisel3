// See LICENSE for license details.

import chisel3._
import chisel3.iotesters._

import scala.util.Random

class NICArbiterUnitTester(c: NICArbiter) extends PeekPokeTester(c) {
  val in = c.io.in
  val out = c.io.out

  val r = new Random(1)

  def idle(cycle: Int = 1): Unit = {

    for (in_port <- in) {
      poke(in_port.valid, false)
      poke(in_port.bits.dst, 0)
      poke(in_port.bits.data, 0)
    }

    poke(out.ready, true)

    step(cycle)
  }

  /**
    * データ送信
    * @param src 入力ポート番号
    * @param data 送信データ
    */
  def sendData(src: Int, data: BigInt): Unit = {
    poke(in(src).valid, true)
    poke(in(src).bits.dst, src)
    poke(in(src).bits.data, data)
  }

  /**
    * データの期待値比較
    * @param src 入力ポート番号
    * @param data 送信データ
    */
  def compareData(src: Int, data: BigInt): Unit = {
    expect(out.valid, true)
    expect(out.bits.dst, src)
    expect(out.bits.data, data)
  }
}

class NICArbiterTester extends BaseTester {

  behavior of "NICArbiter"

  val testArgs = baseArgs :+ s"-tn=NICArbiter"
  val defaultNumOfInput = 3

  it should "特定の１ポートからのみ要求が合った場合はその要求がoutに出力される" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICArbiter-000",
      () => new NICArbiter(defaultNumOfInput, false)) {
      c => new NICArbiterUnitTester(c) {
        idle(10)

        for (idx <- 0 until defaultNumOfInput) {
          val data = intToUnsignedBigInt(r.nextInt())
          sendData(idx, data)
          compareData(idx, data)
          step(1)
          idle(10)
        }
      }
    } should be (true)
  }

  it should "アービターの種類を通常のArbiterにした場合、ポートのインデックスの小さいものからoutに出力される" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICArbiter-001",
      () => new NICArbiter(defaultNumOfInput, false)) {
      c => new NICArbiterUnitTester(c) {
        idle(10)


        val dataSeq = Seq.fill(defaultNumOfInput)(intToUnsignedBigInt(r.nextInt()))

        for ((data, idx) <- dataSeq.zipWithIndex) {
          sendData(idx, data)
        }

        for (idx <- 0 until defaultNumOfInput) {
          step(1)
          compareData(idx, dataSeq(idx))
          poke(in(idx).valid, false)
        }

        idle(10)
      }
    } should be (true)
  }


  //
  // レジスタスライス入れた場合
  //
  it should "特定の１ポートからのみ要求が合った場合はその要求が1cycle後にoutに出力される" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICArbiter-100",
      () => new NICArbiter(defaultNumOfInput, true)) {
      c => new NICArbiterUnitTester(c) {
        idle(10)

        for (idx <- 0 until defaultNumOfInput) {
          val data = intToUnsignedBigInt(r.nextInt())
          sendData(idx, data)
          step(1)
          compareData(idx, data)
          idle(10)
        }
      }
    } should be (true)
  }

  it should "アービターの種類を通常のArbiterにした場合、最初の要求から1cycle後にポートのインデックスの小さいものからoutに出力される" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/NICArbiter-101",
      () => new NICArbiter(defaultNumOfInput, true)) {
      c => new NICArbiterUnitTester(c) {
        idle(10)


        val dataSeq = Seq.fill(defaultNumOfInput)(intToUnsignedBigInt(r.nextInt()))

        for ((data, idx) <- dataSeq.zipWithIndex) {
          sendData(idx, data)
        }

        // レジスタスライスが挿入されたので同じサイクルではvalidはfalse
        expect(out.valid, false)
        val readies = scala.collection.mutable.Seq.fill(defaultNumOfInput)(BigInt(0))


        for (idx <- 0 until defaultNumOfInput) {
          for (j <- 0 until defaultNumOfInput) {
            readies(j) = peek(in(j).ready)
          }
          step(1)
          if (readies(idx) == 0x1) {
            poke(in(idx).valid, false)
          }
          compareData(idx, dataSeq(idx))
          step(1)
        }

        step(1)
        idle(10)
      }
    } should be (true)
  }
}
