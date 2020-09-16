
import chisel3._
import chisel3.util._

class pmp_simple extends Module
{
  val io = IO(new Bundle {
    val vld  = Input(Bool())
    val addr = Input(UInt(32.W))
    val en   = Output(Bool())
  })

  def check_region(addr: UInt, base: Int, length: Int) : Bool = {
    val cond = addr >= base.U(64.W) && addr < (base + length).U(64.W)
    Mux(cond, true.B, false.B)
  }

  //val in_data = dontTouch(Wire(Bool()))
  val in_data = Wire(Bool())
  in_data := Mux(io.vld, check_region(io.addr, 0x3000, 0x1000), false.B)
  io.en := in_data
}

object Elaborate extends App {
  Driver.execute(args, () => new pmp_simple)
}