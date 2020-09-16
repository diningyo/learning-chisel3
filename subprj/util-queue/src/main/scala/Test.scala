
import chisel3._
import chisel3.util._

class dti_wrapper extends Module {
  val io = IO(new Bundle {
    val clk_DR = Input (Clock())// Bypass register clock
    val TDI = Input (UInt(1.W))// data in
    val bypass_en = Input (Bool())// enable signal
    val captureDR = Input (Bool())// captureDR signal

    val TDO_bypass      = Output (UInt(1.W))// Serial data out
  })

  val mod = Module(new dti_bypass_register)

  io <> mod.io
}


class dti_bypass_register extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk_DR = Input (Clock())// Bypass register clock
    val TDI = Input (UInt(1.W))// data in
    val bypass_en = Input (Bool())// enable signal
    val captureDR = Input (Bool())// captureDR signal

    val TDO_bypass      = Output (UInt(1.W))// Serial data out

  })
  setResource("/dti_bypass_register.v")
}

object dti_bypass_registerDriver extends App {
  chisel3.Driver.execute(args, () => new dti_wrapper)
}