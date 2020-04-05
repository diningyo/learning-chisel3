// See README.md for license details.

import chisel3._
import chisel3.util._

class SubModule extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  io.out := RegNext(io.in, false.B)
}

class BetterAsyncReset(useAsyncReset: Boolean = false) extends Module{
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val w_async_reset = reset.asAsyncReset()

  val m_sub = if (useAsyncReset) {
    withReset(w_async_reset) {
      Module(new SubModule)
    }
  } else {
    Module(new SubModule())
  }

  withReset(w_async_reset) {
    m_sub.io.in := RegNext(io.in, false.B)
  }

  io.out := m_sub.io.out
}

object Elaborate extends App {
  Driver.execute(Array("-td=rtl", "-tn=BetterAsyncReset_false"), () => new BetterAsyncReset(false))
  Driver.execute(Array("-td=rtl", "-tn=BetterAsyncReset_true"),  () => new BetterAsyncReset(true))
}
