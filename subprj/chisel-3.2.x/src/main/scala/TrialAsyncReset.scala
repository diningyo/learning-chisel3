
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

/**
 * 非同期リセットの使い方の確認コード
 */
@chiselName
class TrialAsyncReset extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(UInt(2.W))
    val out_a = Output(Bool())
    val out_b = Output(Bool())
    val out_aa = Output(Bool())
  })

  val r_a = withReset(reset.asAsyncReset()) {
    RegInit(0.U(8.W))
  }

  r_a := io.a

  withReset(reset.asAsyncReset()) {
    val r_b = RegInit(2.U)
    val r_c = RegNext(io.a, 0.B)

    r_b := io.b
    io.out_b := r_b
    io.out_aa := r_c
  }

  io.out_a := r_a
}

/**
 * RTLの生成
 */
object ElaborateTrialAsyncReset extends App {
  Driver.execute(
    Array(
      "-td=rtl/chisel-3.2.0/TrialAsyncReset"
    ), () => new TrialAsyncReset)
}