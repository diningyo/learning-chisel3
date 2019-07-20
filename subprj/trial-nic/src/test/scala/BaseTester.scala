// See LICENSE for license details.

import chisel3._
import chisel3.iotesters._

class BaseTester extends ChiselFlatSpec {

  val baseArgs = Array(
    "-tgvo=on",
    "-tbn=treadle"
  )

}
