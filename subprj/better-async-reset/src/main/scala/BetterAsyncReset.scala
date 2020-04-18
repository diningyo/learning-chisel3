// See README.md for license details.

import chisel3._
import chisel3.stage._
import chisel3.util._

//class SubModule extends Module with RequireSyncReset {
class SubModule extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  io.out := RegNext(io.in, false.B)
}

class BetterAsyncReset extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val m_sub = Module(new SubModule)

  m_sub.io.in := RegNext(io.in, false.B)

  io.out := m_sub.io.out
}

object Elaborate extends App {

  val module = args(0) match {
    case "async" => () => new BetterAsyncReset with RequireAsyncReset
    case "sync" => () => new BetterAsyncReset
  }

  Driver.execute(Array("-td=rtl", "-tn=BetterAsyncReset"), module)
  //ChiselStage.execute(Array("-td=rtl", "-tn=BetterAsyncReset_true"),  () => new BetterAsyncReset(true))
  //ChiselMain.stage.execute()
}
