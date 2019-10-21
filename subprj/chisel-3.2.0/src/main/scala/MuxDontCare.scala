
import chisel3._
import chisel3.util._

/**
 * MuxにDontCareが指定可能になった。
 */
class MuxDontCare extends Module {
  val io = IO(new Bundle {
    val sel = Input(Bool())
    val in = Input(UInt(8.W))
    val out = Output(Bool())
  })

  io.out := Mux(io.sel, io.in, DontCare)
}

/**
 * MuxCaseも同様
 */
class MuxCaseDontCare extends Module {
  val io = IO(new Bundle {
    val sel = Input(UInt(2.W))
    val in1 = Input(UInt(8.W))
    val in2 = Input(UInt(8.W))
    val out = Output(Bool())
  })

  io.out := MuxCase(DontCare, Seq(
    io.sel(0) -> io.in1,
    io.sel(1) -> io.in2
  ))
}

/**
 * RTLの生成
 */
object ElaborateMuxDontCare extends App {
  Driver.execute(Array(
    "-td=rtl/chisel-3.2.0/MuxDontCare"
  ),
    () => new MuxDontCare)
  Driver.execute(Array(
    "-td=rtl/chisel-3.2.0/MuxCaseDontCare"
  ), () => new MuxCaseDontCare)
}