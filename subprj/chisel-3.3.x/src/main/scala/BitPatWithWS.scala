
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class BitPatWithWS extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(Bool())
  })

  io.out := io.in === BitPat("b???? 0001")
}

object ElaborateBitPatWithWS extends App {
  println((new ChiselStage).emitVerilog(new BitPatWithWS, args))
}