// See LICENSE for license details.

import chisel3._
import chisel3.util._
import chisel3.iotesters._

/**
  * Arbiterのユニットテストクラス
  * @param c RRArbiter
  */
class ArbiterUnitTester(c: RRArbiter[UInt]) extends PeekPokeTester(c) {

  val in = c.io.in
  val out = c.io.out

  /**
    * バスリクエスト発行
    * @param portIdx リクエストを発行するポート番号
    */
  def req(portIdx: Int): Unit = {
    poke(c.io.in(portIdx).valid, true)
    poke(c.io.in(portIdx).bits, portIdx)
  }
}

/**
  * RRArbiterのテストクラス
  */
class ArbiterTester extends ChiselFlatSpec {
  behavior of "RRArbiter"

  val numOfInput = 4

  it should "以下のテストを実行するとin(2)/in(3)のvalidが同時にLOWに落ちる" in {
    iotesters.Driver.execute(
      Array(
        "-tn=RRAbiter",
        "-td=test_run_dir/RRArbiter-error",
        "-tgvo=on"
      ),
      () => new RRArbiter(UInt(8.W), numOfInput)) {
      c => new ArbiterUnitTester(c) {

        poke(out.ready, true)

        // すべてのポートで同時にリクエスト発行
        for (portIdx <- 0 until numOfInput) {
          req(portIdx)
        }
        step(1)

        for (idx <- 0 until numOfInput) {
          println(s"[$t] chosen port = ${peek(c.io.chosen)}\n" +
            s"valid_0 / ready_0 = ${peek(in(0).valid)}, ${peek(in(0).ready)}\n" +
            s"valid_1 / ready_1 = ${peek(in(1).valid)}, ${peek(in(1).ready)}\n" +
            s"valid_2 / ready_2 = ${peek(in(2).valid)}, ${peek(in(2).ready)}\n" +
            s"valid_3 / ready_3 = ${peek(in(3).valid)}, ${peek(in(0).ready)}"
            )
          expect(out.valid, true)
          for (portIdx <- 0 until numOfInput) {
            if (peek(in(portIdx).ready) == 0x1) {
              poke(in(portIdx).valid, false)
            }
          }
          step(1)
        }
        expect(out.valid, false)
      }
    } should be (true)
  }

  it should "in(N).readyを取得してからvalidを制御するとうまくいく" in {
    iotesters.Driver.execute(
      Array(
        "-tn=RRAbiter",
        "-td=test_run_dir/RRArbiter-ok",
        "-tgvo=on"
      ),
      () => new RRArbiter(UInt(8.W), numOfInput)) {
      c => new ArbiterUnitTester(c) {

        poke(out.ready, true)

        // すべてのポートで同時にリクエスト発行
        for (portIdx <- 0 until numOfInput) {
          req(portIdx)
        }
        step(1)

        for (idx <- 0 until numOfInput) {
          expect(out.valid, true)

          // pokeの発行前にio(N).readyの情報を取得
          val in_readies = in.map(p => peek(p.ready))

          for ((ready, in_port) <- in_readies zip in) {
            if (ready == 0x1) {
              poke(in_port.valid, false)
            }
          }
          step(1)
        }
        expect(out.valid, false)
        step(10)
      }
    } should be (true)
  }
}