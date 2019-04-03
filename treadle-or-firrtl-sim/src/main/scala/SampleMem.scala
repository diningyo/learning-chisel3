// See LICENSE for license details.
import chisel3._
import chisel3.util.{Irrevocable, log2Ceil}

class SimpleMem(size: Int) extends Module {
  val io = IO(new Bundle {
    val wren = Input(Bool())
    val rden = Input(Bool())
    val addr = Input(UInt(log2Ceil(size).W))
    val wrData = Input(UInt(8.W))
    val rdData = Output(UInt(8.W))
  })

  val mem = Mem(size, UInt(32.W))

  when (io.wren) {
    //printf("[memWrite](addr, wrData) = (%x, %x)\n", io.addr, io.wrData)
    mem(io.addr) := io.wrData
  }

  io.rdData := mem(io.addr)
}
