
import chisel3._
import chisel3.experimental.{NoChiselNamePrefix, chiselName}
import chisel3.stage.ChiselStage
import chisel3.util.Counter

// Note that this is not a Module
@chiselName
class Counter(w: Int) {
  val myReg = RegInit(0.U(w.W))
  val myRegA = RegInit(0.U(w.W))
  myReg := myReg + 1.U
  myRegA := myReg + 1.U
}

@chiselName
class TestNoChiselNamePrefix extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(8.W))
  })

  // Name of myReg will be "counter0_myReg"
  val counter0 = new Counter(8)
  // Name of myReg will be "myReg"
  val counter1 = new Counter(8) with NoChiselNamePrefix
  io.out := counter0.myRegA + counter1.myRegA
}

object ElaborateNoChiselNamePrefix extends App {
  println((new ChiselStage).emitVerilog(new TestNoChiselNamePrefix, args))
}