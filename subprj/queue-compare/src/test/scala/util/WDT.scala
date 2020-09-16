// See README.md for license details.

package sni.util

import chisel3._
import chisel3.util._

/**
 * Watch Dog Timer
 * @param limit タイムアウトするサイクル数
 */
class WDT(limit: Int) extends Module {
  val io = IO(new Bundle {
    val expired = Output(Bool())
  })

  val ctr = Counter(limit)
  val expired = RegInit(false.B)


  ctr.inc()

  when (ctr.value === (limit - 1).U) {
    expired := true.B
  }

  assert(!expired, s"WDT is expired. count = ${ctr.value}")

  io.expired := expired
}
