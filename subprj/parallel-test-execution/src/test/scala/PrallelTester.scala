// See LICENSE for license details.

import chisel3._
import chisel3.iotesters.{Driver, PeekPokeTester, ChiselFlatSpec}
import org.scalatest.ParallelTestExecution


/**
  * 入力信号を所定のサイクル遅らせて出力
  * @param delay 何サイクル出力を遅らせるか(delay > 0)
  */
class DelayBuf(delay: Int) extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt(32.W))
    val out = Output(UInt(32.W))
  })

  val delayBuf = Seq(WireInit(io.in)) ++ Seq.fill(delay)(RegInit(0.U(32.W)))

  delayBuf.zip(delayBuf.tail).foreach { case (curr, next) => next := curr }
  io.out := delayBuf(delay)
}

/**
  * DelayBufのテストのスーパークラス
  */
abstract class DelayedBufTester extends ChiselFlatSpec {

  behavior of "DelayBuf"

  for (n <- 0 until 600) {
    it should s"${n}サイクル信号が遅れて出力される" in {
      Driver.execute(Array[String](
        "--backend-name=verilator",
        "--generate-vcd-output=on",
        s"--target-dir=test_run_dir/delay-$n"
      ), () => new DelayBuf(n)) {
        c => new PeekPokeTester(c) {
          reset()
          step(1)

          poke(c.io.in, n)

          step(n)

          var prev = n
          for (setVal <- 0 until 500) {
            poke(c.io.in, setVal + n)

            for (_ <- 0 until n) {
              expect(c.io.out, prev)
              step(1)
            }
            expect(c.io.out, setVal + n)
            prev = setVal + n
            step(1)
          }
        }
      } should be (true)
    }
  }
}

/**
  * シーケンシャルにテストするモジュール
  */
class SequentialTester extends DelayedBufTester

/**
  * パラレルにテストするモジュール
  */
class ParallelTester extends DelayedBufTester with ParallelTestExecution


