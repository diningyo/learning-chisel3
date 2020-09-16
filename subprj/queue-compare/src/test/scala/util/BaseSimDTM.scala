// See README.md for license details.

package sni.util

import chisel3._

/**
  * シミュレーションの結果出力端子
  */
trait BaseSimDTMIO extends Bundle {
  val finish = Output(Bool())
  val result = Output(Bool())
}

/**
 * シミュレーション用テストベンチ
 * @param limit タイムアウトするサイクル数
 */
abstract class BaseSimDTM(limit: Int) extends Module {
  val io: BaseSimDTMIO
  val wdt = Module(new WDT(limit))
}
