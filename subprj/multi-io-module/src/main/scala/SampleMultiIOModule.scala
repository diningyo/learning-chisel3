

import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule
import chisel3.iotesters.PeekPokeTester

class SomeData extends Bundle {
  val a = UInt(8.W)
  val b = UInt(16.W)
  val c = UInt(24.W)
}

class SomeDataIO extends DecoupledIO(new SomeData) {
  override def cloneType: this.type = new SomeDataIO().asInstanceOf[this.type]
}

/**
  * デバッグポート用のBundle
  * @param bits
  */
class DebugIO(bits: Int) extends Bundle {
  val count = Output(UInt(bits.W))
  override def cloneType: this.type = new DebugIO(bits).asInstanceOf[this.type]
}

/**
  * MultiIOModule使ったデバッグポート作成のサンプル
  * @param debug
  */
class DebugWithMultiIOModule(debug: Boolean) extends MultiIOModule {

  val bits = log2Ceil(4)

  val io = IO(new Bundle {
    val in = Flipped(new SomeDataIO)
    val out = new SomeDataIO
  })
  val q = Module(new Queue(chiselTypeOf(io.in.bits), 4))

  q.io.enq <> io.in
  io.out <> q.io.deq

  // debug
  val dbgBits = if (debug) 32 else 0
  val dbg = IO(new DebugIO(dbgBits))

  dbg.count := q.io.count
}

object Test extends App {

  val module = "DebugWithMultiIOModule"

  iotesters.Driver.execute(
    Array(
      s"-tn=$module",
      s"-td=test_run_dir/$module"
    ),
    () => new DebugWithMultiIOModule(true)) {
    c => new PeekPokeTester(c) {
      step(100)
    }
  }
}
