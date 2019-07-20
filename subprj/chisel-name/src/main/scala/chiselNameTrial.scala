
import chisel3._
import chisel3.experimental.chiselName

//@chiselName
class TestMod extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(Bool())
    val c = Output(UInt(4.W))
    val d = Output(UInt(4.W))
  })

  io.d := 0.U

  when (io.a) {
    val innerReg = RegInit(5.U(4.W))
    innerReg := innerReg + 1.U
    when (io.b) {
      val innerRegB = RegInit(innerReg)
      io.d := innerRegB + 1.U
    }
    io.c := innerReg
  } .otherwise {
    io.c := 10.U
  }
}

object Elaborate extends App {
  Driver.execute(Array(
    "-tn=TestMod",
    "-td=rtl/chiselName"
  ),
  () => new TestMod)
}