
import chisel3._
import chisel3.experimental.chiselName
import chisel3.stage.ChiselStage
import chisel3.util._

@chiselName
class Sample {
  val a = Reg(Bool())
  val b = Wire(UInt())

  b := a << 2.U
}

class TestChiselName extends MultiIOModule{

  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(UInt(2.W))
  })

  val s = new Sample
  s.a := io.in
  io.out := s.b
}

object ElaborateChiselName extends App {
  println((new ChiselStage).emitVerilog(new TestChiselName, args))
}