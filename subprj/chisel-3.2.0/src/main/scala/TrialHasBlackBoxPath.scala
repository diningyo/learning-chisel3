
import chisel3._
import chisel3.util._


class VerilogA extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val aa = Output(Bool())
  })

  addPath("subprj/chisel-3.2.0/src/main/resources/vsrc/VerilogA.sv")
}

class VerilogB extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val b = Input(Bool())
    val bb = Output(Bool())
  })

  addPath("subprj/chisel-3.2.0/src/main/resources/vsrc/VerilogB.sv")
}


class TrialHasBlackBoxPath extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val aa = Output(Bool())
    val b = Input(Bool())
    val bb = Output(Bool())
  })

  val m_aaa = Module(new VerilogA)
  val m_bbb = Module(new VerilogB)

  m_aaa.io.a := io.a
  io.aa := m_aaa.io.aa

  m_bbb.io.b := io.b
  io.bb := m_bbb.io.bb

  val a: Bool = 0.B
  val b: Bool = 1.B
  val c: Bool = 2.B
}
