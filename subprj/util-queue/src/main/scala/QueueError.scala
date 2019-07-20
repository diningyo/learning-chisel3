
import chisel3._
import chisel3.util._

class AIO extends Bundle {
  val b = Bool()
  val c = UInt(8.W)
}

class VecErrorIO extends Bundle {
  val a = Decoupled(new AIO)
}

class VecError extends Module {
  val io = IO(new Bundle {
    val sel = Input(Bool())
    val idx_0 = Input(UInt(2.W))
    val idx_1 = Input(UInt(2.W))
    val in = Vec(4, Flipped(new VecErrorIO))
    val out = new VecErrorIO
  })

  // エラー再現用なので、必要な端子以外はDontCareにしてある
  io := DontCare

  // io.selの値に応じてio.in(N).aをio.out.aに接続しようしてエラー
  io.out.a := Mux(io.sel, io.in(0).a, io.in(1).a)
  //val idx = Mux(io.sel, io.idx_0, io.idx_1)
  //io.out.a <> io.in(idx).a
}

object Elaborate extends App {
  Driver.execute(Array(
    "-tn=VecError",
    "-td=rtl/VecError"
  ),
    () => new VecError
  )
}


class MyIO extends Bundle {
  val strb = UInt(4.W)
  val data = UInt(32.W)
}

/*
class MyModule extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new MyIO))
    val out = Decoupled(new MyIO)
  })

  val q = Module(new Queue(io.in.bits, 4))

  q.io.deq <> io.out
}
*/


class MyModule extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new MyIO))
    val out = Decoupled(new MyIO)
  })

  val q = Module(new Queue(chiselTypeOf(io.in.bits), 4))

  q.io.enq <> io.in
  io.out <> q.io.deq
}

object ElaborateTypeError extends App {
  Driver.execute(Array(
    "-tn=VecError",
    "-td=rtl/VecError"
  ),
    () => new MyModule
  )
}