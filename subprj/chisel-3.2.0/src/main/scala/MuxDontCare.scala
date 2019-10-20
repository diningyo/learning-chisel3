
import chisel3._
import chisel3.util._

class MuxDontCare extends Module {
  val io = IO(new Bundle {
    val sel = Input(Bool())
    val in = Input(UInt(8.W))
    val out = Output(Bool())
  })

  io.out := Mux(io.sel, io.in, DontCare)
}

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

object ElaborateMuxDontCare extends App {
  Driver.execute(args, () => new MuxDontCare)
  Driver.execute(args, () => new MuxCaseDontCare)
}