
import chisel3._
import chisel3.util._

class a extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val rtlList = Seq("a.sv", "b.sv")

  rtlList.foreach( f => setResource(s"/$f"))
}

class BlackBoxTop extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val m_a = Module(new a)

  m_a.io.in := io.in
  io.out := m_a.io.out
}

object Elaborate extends App {
  Driver.execute(Array(
    "-tn=BlackBoxTop",
    "-td=rtl/BlackBoxTop"
  ),
    () => new BlackBoxTop
  )
}
