
import chisel3._
import chisel3.util._

/**
 * addPathの使い方の確認
 */
class VerilogA extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val aa = Output(Bool())
  })

  addPath("subprj/chisel-3.2.0/src/main/resources/vsrc/VerilogA.sv")
}

/**
 * addPathの使い方の確認
 */
class VerilogB extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val b = Input(Bool())
    val bb = Output(Bool())
  })

  // resources以外の場所でも読み込める
  addPath("subprj/chisel-3.2.0/src/main/vsrc/VerilogB.sv")
}

/**
 * HasBlackBoxPathの確認用トップモジュール
 */
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
}
