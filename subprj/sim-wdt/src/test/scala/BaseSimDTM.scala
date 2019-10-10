
import chisel3._
import chisel3.iotesters._
import chisel3.util._

/**
  * Watchdog Timer
  * @param limit シミュレーションのMAXサイクル数
  * @param abortEn timeout時にassertでシミュレーションを終了するかどうか
  */
class WDT(limit: Int, abortEn: Boolean) extends Module {
  val io = IO(new Bundle {
    val timeout = Output(Bool())
  })

  val timer = RegInit(0.U(log2Ceil(limit).W))

  when (timer =/= limit.U) {
    timer := timer + 1.U
  }

  val timeout = timer === limit.U

  if (abortEn) {
    assert(!timeout, "WDT is expired!!")
  } else {
    printf("WDT is expired!!")
  }

  io.timeout := timeout
}

/**
  * BaseSimDTM用の信号
  */
trait BaseSimDTMIO extends Bundle {
  val timeout = Output(Bool())
  val finish = Output(Bool())
}

/**
  * シミュレーションのトップモジュール
  * @param limit シミュレーションのMAXサイクル数
  * @param abortEn timeout時にassertでシミュレーションを終了するかどうか
  */
abstract class BaseSimDTM(limit: Int, abortEn: Boolean = true) extends Module {
  val io: BaseSimDTMIO
  val wdt = Module(new WDT(limit, abortEn))

  def connect(finish: Bool): Unit = {
    io.finish := finish
    io.timeout := wdt.io.timeout
  }
}

/**
  * テスト用のシミュレーショントップモジュール
  * @param limit シミュレーションのMAXサイクル数
  * @param abortEn timeout時にassertでシミュレーションを終了するかどうか
  */
class SimDTM(limit: Int, abortEn: Boolean = true) extends BaseSimDTM(limit, abortEn) {
  val io = IO(new Bundle with BaseSimDTMIO)

  connect(false.B)
}

/**
  * SimDTM用テストクラス
  */
class SimDTMTester extends ChiselFlatSpec {
  "SimDTM" should "limitの設定したサイクルが経過するとassertで終了する" in {
    iotesters.Driver.execute(
      Array("-tgvo=on"), () => new SimDTM(20, true)) {
      c => new PeekPokeTester(c) {
        for (_ <- 0 until 25) {
          println(f"step = $t")
          step(1)
        }
      }
    }
  }
}