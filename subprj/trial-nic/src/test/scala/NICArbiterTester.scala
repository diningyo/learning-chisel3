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

  val testArgs = baseArgs :+ s"-tn=$it"
  val defaultNumOfInput = 3

  it should "" in {
    iotesters.Driver.execute(
      testArgs :+ s"-td=test_run_dir/$it",
      () => new NICArbiter(defaultNumOfInput)) {
      c => new NICArbiterUnitTester(c) {
        fail
      }
    } should be (true)
  }
}
