
import chisel3._
import chisel3.util.experimental._
import firrtl.annotations.MemoryLoadFileType

class MemVerilogStyle extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(4.W))
    val rden = Input(Bool())
    val rddata = Output(UInt(32.W))
  })

  val m_mem = Mem(16, UInt(32.W))

  loadMemoryFromFile(m_mem, "subprj/chisel-3.2.0/src/main/resources/test.hex", hexOrBinary = MemoryLoadFileType.Hex)

  io.rddata := Mux(io.rden, m_mem.read(io.addr), 0.U)
}
